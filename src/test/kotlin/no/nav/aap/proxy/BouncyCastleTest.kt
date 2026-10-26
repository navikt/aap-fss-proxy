package no.nav.aap.proxy

import org.assertj.core.api.Assertions.assertThat
import org.bouncycastle.crypto.engines.GOST3412_2015Engine
import org.bouncycastle.crypto.modes.G3413CTRBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import org.junit.jupiter.api.Test

class BouncyCastleTest {

    @Test
    fun `GOST CTR does not reuse keystream after 256 blocks`() {
        val cipher = G3413CTRBlockCipher(GOST3412_2015Engine())
        cipher.init(true, ParametersWithIV(KeyParameter(ByteArray(32)), ByteArray(8)))
        val plaintext = ByteArray(cipher.blockSize * 257)
        val ciphertext = ByteArray(plaintext.size)

        for (offset in plaintext.indices step cipher.blockSize) {
            cipher.processBlock(plaintext, offset, ciphertext, offset)
        }

        assertThat(ciphertext.copyOfRange(0, cipher.blockSize))
            .isNotEqualTo(ciphertext.copyOfRange(cipher.blockSize * 256, ciphertext.size))
    }
}
