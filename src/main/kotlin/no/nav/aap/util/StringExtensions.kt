package no.nav.aap.util

import java.util.Base64.getEncoder
import java.util.Locale.getDefault
import kotlin.text.Charsets.UTF_8

object StringExtensions {

    fun String.decap() = replaceFirstChar { it.lowercase(getDefault()) }

    fun String.partialMask(mask : Char = '*') : String {
        val start = length.div(2)
        return replaceRange(start + 1, length, mask.toString().repeat(length - start - 1))
    }

    fun String.asBearer() = "Bearer $this"

    fun ByteArray.encode() = getEncoder().encodeToString(this)

    fun String.encode() = toByteArray(UTF_8).encode()

}