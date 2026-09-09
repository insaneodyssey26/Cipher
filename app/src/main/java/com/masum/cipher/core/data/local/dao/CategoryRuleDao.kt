package com.masum.cipher.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.masum.cipher.core.data.local.entity.CategoryRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryRuleDao {
    @Query("SELECT * FROM category_rules ORDER BY rowid DESC")
    fun getAllRules(): Flow<List<CategoryRuleEntity>>

    @Query("SELECT customCategory FROM category_rules WHERE merchantName = :merchantName COLLATE NOCASE LIMIT 1")
    suspend fun getCategoryForMerchant(merchantName: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: CategoryRuleEntity)

    @Delete
    suspend fun deleteRule(rule: CategoryRuleEntity)

    @Query("UPDATE category_rules SET customCategory = :newCategory WHERE customCategory = :oldCategory COLLATE NOCASE")
    suspend fun reassignCategoryRules(oldCategory: String, newCategory: String)

    @Query("DELETE FROM category_rules WHERE customCategory = :category COLLATE NOCASE")
    suspend fun deleteRulesForCategory(category: String)
}
