package com.pawga.domain.service

import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.SecretKey

/**
 * Created by pawga777 on 25.03.2024
 */
interface CryptoSymmetricSigner {
    fun generateKey(size: Int, mode: ModeIv = ModeIv.SIMPLE): SecretKey
    fun getKey(): SecretKey?
    fun exportKey(fos: FileOutputStream)
    fun exportKeyToByteArray(): ByteArray
    fun importKey(fis: FileInputStream)
    fun importIvParameterSpec(bytes: ByteArray)
    fun import(keyFis: FileInputStream, ivFis: FileInputStream)
    fun import(keyBytes: ByteArray, ivBytes: ByteArray)
    fun exportIvParameterSpec(fos: FileOutputStream)
    fun exportIvParameterSpecToByteArray(): ByteArray
    fun importIvParameterSpec(fis: FileInputStream)
    fun importKey(bytes: ByteArray)
    fun encrypt(fis: FileInputStream, fos: FileOutputStream)
    fun encrypt(data: ByteArray): ByteArray
    fun decrypt(fis: FileInputStream, fos: FileOutputStream)
    fun decrypt(data: ByteArray): ByteArray
}

enum class ModeIv {
    SIMPLE, RIGHTLY
}