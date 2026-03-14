package com.example.cipherspend.core.domain

import com.example.cipherspend.core.domain.model.TransactionCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategorizerEngine @Inject constructor() {

    private val exactMerchantMatches = mapOf(
        "Amazon" to TransactionCategory.SHOPPING,
        "Flipkart" to TransactionCategory.SHOPPING,
        "Myntra" to TransactionCategory.SHOPPING,
        "Ajio" to TransactionCategory.SHOPPING,
        "Meesho" to TransactionCategory.SHOPPING,
        "Nykaa" to TransactionCategory.SHOPPING,
        "Reliance Digital" to TransactionCategory.SHOPPING,
        "Croma" to TransactionCategory.SHOPPING,
        "Blinkit" to TransactionCategory.SHOPPING,
        "BigBasket" to TransactionCategory.SHOPPING,
        "Zepto" to TransactionCategory.SHOPPING,
        "Instamart" to TransactionCategory.SHOPPING,
        "JioMart" to TransactionCategory.SHOPPING,
        "Zomato" to TransactionCategory.FOOD,
        "Swiggy" to TransactionCategory.FOOD,
        "Eatfit" to TransactionCategory.FOOD,
        "Dominos" to TransactionCategory.FOOD,
        "KFC" to TransactionCategory.FOOD,
        "Pizza Hut" to TransactionCategory.FOOD,
        "Starbucks" to TransactionCategory.FOOD,
        "McDonalds" to TransactionCategory.FOOD,
        "Burger King" to TransactionCategory.FOOD,
        "Uber" to TransactionCategory.TRANSPORT,
        "Ola" to TransactionCategory.TRANSPORT,
        "Rapido" to TransactionCategory.TRANSPORT,
        "Indigo" to TransactionCategory.TRANSPORT,
        "Air India" to TransactionCategory.TRANSPORT,
        "Spicejet" to TransactionCategory.TRANSPORT,
        "IRCTC" to TransactionCategory.TRANSPORT,
        "Redbus" to TransactionCategory.TRANSPORT,
        "MakeMyTrip" to TransactionCategory.TRANSPORT,
        "Goibibo" to TransactionCategory.TRANSPORT,
        "BookMyShow" to TransactionCategory.ENTERTAINMENT,
        "Netflix" to TransactionCategory.ENTERTAINMENT,
        "Spotify" to TransactionCategory.ENTERTAINMENT,
        "Hotstar" to TransactionCategory.ENTERTAINMENT,
        "Prime Video" to TransactionCategory.ENTERTAINMENT,
        "PVR" to TransactionCategory.ENTERTAINMENT,
        "INOX" to TransactionCategory.ENTERTAINMENT,
        "Steam" to TransactionCategory.ENTERTAINMENT,
        "Apollo" to TransactionCategory.HEALTH,
        "Tata 1mg" to TransactionCategory.HEALTH,
        "Pharmeasy" to TransactionCategory.HEALTH,
        "Netmeds" to TransactionCategory.HEALTH,
        "Practo" to TransactionCategory.HEALTH,
        "Airtel" to TransactionCategory.BILLS,
        "Jio" to TransactionCategory.BILLS,
        "Vodafone Idea" to TransactionCategory.BILLS,
        "VI" to TransactionCategory.BILLS,
        "Tata Play" to TransactionCategory.BILLS,
        "Google" to TransactionCategory.BILLS,
        "Paytm" to TransactionCategory.BILLS,
        "PhonePe" to TransactionCategory.BILLS
    )

    private val categoryKeywordAnchors = mapOf(
        listOf("CAFE", "RESTAURANT", "DINER", "KITCHEN", "FOOD", "BAKERY", "PIZZA", "BURGER", "SWEETS", "DHABA") to TransactionCategory.FOOD,
        listOf("STORE", "MARKET", "MART", "SHOP", "MALL", "FASHION", "CLOTHING", "GROCERY", "RETAIL", "SUPERMARKET") to TransactionCategory.SHOPPING,
        listOf("CAB", "TAXI", "METRO", "TRAIN", "FLIGHT", "AIRLINE", "PARKING", "FUEL", "PETROL", "DIESEL", "AUTO", "TOLL") to TransactionCategory.TRANSPORT,
        listOf("CINEMA", "MOVIES", "THEATRE", "GAME", "GAMING", "CLUB", "OTT", "MUSIC") to TransactionCategory.ENTERTAINMENT,
        listOf("MEDICAL", "HEALTH", "CLINIC", "DENTAL", "CARE", "DRUG", "HOSPITAL", "LAB", "DIAGNOSTIC") to TransactionCategory.HEALTH,
        listOf("ELECTRIC", "WATER", "GAS", "TELECOM", "MOBILE", "INTERNET", "RECHARGE", "BILL", "UTILITY", "BROADBAND", "DTH") to TransactionCategory.BILLS,
        listOf("SALARY", "CASHBACK", "REFUND", "INTEREST", "DIVIDEND") to TransactionCategory.INCOME
    )

    fun categorize(merchantName: String): TransactionCategory {
        val normalizedName = merchantName.uppercase()

        for ((brand, category) in exactMerchantMatches) {
            if (normalizedName == brand.uppercase()) {
                return category
            }
        }

        for ((anchors, category) in categoryKeywordAnchors) {
            if (anchors.any { normalizedName.contains(it) }) {
                return category
            }
        }

        return TransactionCategory.OTHERS
    }
}
