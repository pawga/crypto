package com.pawga.usecases.crypto

import com.pawga.cryptosigner.CryptoSignerRsa
import jakarta.inject.Singleton
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.PrivateKey
import java.security.PublicKey

/**
 * Created by pawga777 on 18.03.2024 21:02
 */
@Singleton
class CryptoService {

    private val cryptoSigner = CryptoSignerRsa()

    fun generateKeyPair() = cryptoSigner.generateKeyPair()

    fun getPublicKey(): PublicKey = cryptoSigner.getPublicKey()

    fun getPrivateKey(): PrivateKey = cryptoSigner.getPrivateKey()

    fun exportKeyPair(fileKeyPem: String, filePublicPem: String) =
        cryptoSigner.exportKeyPair(fileKeyPem, filePublicPem)

    fun exportKeyPair(fosKeyPem: FileOutputStream, fosPublicPem: FileOutputStream) =
        cryptoSigner.exportKeyPair(fosKeyPem, fosPublicPem)

    fun exportPublicKey(fosPublicPem: FileOutputStream) {
        cryptoSigner.exportPublicKey(fosPublicPem)
    }

    fun exportPrivateKey(fosKeyPem: FileOutputStream) {
        cryptoSigner.exportPrivateKey(fosKeyPem)
    }

    fun importKeyPair(fileKeyPem: File?, filePublicPem: File?) =
        cryptoSigner.importKeyPair(fileKeyPem, filePublicPem)

    fun importKeyPair(private: ByteArray?, public: ByteArray?) = cryptoSigner.importKeyPair(private, public)

    fun importPrivateKey(fileKeyPem: File) = cryptoSigner.importPrivateKey(fileKeyPem)

    fun importPrivateKey(private: ByteArray) = cryptoSigner.importPrivateKey(private)

    fun importPublicKey(filePublicPem: File) = cryptoSigner.importPublicKey(filePublicPem)

    fun importPublicKey(public: ByteArray) = cryptoSigner.importPublicKey(public)

    fun encrypt(fis: FileInputStream, fos: FileOutputStream) = cryptoSigner.encrypt(fis, fos)

    fun encrypt(data: ByteArray): ByteArray = cryptoSigner.encrypt(data)

    fun decrypt(fis: FileInputStream, fos: FileOutputStream) = cryptoSigner.decrypt(fis, fos)

    fun decrypt(data: ByteArray): ByteArray = cryptoSigner.decrypt(data)

    fun sign(fis: FileInputStream, fos: FileOutputStream) = cryptoSigner.sign(fis, fos)

    fun sign(data: ByteArray): ByteArray = cryptoSigner.sign(data)

    fun verify(fis: FileInputStream, sig: FileInputStream): Boolean = cryptoSigner.verify(fis, sig)

    fun verify(data: ByteArray, sig: ByteArray): Boolean = cryptoSigner.verify(data, sig)
}