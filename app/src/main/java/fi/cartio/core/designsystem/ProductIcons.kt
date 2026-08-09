package fi.cartio.core.designsystem

import fi.cartio.core.model.ProductCategory
import java.text.Normalizer
import java.util.Locale

/** Product-first icon selection. Order is intentional: specific foods precede broad families. */
fun productIcon(name: String, category: ProductCategory): String {
    val value = name.iconKey()
    return when {
        value.has("talouspaperi", "paper towel", "wc paperi", "toilet paper") -> "🧻"
        value.has("astianpesuaine", "dish soap", "pyykinpesuaine", "laundry detergent", "huuhteluaine", "fabric softener", "puhdistusaine", "cleaner", "kasisaippua", "hand soap", "kasidesi", "hand sanitizer") -> "🧴"
        value.has("roskapussi", "trash bag") -> "🗑️"
        value.has("leivinpaperi", "baking paper", "alumiinifolio", "aluminium foil", "tuorekelmu", "cling film") -> "📜"
        value.has("siivoussieni", "cleaning sponge") -> "🧽"
        value.has("tiskiharja", "dish brush") -> "🧹"
        value.has("patteri", "batter") -> "🔋"

        // Product form wins over an ingredient embedded in the name.
        value.has("perunalastu", "potato crisp", "maissilastu", "tortilla chip", "banaanilastu", "banana chip", "omenalastu", "apple chip", "naksu", "snack", "valipalapatukka", "cereal bar", "riisikakku", "rice cake") -> "🍿"
        value.has("hillo", "jam", "marmeladi", "marmalade", "sose", "puree", "tahna", "paste", "tahini") -> "🫙"
        value.has("viinietikka", "vinegar", "vakiviinaetikka") -> "🍶"
        value.has("kastike", "sauce", "liemi", "stock", "bouillon", "ketsuppi", "ketchup", "pikkelssi", "relish") -> "🫙"
        value.has("oljy", "oil", "paistinrasva", "lard", "tali", "tallow") -> "🫒"
        value.has("kermalikoori", "cream liqueur", "likoori", "liqueur", "konjakki", "cognac", "rommi", "rum", "viski", "whisky", "gin long drink") -> "🥃"
        value.has("riisijuoma", "rice drink", "soijajuoma", "soya drink", "kaurajuoma", "oat drink", "mantelijuoma", "almond drink", "kookosjuoma", "coconut drink", "pahkinajuoma", "nut drink", "proteiinijuoma", "protein drink") -> "🥛"
        value.has("mehu", "juice", "marjajuoma", "berry nectar", "hedelmajuoma", "fruit drink") -> "🧃"
        value.has("ateriankorvike", "meal replacement") -> "🥤"
        value.has("rasvajauhe", "fat powder", "kastikejauhe", "sauce mix") -> "🥣"

        value.has("vesimeloni", "watermelon") -> "🍉"
        value.has("hunajameloni", "melon", "cantaloupe") -> "🍈"
        value.has("banaani", "banana") -> "🍌"
        value.has("ananas", "pineapple") -> "🍍"
        value.has("appelsiini", "orange", "mandariini", "mandarin", "greippi", "grapefruit") -> "🍊"
        value.has("sitruuna", "lemon") -> "🍋"
        value.has("omena", "apple") -> "🍎"
        value.has("paaryna", "pear") -> "🍐"
        value.has("persikka", "peach", "nektariini", "nectarine", "aprikoosi", "apricot") -> "🍑"
        value.has("kirsikka", "cherry") -> "🍒"
        value.has("viinirypale", "grape") -> "🍇"
        value.has("mansikka", "strawberry") -> "🍓"
        value.has("mustikka", "blueberry", "puolukka", "lingonberry", "karpalo", "cranberry", "herukka", "currant", "vadelma", "raspberry", "marja", "berry") -> "🫐"
        value.has("kiivi", "kiwi") -> "🥝"
        value.has("kookos", "coconut") -> "🥥"
        value.has("avokado", "avocado") && !value.has("oljy", "oil") -> "🥑"
        value.has("mango", "papaya", "papaija", "guava", "passion", "granaattiomena", "pomegranate", "viikuna", "fig", "taateli", "date", "hedelma", "fruit") && !value.has("juoma", "drink", "hillo", "jam") -> "🥭"

        value.has("valkosipuli", "garlic") -> "🧄"
        value.has("sipuli", "onion", "purjo", "leek", "ruohosipuli", "chives") -> "🧅"
        value.has("porkkana", "carrot") -> "🥕"
        value.has("bataatti", "sweet potato") -> "🍠"
        value.has("peruna", "potato") && !value.has("lastu", "crisp", "french fries") -> "🥔"
        value.has("maissi", "corn", "maize") && !value.has("oljy", "oil", "tarkkelys", "starch") -> "🌽"
        value.has("parsakaali", "broccoli") -> "🥦"
        value.has("kurkku", "cucumber") -> "🥒"
        value.has("tomaatti", "tomato", "ketsuppi", "ketchup") -> "🍅"
        value.has("munakoiso", "aubergine", "eggplant") -> "🍆"
        value.has("chili", "jalapeno") -> "🌶️"
        value.has("paprika", "sweet pepper") && !value.has("jauhe", "powder") -> "🫑"
        value.has("sieni", "mushroom", "kantarelli", "chanterelle", "rousku", "tatti", "bolete", "osterivinokas") -> "🍄"
        value.has("herne", "pea", "papu", "bean", "linssi", "lentil", "kikherne", "chick pea") -> "🫘"
        value.has("pahkina", "nut", "manteli", "almond", "cashew", "kastanja", "chestnut") -> "🥜"
        value.has("oliivi", "olive") && !value.has("oljy", "oil") -> "🫒"
        value.has("kurpitsa", "pumpkin") -> "🎃"
        value.has("inkivaari", "ginger") -> "🫚"
        value.has("lanttu", "swede", "nauris", "turnip", "punajuuri", "beetroot", "retiisi", "radish", "palsternakka", "parsnip", "selleri", "celery", "artisokka", "artichoke", "parsa", "asparagus", "fenkoli", "fennel") -> "🥕"
        value.has("salaatti", "lettuce", "kaali", "cabbage", "pinaatti", "spinach", "rukola", "rocket", "mangoldi", "chard", "merileva", "seaweed") -> "🥬"
        value.has("voikukka", "dandelion") -> "🌼"
        value.has("persilja", "parsley", "tilli", "dill", "basilika", "basil", "oregano", "timjami", "thyme", "korianteri", "coriander", "yrtti", "herb", "verso", "shoot", "nokkonen", "nettle", "maitohorsma", "fireweed", "vuohenputki", "goutweed", "siankarsamo", "yarrow", "kuusenkerkka", "spruce bud") -> "🌿"
        value.has("vihannes", "vegetable", "itu", "sprout", "quorn") -> "🥬"

        value.has("kananmuna", "egg", "viiriaisen muna") -> "🥚"
        value.has("maito", "milk", "aidinmaito", "infant formula") && !value.has("suklaa", "chocolate", "kahvi", "coffee") -> "🥛"
        value.has("juusto", "cheese") -> "🧀"
        value == "voi" || value == "butter" || value.has("margariini", "levite", "spread") -> "🧈"
        value.has("jogurtti", "yoghurt", "yogurt", "viili", "curd milk", "rahka", "quark", "skyr", "mifu") -> "🥣"
        value.has("kerma", "cream", "smetana", "creme fraiche") -> "🥛"
        value.has("jaatelo", "ice cream") -> "🍨"
        value.has("sorbetti", "sorbet", "mehujaa", "ice lolly") -> "🍧"

        value.has("pekoni", "bacon") -> "🥓"
        value.has("makkara", "sausage", "nakki", "frankfurter", "meetvursti", "salami") -> "🌭"
        value.has("kana", "chicken", "broileri") -> "🍗"
        value.has("kalkkuna", "turkey", "lintu", "bird") -> "🦃"
        value.has("sian", "pork", "porsaan", "ham", "kinkku", "rypsiporsas", "viljaporsas", "kassler") -> "🐖"
        value.has("naudan", "beef", "vasikan", "veal", "jauheliha", "mince") -> "🥩"
        value.has("lampaan", "lamb") -> "🐑"
        value.has("poron", "reindeer", "hirven", "elk") -> "🦌"
        value.has("janis", "hare", "kani", "rabbit") -> "🐇"
        value.has("maksa", "liver", "munuainen", "kidney", "sydan", "heart", "kieli", "tongue", "liha", "meat") && !value.has("maitohorsma") -> "🥩"
        value.has("katkarapu", "shrimp") -> "🍤"
        value.has("rapu", "crayfish", "hummeri", "lobster") -> "🦞"
        value.has("simpukka", "mussel", "osteri", "oyster") -> "🦪"
        value.has("mustekala", "squid") -> "🦑"
        value.has("etana", "snail") -> "🐌"
        value.has("kaviaari", "caviar", "mati", "roe") -> "🟠"
        value.has("kala", "fish", "lohi", "salmon", "silakka", "herring", "silli", "turska", "cod", "kuha", "zander", "ahven", "perch", "hauki", "pike", "muikku", "vendace", "taimen", "trout", "tonnikala", "tuna", "sardiini", "sardine", "makrilli", "mackerel", "tilapia", "pangasius", "hoki", "seiti", "saithe") -> "🐟"

        value.has("croissant", "voisarvi") -> "🥐"
        value.has("leipa", "bread", "sampyla", "roll", "patonki", "baguette") -> "🍞"
        value.has("riisi", "rice") && !value.has("tarkkelys", "starch", "juoma", "drink") -> "🍚"
        value.has("pasta", "spaghetti", "makaroni", "macaroni", "nuudeli", "noodle") -> "🍝"
        value.has("kaura", "oat", "ruis", "rye", "ohra", "barley", "vehn", "wheat", "vilja", "cereal", "mallas", "malt", "hiutale", "flake", "suurimo", "semolina", "kuskus", "couscous", "kvinoa", "quinoa", "amarantti", "amaranth", "jauho", "flour", "lese", "bran") -> "🌾"
        value.has("siemen", "seed", "psyllium") -> "🌱"
        value.has("tofu", "tempeh", "soijaproteiini", "soy protein", "soijarouhe", "soya mince", "harkis", "nyhtokaura", "pulled oats") -> "🫘"

        value.has("suklaa", "chocolate", "kaakao", "cocoa") && !value.has("juoma", "drink") -> "🍫"
        value.has("purukumi", "chewing gum") -> "🫧"
        value.has("karkki", "candy", "makeinen", "sweet", "pastilli", "pastille", "lakritsi", "liquorice", "marsipaani", "marzipan", "halva") -> "🍬"
        value.has("hunaja", "honey") && !value.has("viinietikka", "vinegar") -> "🍯"
        value.has("hillo", "jam", "marmeladi", "marmalade", "sose", "puree", "tahna", "paste", "tahini") -> "🫙"
        value.has("perunalastu", "potato crisp", "maissilastu", "tortilla chip", "naksu", "snack", "valipalapatukka", "cereal bar", "riisikakku", "rice cake") -> "🍿"
        value.has("sokeri", "sugar", "fruktoosi", "fructose", "siirappi", "syrup", "makeutusaine", "sweetener", "sorbitoli", "ksylitoli", "xylitol", "maltodekstriini", "maltodextrin") -> "🍬"
        value.has("suola", "salt") -> "🧂"
        value.has("mustapippuri", "valkopippuri", "pepper", "kaneli", "cinnamon", "neilikka", "clove", "kardemumma", "cardamom", "mauste", "spice", "paprikajauhe") -> "🧂"
        value.has("sinappi", "mustard") -> "🌭"
        value.has("majoneesi", "mayonnaise", "kastike", "sauce", "liemi", "stock", "bouillon", "ketsuppi", "ketchup", "pikkelssi", "relish") -> "🫙"
        value.has("oljy", "oil", "paistinrasva", "lard", "tali", "tallow", "rasva", "fat") -> "🫒"
        value.has("etikka", "vinegar") -> "🍶"
        value.has("leivin", "baking", "ruokasooda", "baking soda", "hiiva", "yeast", "tarkkelys", "starch", "liivate", "gelatin", "jauhe", "powder") -> "🥣"

        value.has("kahvi", "coffee") -> "☕"
        value == "tee" || value == "tea" -> "🍵"
        value.has("olut", "beer", "kalja") -> "🍺"
        value.has("viini", "wine", "glogi", "mulled wine") -> "🍷"
        value.has("viina", "spirit", "konjakki", "cognac", "rommi", "rum", "viski", "whisky", "likoori", "liqueur", "gin") -> "🥃"
        value.has("siideri", "cider") -> "🍏"
        value.has("vesi", "water") && !value.has("kastanja", "chestnut") -> "💧"
        value.has("energiajuoma", "energy drink", "urheilujuoma", "sport beverage", "virkistysjuoma", "fitness drink") -> "⚡"
        value.has("mehu", "juice", "juoma", "drink", "virvoitus", "soft drink", "tonic", "tonikki", "sima", "mead") -> "🧃"

        else -> categoryIcon(category)
    }
}

fun categoryIcon(category: ProductCategory): String = when (category) {
    ProductCategory.FRUITS_VEGETABLES -> "🥬"
    ProductCategory.DAIRY -> "🥛"
    ProductCategory.BREAD_GRAINS -> "🌾"
    ProductCategory.MEAT_FISH -> "🐟"
    ProductCategory.FROZEN -> "❄️"
    ProductCategory.PANTRY -> "🥫"
    ProductCategory.DRINKS -> "🧃"
    ProductCategory.HOUSEHOLD -> "🧻"
    ProductCategory.OTHER -> "🛒"
}

private fun String.iconKey(): String = Normalizer.normalize(lowercase(Locale.ROOT), Normalizer.Form.NFD)
    .replace(Regex("\\p{M}+"), "")
    .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
    .trim()

private fun String.has(vararg terms: String) = terms.any { it in this }
