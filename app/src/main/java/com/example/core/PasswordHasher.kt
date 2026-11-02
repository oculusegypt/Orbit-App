package com.example.core

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Real password hashing for local account storage (PBKDF2-HMAC-SHA256).
 *
 * Stored format: `pbkdf2$iterations$saltHex$hashHex`. Verification uses a
 * constant-time digest comparison. Legacy plain entries written by older
 * builds are transparently upgraded on successful login by [needsRehash].
 */
object PasswordHasher {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16
    private const val PREFIX = "pbkdf2"

    private val random = SecureRandom()

    /** Produces a salted PBKDF2 hash suitable for persistent storage. */
    fun hash(password: String): String {
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val derived = derive(password.toCharArray(), salt, ITERATIONS)
        return "$PREFIX$$ITERATIONS$${salt.toHex()}$${derived.toHex()}"
    }

    /** Constant-time verification of [password] against a stored [hashSpec]. */
    fun verify(password: String, hashSpec: String): Boolean {
        val parts = hashSpec.split("$")
        if (parts.size != 4 || parts[0] != PREFIX) {
            // Legacy plaintext entry from an older build: compare directly so
            // existing seeded accounts keep working until they are rehashed.
            return constantTimeEquals(password, hashSpec)
        }
        return try {
            val iterations = parts[1].toInt()
            val salt = parts[2].hexToByteArray()
            val expected = parts[3].hexToByteArray()
            val actual = derive(password.toCharArray(), salt, iterations)
            MessageDigest.isEqual(expected, actual)
        } catch (_: Throwable) {
            false
        }
    }

    /** True when the stored spec is not the current PBKDF2 format and should be rewritten. */
    fun needsRehash(hashSpec: String): Boolean = !hashSpec.startsWith("$PREFIX$")

    private fun derive(password: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val spec = javax.crypto.spec.PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS)
        val factory = javax.crypto.SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].code xor b[i].code)
        return diff == 0
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToByteArray(): ByteArray {
        check(length % 2 == 0) { "Invalid hex length" }
        return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
