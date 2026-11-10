package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.ExtractedIconEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExtractedIconDao {
    @Query("SELECT * FROM extracted_icons ORDER BY timestamp DESC")
    fun getAllIcons(): Flow<List<ExtractedIconEntity>>

    @Query("SELECT * FROM extracted_icons WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteIcons(): Flow<List<ExtractedIconEntity>>

    @Query("SELECT * FROM extracted_icons WHERE id = :id")
    suspend fun getIconById(id: Long): ExtractedIconEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(icon: ExtractedIconEntity): Long

    @Query("UPDATE extracted_icons SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM extracted_icons WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM extracted_icons")
    suspend fun deleteAll()
}
