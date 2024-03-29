package com.pawga.domain.service

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.PrivateKey
import java.security.PublicKey

/**
 * Created by pawga777 on 13.03.2024 23:32
 */
interface CryptoAsymmetricSigner {
    fun generateKeyPair()
    fun getPublicKey(): PublicKey
    fun getPrivateKey(): PrivateKey
    fun exportKeyPair(fileKeyPem: String, filePublicPem: String)
    fun exportKeyPair(fosKeyPem: FileOutputStream, fosPublicPem: FileOutputStream)
    fun exportPublicKey(fosPublicPem: FileOutputStream)
    fun exportPrivateKey(fosKeyPem: FileOutputStream)
    fun importKeyPair(fileKeyPem: File?, filePublicPem: File?)
    fun importKeyPair(private: ByteArray?, public: ByteArray?)
    fun importPrivateKey(fileKeyPem: File)
    fun importPrivateKey(private: ByteArray)
    fun importPublicKey(filePublicPem: File)
    fun importPublicKey(public: ByteArray)
    fun encrypt(fis: FileInputStream, fos: FileOutputStream)
    fun encrypt(data: ByteArray): ByteArray
    fun decrypt(fis: FileInputStream, fos: FileOutputStream)
    fun decrypt(data: ByteArray): ByteArray
    fun sign(fis: FileInputStream, fos: FileOutputStream)
    fun sign(data: ByteArray): ByteArray
    fun verify(fis: FileInputStream, sig: FileInputStream): Boolean
    fun verify(data: ByteArray, sig: ByteArray): Boolean
}