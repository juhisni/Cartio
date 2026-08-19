package fi.cartio

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.cartio.data.local.CartioDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CartioMigrationTest {
    private val databaseName = "cartio-migration-test.db"
    private lateinit var context: Context

    @Before fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(databaseName)
    }

    @After fun cleanUp() {
        context.deleteDatabase(databaseName)
    }

    @Test fun migrateVersion1To4PreservesProductsAndAddsOrderingAndIcons() {
        createVersionOneDatabase()
        val room = Room.databaseBuilder(context, CartioDatabase::class.java, databaseName)
            .addMigrations(CartioDatabase.MIGRATION_1_2, CartioDatabase.MIGRATION_2_3, CartioDatabase.MIGRATION_3_4)
            .build()
        val migrated = room.openHelper.writableDatabase

        assertRow(migrated, "SELECT name, checked, sortOrder FROM shopping_items WHERE id = 1") { cursor ->
            assertEquals("Milk", cursor.getString(0)); assertEquals(1, cursor.getInt(1)); assertEquals(1, cursor.getInt(2))
        }
        assertRow(migrated, "SELECT name, icon FROM saved_lists WHERE id = 10") { cursor ->
            assertEquals("Weekly", cursor.getString(0)); assertEquals("CART", cursor.getString(1))
        }
        assertRow(migrated, "SELECT name, sortOrder FROM saved_list_items WHERE id = 20") { cursor ->
            assertEquals("Bread", cursor.getString(0)); assertEquals(20, cursor.getInt(1))
        }
        room.close()
    }

    private fun createVersionOneDatabase() {
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(databaseName), null).use { database ->
            database.execSQL("CREATE TABLE shopping_items (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, normalizedName TEXT NOT NULL, quantity REAL, unit TEXT, category TEXT NOT NULL, checked INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
            database.execSQL("CREATE INDEX index_shopping_items_normalizedName ON shopping_items (normalizedName)")
            database.execSQL("CREATE TABLE saved_lists (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, createdAt INTEGER NOT NULL)")
            database.execSQL("CREATE TABLE saved_list_items (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, listId INTEGER NOT NULL, name TEXT NOT NULL, normalizedName TEXT NOT NULL, quantity REAL, unit TEXT, category TEXT NOT NULL, checked INTEGER NOT NULL, FOREIGN KEY(listId) REFERENCES saved_lists(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
            database.execSQL("CREATE INDEX index_saved_list_items_listId ON saved_list_items (listId)")
            database.execSQL("CREATE TABLE learned_categories (normalizedName TEXT NOT NULL PRIMARY KEY, category TEXT NOT NULL)")
            database.execSQL("CREATE TABLE product_usage (normalizedName TEXT NOT NULL PRIMARY KEY, displayName TEXT NOT NULL, category TEXT NOT NULL, useCount INTEGER NOT NULL, lastUsedAt INTEGER NOT NULL)")
            database.execSQL("INSERT INTO shopping_items (id, name, normalizedName, quantity, unit, category, checked, createdAt, updatedAt) VALUES (1, 'Milk', 'milk', 2.0, 'l', 'DAIRY', 1, 100, 100)")
            database.execSQL("INSERT INTO saved_lists (id, name, createdAt) VALUES (10, 'Weekly', 100)")
            database.execSQL("INSERT INTO saved_list_items (id, listId, name, normalizedName, quantity, unit, category, checked) VALUES (20, 10, 'Bread', 'bread', NULL, NULL, 'BREAD_GRAINS', 0)")
            database.version = 1
        }
    }

    private fun assertRow(database: SupportSQLiteDatabase, query: String, assertions: (android.database.Cursor) -> Unit) {
        database.query(query).use { cursor ->
            check(cursor.moveToFirst()) { "Expected migration row for: $query" }
            assertions(cursor)
        }
    }
}
