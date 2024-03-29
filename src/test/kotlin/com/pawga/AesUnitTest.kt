package com.pawga

import com.pawga.cryptosigner.CryptoSignerAes
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Paths

/**
 * Created by pawga777 on 26.03.2024
 */
class AesUnitTest : WithAssertions {

    @Test
    fun givenString_whenEncrypt_thenSuccess() {

        // given
        val cryptoSignerAes = CryptoSignerAes()
        cryptoSignerAes.generateKey(256)
        val input = "baeldung"

        // when
        val cipher = cryptoSignerAes.encrypt(input.encodeToByteArray())
        val plainText = String(cryptoSignerAes.decrypt(cipher))

        // then
        Assertions.assertEquals(input, plainText)
    }

    @Test
    fun givenFile_whenIV_thenSuccess() {
        // given
        val cryptoSignerAes = CryptoSignerAes()
        cryptoSignerAes.generateKey(256)
        val ivFile = File("IvParameterSpec")

        val ivBytes = cryptoSignerAes.exportIvParameterSpecToByteArray()

        // when
        cryptoSignerAes.exportIvParameterSpec(FileOutputStream(ivFile))
        cryptoSignerAes.importIvParameterSpec(FileInputStream(ivFile))
        val ivBytes2 = cryptoSignerAes.exportIvParameterSpecToByteArray()

        // then
        assert(ivBytes.contentEquals(ivBytes2))

        ivFile.deleteOnExit()
    }

    @Test
    fun givenFile_whenEncrypt_thenSuccess() {
        // given
        val cryptoSignerAes = CryptoSignerAes()
        cryptoSignerAes.generateKey(256)
        val inputFile = Paths.get("src/test/resources/test.txt").toFile()
        val encryptedFile = File("baeldung.encrypted")
        val decryptedFile = File("document.decrypted")
        val ivFile = File("iv.bin")
        val keyFile = File("symmetric-key.bin")

        // when
        cryptoSignerAes.exportIvParameterSpec(FileOutputStream(ivFile))
        cryptoSignerAes.exportKey(FileOutputStream(keyFile))

        cryptoSignerAes.importKey(FileInputStream(keyFile))
        cryptoSignerAes.importIvParameterSpec(FileInputStream(ivFile))

        cryptoSignerAes.encrypt(FileInputStream(inputFile), FileOutputStream(encryptedFile))
        cryptoSignerAes.decrypt(FileInputStream(encryptedFile), FileOutputStream(decryptedFile))

        // then
        assertThat(inputFile).hasSameTextualContentAs(decryptedFile)

        encryptedFile.deleteOnExit()
        decryptedFile.deleteOnExit()
        ivFile.deleteOnExit()
        keyFile.deleteOnExit()
    }

    @Test
    fun givenImportFile_whenEncrypt_thenSuccess() {
        // given
        val cryptoSignerAes = CryptoSignerAes()
        cryptoSignerAes.generateKey(256)
        val inputFile = Paths.get("src/test/resources/test.txt").toFile()
        val encryptedFile = File("baeldung.encrypted")
        val decryptedFile = File("document.decrypted")
        val ivFile = File("src/test/resources/iv.bin")
        val keyFile = File("src/test/resources/symmetric-key.bin")

        // when
        cryptoSignerAes.importKey(FileInputStream(keyFile))
        cryptoSignerAes.importIvParameterSpec(FileInputStream(ivFile))

        cryptoSignerAes.encrypt(FileInputStream(inputFile), FileOutputStream(encryptedFile))
        cryptoSignerAes.decrypt(FileInputStream(encryptedFile), FileOutputStream(decryptedFile))

        // then
        assertThat(inputFile).hasSameTextualContentAs(decryptedFile)

        encryptedFile.deleteOnExit()
        decryptedFile.deleteOnExit()
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }
}