package com.example.cipherspend.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cipherspend.core.data.local.entity.MerchantAliasEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantAliasDao {
    @Query("SELECT * FROM merchant_aliases")
    fun getAllAliases(): Flow<List<MerchantAliasEntity>>

    @Query("SELECT * FROM merchant_aliases WHERE rawName = :rawName LIMIT 1")
    suspend fun getAliasForRawName(rawName: String): MerchantAliasEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlias(alias: MerchantAliasEntity)

    @Query("DELETE FROM merchant_aliases WHERE rawName = :rawName")
    suspend fun deleteAlias(rawName: String)
}
