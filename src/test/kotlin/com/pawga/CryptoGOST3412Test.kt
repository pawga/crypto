package com.pawga

import org.bouncycastle.crypto.CryptoServicesRegistrar
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.Arrays.areEqual
import org.bouncycastle.util.Strings
import org.bouncycastle.util.encoders.Hex
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.security.InvalidAlgorithmParameterException
import java.security.Key
import java.security.Security
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.test.DefaultAsserter.fail

class CryptoGOST3412Test {

    @Test
    fun performTest() {
        Security.addProvider(BouncyCastleProvider())

        testECB(
            Hex.decode("8899aabbccddeeff0011223344556677fedcba98765432100123456789abcdef"),
            Hex.decode("1122334455667700ffeeddccbbaa9988"),
            Hex.decode("7f679d90bebc24305a468d42b9d4edcd")
        )

        testECB(
            Hex.decode("8899aabbccddeeff0011223344556677fedcba98765432100123456789abcdef"),
            Hex.decode("1122334455667700ffeeddccbbaa9988"),
            Hex.decode("7f679d90bebc24305a468d42b9d4edcd")
        )

        testCFB(
            Hex.decode("8899aabbccddeeff0011223344556677fedcba98765432100123456789abcdef"),
            Hex.decode("1234567890abcef0a1b2c3d4e5f0011223344556677889901213141516171819"),
            Hex.decode("1122334455667700ffeeddccbbaa998800112233445566778899aabbcceeff0a112233445566778899aabbcceeff0a002233445566778899aabbcceeff0a0011"),
            Hex.decode("819b19c5867e61f1cf1b16f664f66e46ed8fcb82b1110b1e7ec03bfa6611f2eabd7a32363691cbdc3bbe403bc80552d822c2cdf483981cd71d5595453d7f057d")
        )

        val inputs = arrayOf(
            Hex.decode("1122334455667700ffeeddccbbaa9988"),
            Hex.decode("00112233445566778899aabbcceeff0a"),
            Hex.decode("112233445566778899aabbcceeff0a00"),
            Hex.decode("2233445566778899aabbcceeff0a0011"),
        )

        val mac = Mac.getInstance("GOST3412MAC", "BC")

        mac.init(
            SecretKeySpec(
                Hex.decode("8899aabbccddeeff0011223344556677fedcba98765432100123456789abcdef"),
                "GOST3412MAC"
            )
        )

        for (input in inputs.indices) {
            mac.update(inputs[input])
        }

        if (!areEqual(Hex.decode("336f4d296059fbe34ddeb35b37749c67"), mac.doFinal())) {
            fail("mac test failed.")
        }

        testCTR()
    }

    private fun testECB(
        keyBytes: ByteArray,
        input: ByteArray,
        output: ByteArray
    ) {
        val cIn: CipherInputStream
        val cOut: CipherOutputStream
        val bIn: ByteArrayInputStream

        val key: Key = SecretKeySpec(keyBytes, "GOST3412-2015")

        val inCipher = Cipher.getInstance("GOST3412-2015/ECB/NoPadding", "BC")
        val out = Cipher.getInstance("GOST3412-2015/ECB/NoPadding", "BC")
        out.init(Cipher.ENCRYPT_MODE, key)
        inCipher.init(Cipher.DECRYPT_MODE, key)

        //
        // encryption pass
        //
        val bOut = ByteArrayOutputStream()

        cOut = CipherOutputStream(bOut, out)

        for (i in 0 until input.size / 2) {
            cOut.write(input[i].toInt())
        }
        cOut.write(input, input.size / 2, input.size - input.size / 2)
        cOut.close()

        var bytes = bOut.toByteArray()

        if (!bytes.contentEquals(output)) {
            fail(
                "GOST3412-2015 failed encryption - expected " + String(Hex.encode(output)) + " got " + String(
                    Hex.encode(
                        bytes
                    )
                )
            )
        }

        //
        // decryption pass
        //
        bIn = ByteArrayInputStream(bytes)

        cIn = CipherInputStream(bIn, inCipher)

        val dIn = DataInputStream(cIn)

        bytes = ByteArray(input.size)

        for (i in 0 until input.size / 2) {
            bytes[i] = dIn.read().toByte()
        }
        dIn.readFully(bytes, input.size / 2, bytes.size - input.size / 2)

        if (!bytes.contentEquals(input)) {
            fail(
                "GOST3412-2015 failed decryption - expected " + String(Hex.encode(input)) + " got " + String(
                    Hex.encode(
                        bytes
                    )
                )
            )
        }
    }

    private fun testCFB(
        keyBytes: ByteArray,
        iv: ByteArray,
        input: ByteArray,
        output: ByteArray
    ) {
        val cIn: CipherInputStream
        val cOut: CipherOutputStream
        val bIn: ByteArrayInputStream

        val key: Key = SecretKeySpec(keyBytes, "GOST3412-2015")

        val `in` = Cipher.getInstance("GOST3412-2015/CFB8/NoPadding", "BC")
        val out = Cipher.getInstance("GOST3412-2015/CFB8/NoPadding", "BC")

        out.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        `in`.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))

        //
        // encryption pass
        //
        val bOut = ByteArrayOutputStream()

        cOut = CipherOutputStream(bOut, out)

        for (i in 0 until input.size / 2) {
            cOut.write(input[i].toInt())
        }
        cOut.write(input, input.size / 2, input.size - input.size / 2)
        cOut.close()

        var bytes = bOut.toByteArray()

        if (!areEqual(bytes, output)) {
            fail(
                "GOST3412-2015 failed encryption - expected " + String(Hex.encode(output)) + " got " + String(
                    Hex.encode(
                        bytes
                    )
                )
            )
        }

        //
        // decryption pass
        //
        bIn = ByteArrayInputStream(bytes)

        cIn = CipherInputStream(bIn, `in`)

        val dIn = DataInputStream(cIn)

        bytes = ByteArray(input.size)

        for (i in 0 until input.size / 2) {
            bytes[i] = dIn.read().toByte()
        }
        dIn.readFully(bytes, input.size / 2, bytes.size - input.size / 2)

        if (!areEqual(bytes, input)) {
            fail(
                "GOST3412-2015 failed decryption - expected " + String(Hex.encode(input)) + " got " + String(
                    Hex.encode(
                        bytes
                    )
                )
            )
        }
    }

    private fun testCTR() {
        testG3413CTRInit(8)

        try {
            testG3413CTRInit(16)
        } catch (e: InvalidAlgorithmParameterException) {
            isTrue(e.message!!.endsWith("IV must be 8 bytes long."))
        }
    }

    private fun testG3413CTRInit(pIVLen: Int) {
        /* Create the generator and generate a key */
        val myGenerator = KeyGenerator.getInstance("GOST3412-2015", "BC")

        /* Initialise the generator */
        myGenerator.init(256)
        val myKey = myGenerator.generateKey()

        /* Create IV */
        val myIV = ByteArray(pIVLen)
        CryptoServicesRegistrar.getSecureRandom().nextBytes(myIV)

        /* Create a G3413CTR Cipher */
        val myCipher = Cipher.getInstance("GOST3412-2015" + "/CTR/NoPadding", "BC")
        myCipher.init(Cipher.ENCRYPT_MODE, myKey, IvParameterSpec(myIV))

        val msg = Strings.toByteArray("G3413CTR JCA init Bug fixed")

        val enc = myCipher.doFinal(msg)

        myCipher.init(Cipher.DECRYPT_MODE, myKey, IvParameterSpec(myIV))

        val dec = myCipher.doFinal(enc)

        isTrue(areEqual(msg, dec))
    }

    private fun isTrue(var1: Boolean) {
        if (!var1) {
            throw RuntimeException()
        }
    }
}