package com.masum.cipher.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.masum.cipher.core.data.local.entity.CustomCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomCategoryDao {

    @Query("SELECT * FROM custom_categories ORDER BY name ASC")
    fun getAllCustomCategoriesFlow(): Flow<List<CustomCategoryEntity>>

    @Query("SELECT * FROM custom_categories ORDER BY name ASC")
    suspend fun getAllCustomCategories(): List<CustomCategoryEntity>

    @Query("SELECT * FROM custom_categories WHERE id = :id LIMIT 1")
    suspend fun getCustomCategoryById(id: Long): CustomCategoryEntity?

    @Query("SELECT * FROM custom_categories WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getCustomCategoryByName(name: String): CustomCategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomCategory(category: CustomCategoryEntity): Long

    @Update
    suspend fun updateCustomCategory(category: CustomCategoryEntity)

    @Delete
    suspend fun deleteCustomCategory(category: CustomCategoryEntity)

    @Query("DELETE FROM custom_categories WHERE id = :id")
    suspend fun deleteCustomCategoryById(id: Long)

    @Query("DELETE FROM custom_categories")
    suspend fun deleteAllCustomCategories()
}
