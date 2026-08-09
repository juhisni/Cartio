package fi.cartio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import fi.cartio.core.model.ProductCategory

class Converters {
    @TypeConverter fun category(value: String) = ProductCategory.valueOf(value)
    @TypeConverter fun category(value: ProductCategory) = value.name
}

@Database(entities = [ShoppingItemEntity::class, SavedShoppingListEntity::class, SavedShoppingListItemEntity::class, LearnedProductCategoryEntity::class, ProductUsageEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class CartioDatabase : RoomDatabase() { abstract fun dao(): CartioDao }
