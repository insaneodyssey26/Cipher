package com.example.cipherspend.core.domain

import com.example.cipherspend.core.domain.model.TransactionCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategorizerEngine @Inject constructor() {

    private val brandMappings = mapOf(
        "AMAZON" to TransactionCategory.SHOPPING,
        "FLIPKART" to TransactionCategory.SHOPPING,
        "MYNTRA" to TransactionCategory.SHOPPING,
        "AJIO" to TransactionCategory.SHOPPING,
        "MEESHO" to TransactionCategory.SHOPPING,
        "NYKAA" to TransactionCategory.SHOPPING,
        "RELIANCE" to TransactionCategory.SHOPPING,
        "CROMA" to TransactionCategory.SHOPPING,
        "BLINKIT" to TransactionCategory.SHOPPING,
        "BIGBASKET" to TransactionCategory.SHOPPING,
        "ZEPTO" to TransactionCategory.SHOPPING,
        "INSTAMART" to TransactionCategory.SHOPPING,
        "JIOMART" to TransactionCategory.SHOPPING,
        "ZOMATO" to TransactionCategory.FOOD,
        "SWIGGY" to TransactionCategory.FOOD,
        "EATFIT" to TransactionCategory.FOOD,
        "DOMINOS" to TransactionCategory.FOOD,
        "KFC" to TransactionCategory.FOOD,
        "PIZZA HUT" to TransactionCategory.FOOD,
        "STARBUCKS" to TransactionCategory.FOOD,
        "MCDONALDS" to TransactionCategory.FOOD,
        "BURGER KING" to TransactionCategory.FOOD,
        "UBER" to TransactionCategory.TRANSPORT,
        "OLA" to TransactionCategory.TRANSPORT,
        "RAPIDO" to TransactionCategory.TRANSPORT,
        "INDIGO" to TransactionCategory.TRANSPORT,
        "AIR INDIA" to TransactionCategory.TRANSPORT,
        "SPICEJET" to TransactionCategory.TRANSPORT,
        "IRCTC" to TransactionCategory.TRANSPORT,
        "REDBUS" to TransactionCategory.TRANSPORT,
        "MAKEMYTRIP" to TransactionCategory.TRANSPORT,
        "GOIBIBO" to TransactionCategory.TRANSPORT,
        "BOOKMYSHOW" to TransactionCategory.ENTERTAINMENT,
        "NETFLIX" to TransactionCategory.ENTERTAINMENT,
        "SPOTIFY" to TransactionCategory.ENTERTAINMENT,
        "HOTSTAR" to TransactionCategory.ENTERTAINMENT,
        "PRIME VIDEO" to TransactionCategory.ENTERTAINMENT,
        "PVR" to TransactionCategory.ENTERTAINMENT,
        "INOX" to TransactionCategory.ENTERTAINMENT,
        "STEAM" to TransactionCategory.ENTERTAINMENT,
        "APOLLO" to TransactionCategory.HEALTH,
        "TATA 1MG" to TransactionCategory.HEALTH,
        "PHARMEASY" to TransactionCategory.HEALTH,
        "NETMEDS" to TransactionCategory.HEALTH,
        "PRACTO" to TransactionCategory.HEALTH,
        "AIRTEL" to TransactionCategory.BILLS,
        "JIO" to TransactionCategory.BILLS,
        "VODAFONE" to TransactionCategory.BILLS,
        "VI" to TransactionCategory.BILLS,
        "TATA PLAY" to TransactionCategory.BILLS,
        "GOOGLE" to TransactionCategory.BILLS,
        "PAYTM" to TransactionCategory.BILLS,
        "PHONEPE" to TransactionCategory.BILLS
    )

    private val keywordAnchors = mapOf(
        listOf("CAFE", "RESTAURANT", "DINER", "KITCHEN", "FOOD", "BAKERY", "PIZZA", "BURGER", "SWEETS", "DHABA") to TransactionCategory.FOOD,
        listOf("STORE", "MARKET", "MART", "SHOP", "MALL", "FASHION", "CLOTHING", "GROCERY", "RETAIL", "SUPERMARKET") to TransactionCategory.SHOPPING,
        listOf("CAB", "TAXI", "METRO", "TRAIN", "FLIGHT", "AIRLINE", "PARKING", "FUEL", "PETROL", "DIESEL", "AUTO", "TOLL") to TransactionCategory.TRANSPORT,
        listOf("CINEMA", "MOVIES", "THEATRE", "GAME", "GAMING", "CLUB", "OTT", "MUSIC") to TransactionCategory.ENTERTAINMENT,
        listOf("MEDICAL", "HEALTH", "CLINIC", "DENTAL", "CARE", "DRUG", "HOSPITAL", "LAB", "DIAGNOSTIC") to TransactionCategory.HEALTH,
        listOf("ELECTRIC", "WATER", "GAS", "TELECOM", "MOBILE", "INTERNET", "RECHARGE", "BILL", "UTILITY", "BROADBAND", "DTH") to TransactionCategory.BILLS,
        listOf("SALARY", "CASHBACK", "REFUND", "INTEREST", "DIVIDEND") to TransactionCategory.INCOME
    )

    fun categorize(merchantName: String): TransactionCategory {
        val normalized = merchantName.uppercase().trim()

        brandMappings[normalized]?.let { return it }

        for ((keywords, category) in keywordAnchors) {
            if (keywords.any { normalized.contains(it) }) return category
        }

        return TransactionCategory.OTHERS
    }
}
