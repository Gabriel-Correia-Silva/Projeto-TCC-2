package com.example.projeto_ttc2.database.dao

import androidx.room.*
import com.example.projeto_ttc2.database.entities.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: User)

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: String): User?

}