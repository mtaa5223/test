package support

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

data class RsaTestKeyPair(
    val privateKey: RSAPrivateKey,
    val publicKey: RSAPublicKey,
    val kid: String,
)

object RsaTestKeys {
    fun generate(kid: String = "test-kid-1", keySize: Int = 2048): RsaTestKeyPair {
        val gen = KeyPairGenerator.getInstance("RSA")
        gen.initialize(keySize)
        val pair: KeyPair = gen.generateKeyPair()
        return RsaTestKeyPair(
            privateKey = pair.private as RSAPrivateKey,
            publicKey = pair.public as RSAPublicKey,
            kid = kid,
        )
    }
}
