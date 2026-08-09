package fi.cartio.domain.suggestion

import fi.cartio.core.model.ProductCategory
import fi.cartio.core.model.ProductSuggestion

internal data class CatalogProduct(
    val finnishName: String,
    val englishName: String,
    val category: ProductCategory,
    val finnishTerms: Set<String>,
    val englishTerms: Set<String>,
)

internal object BundledProductCatalog {
    private data class Modifier(val finnish: String, val english: String)

    private val bases = mapOf(
        ProductCategory.FRUITS_VEGETABLES to pairs("banaani|banana,omena|apple,appelsiini|orange,mandariini|mandarin,päärynä|pear,viinirypäleet|grapes,mansikka|strawberry,mustikka|blueberry,vadelma|raspberry,tomaatti|tomato,kurkku|cucumber,paprika|bell pepper,porkkana|carrot,peruna|potato,sipuli|onion"),
        ProductCategory.DAIRY to pairs("maito|milk,kauramaito|oat milk,soijamaito|soy milk,jogurtti|yogurt,rahka|quark,raejuusto|cottage cheese,juusto|cheese,kerma|cream,voi|butter,margariini|margarine,kananmunat|eggs,kermaviili|sour cream,tuorejuusto|cream cheese,mozzarella|mozzarella,feta|feta"),
        ProductCategory.BREAD_GRAINS to pairs("ruisleipä|rye bread,vaalea leipä|white bread,täysjyväleipä|wholegrain bread,sämpylä|bread roll,patonki|baguette,näkkileipä|crispbread,tortilla|tortilla,riisi|rice,kaurahiutaleet|oats,murot|cereal,mysli|muesli,ohra|barley,kvinoa|quinoa,couscous|couscous,maissihiutaleet|corn flakes"),
        ProductCategory.MEAT_FISH to pairs("kana|chicken,kalkkuna|turkey,naudanliha|beef,sianliha|pork,jauheliha|minced meat,pekoni|bacon,kinkku|ham,makkara|sausage,lihapullat|meatballs,lohi|salmon,tonnikala|tuna,turska|cod,katkaravut|shrimp,simpukat|mussels,tofu|tofu"),
        ProductCategory.FROZEN to pairs("pakastevihannekset|frozen vegetables,pakastemarjat|frozen berries,pakastepizza|frozen pizza,ranskanperunat|french fries,jäätelö|ice cream,pakastekala|frozen fish,pakastekana|frozen chicken,pakastekeitto|frozen soup,pakastepiirakka|frozen pie,pakastepulla|frozen bun,pakastepinaatti|frozen spinach,pakasteherneet|frozen peas,pakastemaissi|frozen corn,jääpalat|ice cubes,sorbetti|sorbet"),
        ProductCategory.PANTRY to pairs("pasta|pasta,makaroni|macaroni,spaghetti|spaghetti,jauho|flour,sokeri|sugar,suola|salt,kahvi|coffee,tee|tea,ruokaöljy|cooking oil,oliiviöljy|olive oil,etikka|vinegar,tomaattimurska|crushed tomatoes,pavut|beans,linssit|lentils,mausteet|spices"),
        ProductCategory.DRINKS to pairs("vesi|water,kivennäisvesi|sparkling water,appelsiinimehu|orange juice,omenamehu|apple juice,limonadi|soda,kolajuoma|cola,energiajuoma|energy drink,urheilujuoma|sports drink,kaakao|cocoa,mehutiiviste|juice concentrate,kookosvesi|coconut water,jäätee|iced tea,alkoholiton olut|non-alcoholic beer,smoothie|smoothie,kombucha|kombucha"),
        ProductCategory.HOUSEHOLD to pairs("talouspaperi|paper towel,wc-paperi|toilet paper,astianpesuaine|dish soap,pyykinpesuaine|laundry detergent,huuhteluaine|fabric softener,yleispuhdistusaine|all-purpose cleaner,saippua|soap,käsidesi|hand sanitizer,roskapussi|trash bag,leivinpaperi|baking paper,folio|aluminium foil,kelmu|cling film,siivoussieni|cleaning sponge,tiskiharja|dish brush,patterit|batteries"),
        ProductCategory.OTHER to pairs("koiranruoka|dog food,kissanruoka|cat food,kissan hiekka|cat litter,vauvanruoka|baby food,vaipat|diapers,kosteuspyyhkeet|wet wipes,hammastahna|toothpaste,hammasharja|toothbrush,shampoo|shampoo,hoitoaine|conditioner,suihkugeeli|shower gel,deodorantti|deodorant,siteet|sanitary pads,tamponit|tampons,partaterät|razor blades"),
    )

    private val modifiers = mapOf(
        ProductCategory.FRUITS_VEGETABLES to modifiers("|,luomu|organic,kotimainen|domestic,tuore|fresh,viipaloitu|sliced,pieni|small,iso|large"),
        ProductCategory.DAIRY to modifiers("|,luomu|organic,laktoositon|lactose-free,vähärasvainen|low-fat,rasvaton|fat-free,pieni|small,perhepakkaus|family pack"),
        ProductCategory.BREAD_GRAINS to modifiers("|,luomu|organic,täysjyvä|wholegrain,gluteeniton|gluten-free,kuitupitoinen|high-fibre,pieni|small,perhepakkaus|family pack"),
        ProductCategory.MEAT_FISH to modifiers("|,luomu|organic,kotimainen|domestic,marinoitu|marinated,suikale|strips,pieni|small,perhepakkaus|family pack"),
        ProductCategory.FROZEN to modifiers("|,pieni|small,iso|large,perhepakkaus|family pack,gluteeniton|gluten-free"),
        ProductCategory.PANTRY to modifiers("|,luomu|organic,gluteeniton|gluten-free,täysjyvä|wholegrain,sokeriton|sugar-free,pieni|small,iso|large"),
        ProductCategory.DRINKS to modifiers("|,sokeriton|sugar-free,luomu|organic,pieni|small,iso|large,monipakkaus|multipack"),
        ProductCategory.HOUSEHOLD to modifiers("|,hajusteeton|fragrance-free,ekologinen|eco-friendly,pieni|small,iso|large,monipakkaus|multipack"),
        ProductCategory.OTHER to modifiers("|,hajusteeton|fragrance-free,herkälle iholle|sensitive,pieni|small,iso|large,monipakkaus|multipack"),
    )

    val products: List<CatalogProduct> = bases.flatMap { (category, categoryBases) ->
        categoryBases.flatMap { (finnishBase, englishBase) ->
            modifiers.getValue(category).map { modifier -> product(finnishBase, englishBase, category, modifier) }
        }
    }

    val searchableTermCount: Int = products.flatMap { it.finnishTerms + it.englishTerms }.toSet().size

    private val exactCategories: Map<String, ProductCategory> = buildMap {
        products.forEach { product -> (product.finnishTerms + product.englishTerms).forEach { put(it, product.category) } }
    }

    private val keywords: List<Pair<String, ProductCategory>> = bases.flatMap { (category, values) ->
        values.flatMap { listOf(it.first to category, it.second to category) }
    }

    private val defaultNames = listOf("banaani", "maito", "ruisleipä", "kananmunat", "pasta", "lohi", "kurkku", "kahvi")

    fun exactCategory(normalized: String): ProductCategory? = exactCategories[normalized]

    fun keywordCategory(normalized: String): ProductCategory? = keywords.firstOrNull { (keyword, _) ->
        Regex("(^|\\s)${Regex.escape(keyword)}($|\\s)").containsMatchIn(normalized)
    }?.second

    fun suggestions(query: String): List<ProductSuggestion> {
        if (query.isBlank()) return defaultNames.mapNotNull { name -> products.firstOrNull { it.finnishTerms == setOf(name) } }.map { ProductSuggestion(it.finnishName, it.category) }
        return products.asSequence().mapNotNull { product ->
            val finnishMatch = product.finnishTerms.any { query in it }
            val englishMatch = product.englishTerms.any { query in it }
            when {
                !finnishMatch && !englishMatch -> null
                englishMatch && !finnishMatch -> ProductSuggestion(product.englishName, product.category)
                else -> ProductSuggestion(product.finnishName, product.category)
            }
        }.distinctBy { it.name }.take(8).toList()
    }

    private fun product(fiBase: String, enBase: String, category: ProductCategory, modifier: Modifier): CatalogProduct {
        if (modifier.finnish.isBlank()) return CatalogProduct(fiBase.cap(), enBase.cap(), category, setOf(fiBase), setOf(enBase))
        val fi = "${modifier.finnish} $fiBase"
        val en = "${modifier.english} $enBase"
        return CatalogProduct(fi.cap(), en.cap(), category, setOf(fi, "$fiBase ${modifier.finnish}"), setOf(en, "$enBase ${modifier.english}"))
    }

    private fun pairs(value: String) = value.split(',').map { entry -> entry.split('|').let { it[0] to it[1] } }
    private fun modifiers(value: String) = value.split(',').map { entry -> entry.split('|').let { Modifier(it[0], it[1]) } }
    private fun String.cap() = replaceFirstChar { it.titlecase() }
}
