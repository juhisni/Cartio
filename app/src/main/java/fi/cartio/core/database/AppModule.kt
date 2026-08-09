package fi.cartio.core.database

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fi.cartio.data.local.CartioDao
import fi.cartio.data.local.CartioDatabase
import fi.cartio.data.repository.OfflineCartioRepository
import fi.cartio.domain.repository.CartioRepository
import fi.cartio.domain.suggestion.CategorySuggestionEngine
import fi.cartio.domain.suggestion.OfflineCategorySuggestionEngine
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton fun database(@ApplicationContext context: Context): CartioDatabase = Room.databaseBuilder(context, CartioDatabase::class.java, "cartio.db").build()
    @Provides fun dao(database: CartioDatabase): CartioDao = database.dao()
}

@Module @InstallIn(SingletonComponent::class)
abstract class BindingModule {
    @Binds abstract fun repository(impl: OfflineCartioRepository): CartioRepository
    @Binds abstract fun engine(impl: OfflineCategorySuggestionEngine): CategorySuggestionEngine
}
