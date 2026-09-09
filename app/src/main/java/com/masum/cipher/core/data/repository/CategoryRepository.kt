package com.masum.cipher.core.data.repository

import com.masum.cipher.core.data.local.dao.CategoryRuleDao
import com.masum.cipher.core.data.local.dao.CustomCategoryDao
import com.masum.cipher.core.data.local.dao.SubscriptionDao
import com.masum.cipher.core.data.local.dao.TransactionDao
import com.masum.cipher.core.data.local.entity.CustomCategoryEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val customCategoryDao: CustomCategoryDao,
    private val transactionDao: TransactionDao,
    private val categoryRuleDao: CategoryRuleDao,
    private val subscriptionDao: SubscriptionDao,
    private val userPreferences: UserPreferences
) {
    fun getAllCustomCategoriesFlow(): Flow<List<CustomCategoryEntity>> =
        customCategoryDao.getAllCustomCategoriesFlow()

    suspend fun getAllCustomCategories(): List<CustomCategoryEntity> =
        customCategoryDao.getAllCustomCategories()

    suspend fun addCustomCategory(name: String, iconName: String, colorHex: Long): Long {
        val entity = CustomCategoryEntity(
            name = name.trim(),
            iconName = iconName,
            colorHex = colorHex
        )
        return customCategoryDao.insertCustomCategory(entity)
    }

    suspend fun updateCustomCategory(id: Long, oldName: String, newName: String, iconName: String, colorHex: Long) {
        val trimmedNew = newName.trim()
        val entity = CustomCategoryEntity(
            id = id,
            name = trimmedNew,
            iconName = iconName,
            colorHex = colorHex
        )
        customCategoryDao.updateCustomCategory(entity)
        if (oldName != trimmedNew) {
            transactionDao.reassignCategory(oldName, trimmedNew)
            categoryRuleDao.reassignCategoryRules(oldName, trimmedNew)
            subscriptionDao.reassignCategory(oldName, trimmedNew)
        }
    }

    suspend fun deleteCustomCategory(category: CustomCategoryEntity, fallbackCategory: String = "OTHERS") {
        customCategoryDao.deleteCustomCategory(category)
        transactionDao.reassignCategory(category.name, fallbackCategory)
        categoryRuleDao.deleteRulesForCategory(category.name)
        subscriptionDao.reassignCategory(category.name, fallbackCategory)
        userPreferences.setCategoryBudget(category.name, 0.0)
    }
}
