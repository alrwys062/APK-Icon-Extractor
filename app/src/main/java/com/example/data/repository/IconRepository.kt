package com.example.data.repository

import com.example.data.dao.ExtractedIconDao
import com.example.data.entity.ExtractedIconEntity
import kotlinx.coroutines.flow.Flow

class IconRepository(private val dao: ExtractedIconDao) {
    val allIcons: Flow<List<ExtractedIconEntity>> = dao.getAllIcons()

    suspend fun insert(icon: ExtractedIconEntity): Long {
        return dao.insert(icon)
    }

    suspend fun updateFavorite(id: Long, isFavorite: Boolean) {
        dao.updateFavorite(id, isFavorite)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun getById(id: Long): ExtractedIconEntity? {
        return dao.getIconById(id)
    }
}
