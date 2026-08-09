package fi.cartio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import fi.cartio.core.model.ProductCategory

class Converters {
    @TypeConverter fun category(value: String) = ProductCategory.valueOf(value)
    @TypeConverter fun category(value: ProductCategory) = value.name
}

@Database(entities = [ShoppingItemEntity::class, SavedShoppingListEntity::class, SavedShoppingListItemEntity::class, ActiveShoppingListEntity::class, LearnedProductCategoryEntity::class, ProductUsageEntity::class], version = 3, exportSchema = true)
@TypeConverters(Converters::class)
abstract class CartioDatabase : RoomDatabase() {
    abstract fun dao(): CartioDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `active_list` (`singletonId` INTEGER NOT NULL, `savedListId` INTEGER NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`singletonId`))")
                db.execSQL("INSERT INTO `saved_lists` (`name`, `createdAt`) SELECT 'Shopping list', CAST(strftime('%s','now') AS INTEGER) * 1000 WHERE EXISTS (SELECT 1 FROM `shopping_items`)")
                db.execSQL("INSERT INTO `active_list` (`singletonId`, `savedListId`, `name`, `createdAt`) SELECT 1, last_insert_rowid(), 'Shopping list', CAST(strftime('%s','now') AS INTEGER) * 1000 WHERE EXISTS (SELECT 1 FROM `shopping_items`)")
                db.execSQL("INSERT INTO `saved_list_items` (`listId`, `name`, `normalizedName`, `quantity`, `unit`, `category`, `checked`) SELECT (SELECT `savedListId` FROM `active_list` WHERE `singletonId` = 1), `name`, `normalizedName`, `quantity`, `unit`, `category`, `checked` FROM `shopping_items` WHERE EXISTS (SELECT 1 FROM `active_list`)")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `shopping_items` ADD COLUMN `sortOrder` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `saved_list_items` ADD COLUMN `sortOrder` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE `shopping_items` SET `sortOrder` = `id`")
                db.execSQL("UPDATE `saved_list_items` SET `sortOrder` = `id`")
            }
        }
    }
}
