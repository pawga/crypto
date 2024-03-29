package com.pawga.cryptosigner

import com.pawga.domain.service.CryptoSymmetricSigner
import com.pawga.domain.service.ModeIv
import com.pawga.exceptions.CryptoSignerException
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


/**
 * Created by pawga777 on 26.03.2024
 */
@Singleton
class CryptoSignerAes : CryptoSymmetricSigner {

    private var secretKey: SecretKey? = null
    private var ivParameterSpec: IvParameterSpec? = null

    override fun generateKey(size: Int, mode: ModeIv): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(size)
        secretKey = keyGenerator.generateKey()
        ivParameterSpec = generateIv(mode)
        return secretKey!!
    }

    override fun getKey(): SecretKey? = secretKey

    override fun exportKey(fos: FileOutputStream) {
        if (secretKey == null) {
            throw CryptoSignerException("The secretKey is uninitialized!")
        }
        writeSecretKeyKey(secretKey!!, fos)
    }

    override fun exportKeyToByteArray(): ByteArray {
        if (secretKey == null) {
            throw CryptoSignerException("The secretKey is uninitialized!")
        }
        return secretKey!!.encoded
    }

    override fun importKey(fis: FileInputStream) {
        secretKey = readSecretKey(fis)
    }

    override fun importKey(bytes: ByteArray) {
        secretKey = convertByteArrayToSecretKey(bytes)
    }

    override fun importIvParameterSpec(bytes: ByteArray) {
        ivParameterSpec = IvParameterSpec(bytes)
    }

    override fun importIvParameterSpec(fis: FileInputStream) {
        val bytes = fis.readAllBytes()
        ivParameterSpec = IvParameterSpec(bytes)
    }

    override fun import(keyFis: FileInputStream, ivFis: FileInputStream) {
        secretKey = readSecretKey(keyFis)
        importIvParameterSpec(ivFis)
    }

    override fun import(keyBytes: ByteArray, ivBytes: ByteArray) {
        secretKey = convertByteArrayToSecretKey(keyBytes)
        ivParameterSpec = IvParameterSpec(ivBytes)
    }

    override fun exportIvParameterSpec(fos: FileOutputStream) {
        if (ivParameterSpec == null) {
            throw CryptoSignerException("The ivParameterSpec is uninitialized!")
        }
        fos.write(ivParameterSpec!!.iv)
    }

    override fun exportIvParameterSpecToByteArray(): ByteArray {
        if (ivParameterSpec == null) {
            throw CryptoSignerException("The ivParameterSpec is uninitialized!")
        }
        return ivParameterSpec!!.iv
    }

    override fun encrypt(data: ByteArray): ByteArray {
        if (secretKey == null) {
            throw CryptoSignerException("The secretKey is uninitialized!")
        }
        if (ivParameterSpec == null) {
            throw CryptoSignerException("The ivParameterSpec is uninitialized!")
        }
        return encrypt(data, secretKey!!, ivParameterSpec!!)
    }

    override fun encrypt(fis: FileInputStream, fos: FileOutputStream) {
        if (secretKey == null) {
            throw CryptoSignerException("The secretKey is uninitialized!")
        }
        if (ivParameterSpec == null) {
            throw CryptoSignerException("The ivParameterSpec is uninitialized!")
        }
        cryptoFile(secretKey!!, ivParameterSpec!!, fis, fos, Cipher.ENCRYPT_MODE)
    }

    override fun decrypt(data: ByteArray): ByteArray {
        if (secretKey == null) {
            throw CryptoSignerException("The secretKey is uninitialized!")
        }
        if (ivParameterSpec == null) {
            throw CryptoSignerException("The ivParameterSpec is uninitialized!")
        }
        return decrypt(data, secretKey!!, ivParameterSpec!!)
    }

    override fun decrypt(fis: FileInputStream, fos: FileOutputStream) {
        if (secretKey == null) {
            throw CryptoSignerException("The secretKey is uninitialized!")
        }
        if (ivParameterSpec == null) {
            throw CryptoSignerException("The ivParameterSpec is uninitialized!")
        }
        cryptoFile(secretKey!!, ivParameterSpec!!, fis, fos, Cipher.DECRYPT_MODE)
    }

    // private

    private fun convertByteArrayToSecretKey(decodedKey: ByteArray): SecretKey {
        val originalKey: SecretKey = SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
        return originalKey
    }

    private fun readSecretKey(fis: FileInputStream): SecretKey {
        val decodedKey = fis.readAllBytes()
        return convertByteArrayToSecretKey(decodedKey)
    }

    private fun writeSecretKeyKey(secretKey: SecretKey, fos: FileOutputStream) {
        fos.write(secretKey.encoded)
    }

    private fun generateIv(mode: ModeIv = ModeIv.SIMPLE): IvParameterSpec {
        val iv: ByteArray
        if (mode == ModeIv.RIGHTLY) {
            iv = ByteArray(16)
            SecureRandom().nextBytes(iv)
        } else {
            iv = IV
        }
        return IvParameterSpec(iv)
    }

    private fun encrypt(
        input: ByteArray,
        key: SecretKey,
        iv: IvParameterSpec,
        algorithm: String = ALGORITHM
    ): ByteArray {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        return cipher.doFinal(input)
    }

    private fun decrypt(
        bytes: ByteArray,
        key: SecretKey,
        iv: IvParameterSpec,
        algorithm: String = ALGORITHM,
    ): ByteArray {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, key, iv)
        return cipher.doFinal(bytes)
    }

    private fun cryptoFile(
        key: SecretKey,
        iv: IvParameterSpec,
        inputStream: FileInputStream,
        outputStream: FileOutputStream,
        mode: Int,
        algorithm: String = ALGORITHM,
    ) {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(mode, key, iv)

        val buffer = ByteArray(256)
        var bytesRead: Int
        while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
            val output = cipher.update(buffer, 0, bytesRead)
            if (output != null) {
                outputStream.write(output)
            }
        }
        val outputBytes = cipher.doFinal()
        if (outputBytes != null) {
            outputStream.write(outputBytes)
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    private val ALGORITHM = "AES/CBC/PKCS5Padding"
    private val IV = byteArrayOf( // just a magic number
        0x11, 0x2E, 0x38, 0x4F, 0x5D, 0x63,  0x1F, 0x41,
        0x22, 0x3F, 0x49, 0x5D, 0x67, 0x36,  0x77, 0x4F)

}