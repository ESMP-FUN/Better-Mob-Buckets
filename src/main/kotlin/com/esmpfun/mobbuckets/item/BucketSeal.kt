package com.esmpfun.mobbuckets.item

import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Signs what a filled bucket holds with a key only this server knows. Creative-mode players can
 * edit item data freely, so without this a hand-made bucket could release any entity at all.
 */
class BucketSeal(private val keyFile: File) {

    private val key: SecretKeySpec by lazy { SecretKeySpec(loadOrCreate(), ALGORITHM) }

    fun sign(mobId: String, data: ByteArray): ByteArray {
        val mac = Mac.getInstance(ALGORITHM)
        mac.init(key)
        mac.update(mobId.toByteArray(Charsets.UTF_8))
        mac.update(0)
        return mac.doFinal(data)
    }

    fun verify(mobId: String, data: ByteArray, seal: ByteArray?): Boolean =
        seal != null && MessageDigest.isEqual(sign(mobId, data), seal)

    private fun loadOrCreate(): ByteArray {
        if (keyFile.isFile) {
            val existing = keyFile.readBytes()
            if (existing.size >= KEY_BYTES) return existing
        }
        val fresh = ByteArray(KEY_BYTES).also { SecureRandom().nextBytes(it) }
        keyFile.parentFile.mkdirs()
        keyFile.writeBytes(fresh)
        return fresh
    }

    private companion object {
        const val ALGORITHM = "HmacSHA256"
        const val KEY_BYTES = 32
    }
}
