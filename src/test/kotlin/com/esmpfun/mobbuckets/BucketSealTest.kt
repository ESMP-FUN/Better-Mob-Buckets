package com.esmpfun.mobbuckets

import com.esmpfun.mobbuckets.item.BucketSeal
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BucketSealTest {

    private val keyFile = Files.createTempDirectory("mobbuckets").resolve("bucket-key.dat").toFile()
    private val data = byteArrayOf(1, 2, 3, 4)

    @Test
    fun `a signed bucket verifies, also after a restart`() {
        val seal = BucketSeal(keyFile).sign("cow", data)
        assertTrue(BucketSeal(keyFile).verify("cow", data, seal))
    }

    @Test
    fun `edited buckets are refused`() {
        val sealer = BucketSeal(keyFile)
        val seal = sealer.sign("cow", data)
        assertFalse(sealer.verify("wither", data, seal))
        assertFalse(sealer.verify("cow", byteArrayOf(1, 2, 3, 5), seal))
        assertFalse(sealer.verify("cow", data, null))
    }

    @Test
    fun `another server's key is refused`() {
        val seal = BucketSeal(keyFile).sign("cow", data)
        val otherKey = Files.createTempDirectory("mobbuckets").resolve("bucket-key.dat").toFile()
        assertFalse(BucketSeal(otherKey).verify("cow", data, seal))
    }
}
