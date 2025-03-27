package com.lex.qr.utils

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        val formatted = value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        encoder.encodeString(formatted)
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val string = decoder.decodeString()
        return LocalDateTime.parse(string, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}

object LocalDateTimeKotlinSerializer : KSerializer<kotlinx.datetime.LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: kotlinx.datetime.LocalDateTime) {
        encoder.encodeString(value.toJavaLocalDateTime().format(formatter))
    }

    override fun deserialize(decoder: Decoder): kotlinx.datetime.LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), formatter).toKotlinLocalDateTime()
    }
}
fun formatDateTime(dateTime: LocalDateTime): String {
    val dateTimeTZ = dateTime.plusHours(5)
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", Locale("ru"))
    return dateTimeTZ.format(formatter)
}
fun LocalDateTime.toKotlinLocalDateTime(): kotlinx.datetime.LocalDateTime =
    this.toInstant(java.time.ZoneOffset.UTC).toKotlinInstant().toLocalDateTime(TimeZone.UTC)

fun kotlinx.datetime.LocalDateTime.toJavaLocalDateTime(): LocalDateTime =
    LocalDateTime.of(this.date.year, this.date.monthNumber, this.date.dayOfMonth, this.hour, this.minute, this.second, this.nanosecond)