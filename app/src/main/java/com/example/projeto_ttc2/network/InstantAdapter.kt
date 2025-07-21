package com.example.projeto_ttc2.network

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.time.Instant
import java.time.format.DateTimeFormatter

class InstantAdapter : TypeAdapter<Instant>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Instant?) {
        if (value == null) {
            out.nullValue()
            return
        }
        out.value(DateTimeFormatter.ISO_INSTANT.format(value))
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): Instant? {
        if (`in`.peek() == com.google.gson.stream.JsonToken.NULL) {
            `in`.nextNull()
            return null
        }
        return Instant.parse(`in`.nextString())
    }
}