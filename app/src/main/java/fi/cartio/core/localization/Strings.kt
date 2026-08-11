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
    val removed: String, val undo: String, val expandCategory: String, val collapseCategory: String,
    val searchLists: String, val noMatchingLists: String,
    val catalogAttribution: String,
    val addTypedProduct: String,
    val productNotFound: String,
    val savedEmptyTitle: String, val savedEmptyBody: String, val itemCount: String, val moreOptions: String,
    val offlineDescription: String,
    val whatWouldYouLike: String, val createNewList: String, val openSavedLists: String, val listsStayOnDevice: String,
    val createList: String, val currentList: String, val listProgress: String, val switchList: String,
    val active: String, val manageSavedLists: String,
    val reorder: String, val moveUp: String, val moveDown: String, val listIcon: String,
    val aboutTagline: String, val aboutSummary: String, val privacyAndData: String,
    val privacySummary: String, val localStorage: String, val localStorageBody: String,
    val androidBackup: String, val androidBackupBody: String, val permissions: String,
    val permissionsBody: String, val versionFormat: String,
    val developerAndSupport: String, val developedBy: String, val contactSupport: String,
    val privacyPolicy: String, val privacyPolicyBody: String,
    val legalAndLicenses: String, val copyrightNotice: String, val allRightsReserved: String,
    val legalNotices: String, val legalNoticesBody: String,
    val listActions: String, val editList: String, val markAllIncomplete: String,
    val removeCompleted: String, val deleteList: String, val deleteListConfirmation: String,
    val markedIncomplete: String, val completedRemoved: String, val listDeleted: String,
)

val LocalStrings = staticCompositionLocalOf { strings(AppLanguage.ENGLISH) }
fun strings(language: AppLanguage) = if (language == AppLanguage.FINNISH) CartioStrings(
    "Ostoslista", "Päänäkymä", "Tallennetut", "Asetukset", "Lisää tuote", "Hae tai kirjoita tuote",
    "Viimeksi lisätyt", "Usein lisätyt", "Lista on vielä tyhjä", "Lisää ensimmäinen tuote nopeasti plus-painikkeesta.",
    "Tallenna lista", "Listan nimi", "Ota käyttöön", "Nimeä uudelleen", "Poista", "Kieli / Language", "Teema", "Tietoja sovelluksesta",
    "Vaalea", "Tumma", "Järjestelmän mukaan", "Suomi", "English", "Peruuta", "Tallenna", "lisätty",
    "Muokkaa tuotetta", "Tuotteen nimi", "Määrä", "Yksikkö", "Kategoria", "poistettu", "Kumoa", "Laajenna kategoria", "Tiivistä kategoria",
    "Hae listoja", "Hakua vastaavia listoja ei löytynyt", "Tuoteluettelo: Fineli / THL, CC BY 4.0", "Lisää ”%s” listalle", "Etkö löytänyt etsimääsi tuotetta?",
    "Ei tallennettuja listoja", "Tallenna usein käyttämäsi ostoslista, niin saat sen myöhemmin käyttöön yhdellä napautuksella.", "%d tuotetta", "Lisää toimintoja", "Nopea, rauhallinen ja täysin offline.",
    "Mitä haluaisit tehdä?", "Luo uusi lista", "Avaa tallennetut listat", "Listasi säilyvät tällä laitteella", "Luo lista", "NYKYINEN LISTA", "%d tuotetta · %d valmiina", "Vaihda listaa", "Käytössä", "Hallitse tallennettuja listoja", "Järjestä vetämällä", "Siirrä ylös", "Siirrä alas", "Listan ikoni",
    "Ostokset yksinkertaisemmin.", "Nopea ja offline-ensisijainen ostoslista jokapäiväiseen käyttöön.", "Yksityisyys ja tiedot",
    "Ei käyttäjätiliä, mainoksia, analytiikkaa tai verkkoyhteyttä. Cartio ei lähetä listojasi minnekään.", "Tallennettu laitteelle", "Listat, asetukset ja tuotehistoria tallennetaan paikallisesti laitteellesi.",
    "Android-varmuuskopio", "Laitteesi asetuksista riippuen Android voi sisällyttää Cartion tiedot järjestelmän varmuuskopioihin tai laitteiden väliseen siirtoon.", "Käyttöoikeudet", "Cartio ei tällä hetkellä pyydä laitteesi käyttöoikeuksia.", "Versio %s (%d)",
    "Kehittäjä ja tuki", "Kehittäjä", "Ota yhteyttä tukeen",
    "Tietosuojakäytäntö", "Lue Cartion tietosuojakäytäntö",
    "Lakiasiat ja lisenssit", "© 2026 Juha-Matti Niiranen", "Kaikki oikeudet pidätetään.",
    "Lisenssitiedot", "Avoimen lähdekoodin ohjelmistot ja aineistojen käyttöehdot",
    "Listan toiminnot", "Muokkaa listaa", "Merkitse kaikki ostamattomiksi",
    "Poista ostetut tuotteet", "Poista lista", "Haluatko varmasti poistaa listan ”%s”?",
    "Kaikki tuotteet merkittiin ostamattomiksi", "Ostetut tuotteet poistettiin", "Lista poistettiin"
) else CartioStrings(
    "Shopping list", "Main", "Saved", "Settings", "Add product", "Search or type a product",
    "Recently added", "Frequently added", "Your list is empty", "Add your first product quickly with the plus button.",
    "Save list", "List name", "Use list", "Rename", "Delete", "Language", "Theme", "About Cartio",
    "Light", "Dark", "System default", "Finnish", "English", "Cancel", "Save", "added",
    "Edit product", "Product name", "Quantity", "Unit", "Category", "removed", "Undo", "Expand category", "Collapse category",
    "Search lists", "No matching lists found", "Product catalog: Fineli / THL, CC BY 4.0", "Add “%s” to list", "Didn't find the product you were looking for?",
    "No saved lists", "Save a shopping list you use often and restore it later with one tap.", "%d items", "More options", "Fast, calm, and completely offline.",
    "What would you like to do?", "Create new list", "Open saved lists", "Your lists stay on this device", "Create list", "CURRENT LIST", "%d items · %d completed", "Switch list", "Active", "Manage saved lists", "Drag to reorder", "Move up", "Move down", "List icon",
    "Shopping, simplified.", "A fast, offline-first shopping list designed for everyday errands.", "Privacy & data",
    "No account, ads, analytics, or network access. Cartio does not send your lists anywhere.", "Stored on your device", "Lists, preferences, and product history are stored locally on your device.",
    "Android backup", "Depending on your device settings, Android may include Cartio data in system backups or device transfers.", "Permissions", "Cartio currently requests no device permissions.", "Version %s (%d)",
    "Developer & support", "Developed by", "Contact support",
    "Privacy policy", "Read Cartio's privacy policy",
    "Legal & licenses", "© 2026 Juha-Matti Niiranen", "All rights reserved.",
    "License notices", "Open-source software and data licensing terms",
    "List actions", "Edit list", "Mark all as not completed",
    "Remove completed products", "Delete list", "Are you sure you want to delete “%s”?",
    "All products marked as not completed", "Completed products removed", "List deleted"
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
