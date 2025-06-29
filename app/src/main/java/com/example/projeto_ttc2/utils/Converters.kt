package com.example.projeto_ttc2.utils

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun instantToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    @TypeConverter
    fun fromZoneOffsetString(value: String?): ZoneOffset? {
        return value?.let { ZoneOffset.of(it) }
    }

    @TypeConverter
    fun zoneOffsetToString(zoneOffset: ZoneOffset?): String? {
        return zoneOffset?.id
    }

    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun dateToEpochDay(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }
}
