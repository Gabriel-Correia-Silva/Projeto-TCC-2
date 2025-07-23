package com.example.projeto_ttc2.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projeto_ttc2.database.entities.BleAccelerometerReading

@Dao
interface BleAccelerometerReadingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: BleAccelerometerReading)

    @Query("SELECT * FROM ble_accelerometer_readings WHERE userId = :userId AND synced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedReadingsForUser(userId: String): List<BleAccelerometerReading>

    @Update
    suspend fun updateAll(readings: List<BleAccelerometerReading>)

    @Query("DELETE FROM ble_accelerometer_readings WHERE synced = 1 AND timestamp < :olderThanTimestamp")
    suspend fun deleteSyncedAndOldReadings(olderThanTimestamp: Long)
}