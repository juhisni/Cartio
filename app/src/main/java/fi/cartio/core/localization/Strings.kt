package fi.cartio.core.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import fi.cartio.core.model.AppLanguage
import fi.cartio.core.model.ProductCategory

data class CartioStrings(
    val shoppingList: String, val main: String, val saved: String, val settings: String,
    val addProduct: String, val searchHint: String, val recent: String, val frequent: String,
    val emptyTitle: String, val emptyBody: String, val saveList: String, val listName: String,
    val restore: String, val rename: String, val delete: String, val language: String,
    val theme: String, val appInfo: String, val light: String, val dark: String, val system: String,
    val finnish: String, val english: String, val cancel: String, val save: String, val added: String,
    val editProduct: String, val productName: String, val quantity: String, val unit: String, val category: String,
    val removed: String, val undo: String,
)

val LocalStrings = staticCompositionLocalOf { strings(AppLanguage.FINNISH) }
fun strings(language: AppLanguage) = if (language == AppLanguage.FINNISH) CartioStrings(
    "Ostoslista", "Päänäkymä", "Tallennetut", "Asetukset", "Lisää tuote", "Hae tai kirjoita tuote",
    "Viimeksi lisätyt", "Usein lisätyt", "Lista on vielä tyhjä", "Lisää ensimmäinen tuote nopeasti plus-painikkeesta.",
    "Tallenna lista", "Listan nimi", "Ota käyttöön", "Nimeä uudelleen", "Poista", "Kieli / Language", "Teema", "Tietoja sovelluksesta",
    "Vaalea", "Tumma", "Järjestelmän mukaan", "Suomi", "English", "Peruuta", "Tallenna", "lisätty",
    "Muokkaa tuotetta", "Tuotteen nimi", "Määrä", "Yksikkö", "Kategoria", "poistettu", "Kumoa"
) else CartioStrings(
    "Shopping list", "Main", "Saved", "Settings", "Add product", "Search or type a product",
    "Recently added", "Frequently added", "Your list is empty", "Add your first product quickly with the plus button.",
    "Save list", "List name", "Use list", "Rename", "Delete", "Language", "Theme", "About Cartio",
    "Light", "Dark", "System default", "Finnish", "English", "Cancel", "Save", "added",
    "Edit product", "Product name", "Quantity", "Unit", "Category", "removed", "Undo"
)

@Composable fun categoryName(category: ProductCategory): String {
    val fi = LocalStrings.current.main == "Päänäkymä"
    return if (fi) when (category) {
        ProductCategory.FRUITS_VEGETABLES -> "Hedelmät ja vihannekset"; ProductCategory.DAIRY -> "Maitotuotteet"
        ProductCategory.BREAD_GRAINS -> "Leipä ja viljat"; ProductCategory.MEAT_FISH -> "Liha ja kala"; ProductCategory.FROZEN -> "Pakasteet"
        ProductCategory.PANTRY -> "Kuivatuotteet"; ProductCategory.DRINKS -> "Juomat"; ProductCategory.HOUSEHOLD -> "Koti"; ProductCategory.OTHER -> "Muut"
    } else when (category) {
        ProductCategory.FRUITS_VEGETABLES -> "Fruits & vegetables"; ProductCategory.DAIRY -> "Dairy"
        ProductCategory.BREAD_GRAINS -> "Bread & grains"; ProductCategory.MEAT_FISH -> "Meat & fish"; ProductCategory.FROZEN -> "Frozen"
        ProductCategory.PANTRY -> "Pantry"; ProductCategory.DRINKS -> "Drinks"; ProductCategory.HOUSEHOLD -> "Household"; ProductCategory.OTHER -> "Other"
    }
}
