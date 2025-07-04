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
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilli()
    }

    @TypeConverter
    fun fromLocalDateString(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun localDateToString(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromZoneOffsetString(value: String?): ZoneOffset? {
        return value?.let { ZoneOffset.of(it) }
    }

    @TypeConverter
    fun zoneOffsetToString(zoneOffset: ZoneOffset?): String? {
        return zoneOffset?.id
    }
}