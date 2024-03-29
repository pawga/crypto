package com.pawga.cryptosigner

import com.pawga.domain.service.CryptoAsymmetricSigner
import com.pawga.exceptions.CryptoSignerException
import jakarta.inject.Singleton
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemReader
import org.bouncycastle.util.io.pem.PemWriter
import java.io.*
import java.security.*
import java.security.spec.EncodedKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

/**
 * Created by pawga777 on 13.03.2024 23:57
 */

@Singleton
class CryptoSignerRsa : CryptoAsymmetricSigner {

    private var publicKey: PublicKey? = null
    private var privateKey: PrivateKey? = null

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    override fun generateKeyPair() {
        val generator = KeyPairGenerator.getInstance(DEFAULT_ALGORITHM)
        generator.initialize(KEY_SIZE)
        val pair = generator.generateKeyPair()
        publicKey = pair.public
        privateKey = pair.private
    }

    override fun getPublicKey(): PublicKey {
        if (publicKey == null) {
            throw CryptoSignerException("The publicKey is uninitialized!")
        }
        return publicKey!!
    }

    override fun getPrivateKey(): PrivateKey {
        if (privateKey == null) {
            throw CryptoSignerException("The privateKey is uninitialized!")
        }
        return privateKey!!
    }

    override fun exportKeyPair(fileKeyPem: String, filePublicPem: String) {
        FileOutputStream(fileKeyPem).use { fos ->
            FileOutputStream(filePublicPem).use { fosPublic ->
                exportKeyPair(fos, fosPublic)
            }
        }
    }

    override fun exportKeyPair(fosKeyPem: FileOutputStream, fosPublicPem: FileOutputStream) {
        if (privateKey == null && publicKey == null) {
            throw CryptoSignerException("The key pair is uninitialized!")
        }
        writePKCS8PrivateKey(privateKey!!, fosKeyPem)
        writePublicKey(publicKey!!, fosPublicPem)
    }

    override fun exportPublicKey(fosPublicPem: FileOutputStream) {
        if (publicKey == null) {
            throw CryptoSignerException("The publicKey is uninitialized!")
        }
        writePublicKey(publicKey!!, fosPublicPem)
    }

    override fun exportPrivateKey(fosKeyPem: FileOutputStream) {
        if (privateKey == null) {
            throw CryptoSignerException("The privateKey is uninitialized!")
        }
        writePKCS8PrivateKey(privateKey!!, fosKeyPem)
    }

    override fun importKeyPair(fileKeyPem: File?, filePublicPem: File?) {
        privateKey = if (fileKeyPem != null) {
            readPKCS8PrivateKey(fileKeyPem)
        } else {
            null
        }
        publicKey = if (filePublicPem != null) {
            readPublicKey(filePublicPem)
        } else {
            null
        }
    }

    override fun importKeyPair(private: ByteArray?, public: ByteArray?) {
        privateKey = readPKCS8PrivateKey(private)
        publicKey = readPublicKey(public)
    }

    override fun importPrivateKey(fileKeyPem: File) {
        privateKey = readPKCS8PrivateKey(fileKeyPem)
        publicKey = null
    }

    override fun importPrivateKey(private: ByteArray) {
        privateKey = readPKCS8PrivateKey(private)
        publicKey = null
    }

    override fun importPublicKey(filePublicPem: File) {
        privateKey = null
        publicKey = readPublicKey(filePublicPem)
    }

    override fun importPublicKey(public: ByteArray) {
        privateKey = null
        publicKey = readPublicKey(public)
    }

    override fun encrypt(fis: FileInputStream, fos: FileOutputStream) {
        if (publicKey == null) {
            throw CryptoSignerException("The publicKey is uninitialized!")
        }
        val data = fis.readAllBytes()
        val encData = encrypt(data)
        fos.write(encData)
    }

    override fun encrypt(data: ByteArray): ByteArray {
        if (publicKey == null) {
            throw CryptoSignerException("The publicKey is uninitialized!")
        }
        val encryptCipher = Cipher.getInstance(DEFAULT_ALGORITHM)
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return encryptCipher.doFinal(data)
    }

    override fun decrypt(fis: FileInputStream, fos: FileOutputStream) {
        if (privateKey == null) {
            throw CryptoSignerException("The privateKey is uninitialized!")
        }
        val data = fis.readAllBytes()
        val decData = decrypt(data)
        fos.write(decData)
    }

    override fun decrypt(data: ByteArray): ByteArray {
        if (privateKey == null) {
            throw CryptoSignerException("The privateKey is uninitialized!")
        }
        val decryptCipher = Cipher.getInstance(DEFAULT_ALGORITHM)
        decryptCipher.init(Cipher.DECRYPT_MODE, privateKey)
        return decryptCipher.doFinal(data)
    }

    override fun sign(fis: FileInputStream, fos: FileOutputStream) {
        if (privateKey == null) {
            throw CryptoSignerException("The privateKey is uninitialized!")
        }
        val data = fis.readAllBytes()
        val signData = sign(data)
        fos.write(signData)
    }

    override fun sign(data: ByteArray): ByteArray {
        if (privateKey == null) {
            throw CryptoSignerException("The privateKey is uninitialized!")
        }
        val signature = Signature.getInstance(DEFAULT_SIGNATURE_ALGORITHM)
        signature.initSign(privateKey)
        signature.update(data)
        return signature.sign()
    }

    override fun verify(fis: FileInputStream, sig: FileInputStream): Boolean {
        if (publicKey == null) {
            throw CryptoSignerException("The publicKey is uninitialized!")
        }
        val data = fis.readAllBytes()
        val dataSig = sig.readAllBytes()
        return verify(data, dataSig)
    }

    override fun verify(data: ByteArray, sig: ByteArray): Boolean {
        if (publicKey == null) {
            throw CryptoSignerException("The publicKey is uninitialized!")
        }
        val signature = Signature.getInstance(DEFAULT_SIGNATURE_ALGORITHM)
        signature.initVerify(publicKey)
        signature.update(data)
        return signature.verify(sig)
    }

    //private

    private fun writePublicKey(publicKey: PublicKey, fos: FileOutputStream) {
        val pemObject = PemObject(PEM_SIGNATURE_PUBLIC_KEY, publicKey.encoded)
        PemWriter(OutputStreamWriter(fos)).use { pemWriter ->
            pemWriter.writeObject(pemObject)
        }
    }

    private fun writePKCS8PrivateKey(privateKey: PrivateKey, fos: FileOutputStream) {
        val pemObject = PemObject(PEM_SIGNATURE_PRIVATE_KEY, privateKey.encoded)
        PemWriter(OutputStreamWriter(fos)).use { pemWriter ->
            pemWriter.writeObject(pemObject)
        }
    }

    private fun readPublicKey(file: File?): PublicKey? {
        if (file == null) return null
        FileReader(file).use { keyReader ->
            PemReader(keyReader).use { pemReader ->
                val pemObject = pemReader.readPemObject()
                val content = pemObject.content
                return readPublicKey(content)
            }
        }
    }

    private fun readPublicKey(data: ByteArray?): PublicKey? {
        if (data == null) return null
        val factory = KeyFactory.getInstance(DEFAULT_ALGORITHM)
        val publicKeySpec: EncodedKeySpec = X509EncodedKeySpec(data)
        return factory.generatePublic(publicKeySpec)
    }

    private fun readPKCS8PrivateKey(file: File?): PrivateKey? {
        if (file == null) return null
        FileReader(file).use { keyReader ->
            PemReader(keyReader).use { pemReader ->
                val pemObject = pemReader.readPemObject()
                val content = pemObject.content
                return readPKCS8PrivateKey(content)
            }
        }
    }

    private fun readPKCS8PrivateKey(data: ByteArray?): PrivateKey? {
        if (data == null) return null
        val factory = KeyFactory.getInstance(DEFAULT_ALGORITHM)
        val keySpec = PKCS8EncodedKeySpec(data)
        return factory.generatePrivate(keySpec)
    }

    private val DEFAULT_ALGORITHM = "RSA"
    private val DEFAULT_SIGNATURE_ALGORITHM = "SHA256withRSA"
    private val KEY_SIZE = 2048
    private val PEM_SIGNATURE_PRIVATE_KEY = "RSA PRIVATE KEY"
    private val PEM_SIGNATURE_PUBLIC_KEY = "RSA PUBLIC KEY"

}