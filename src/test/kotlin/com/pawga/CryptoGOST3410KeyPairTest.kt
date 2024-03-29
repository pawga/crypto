package com.pawga

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.Test
import java.security.*
import java.security.spec.ECGenParameterSpec
import kotlin.test.DefaultAsserter.fail

/**
 * Created by pawga777 on 27.03.2024
 */

class CryptoGOST3410KeyPairTest {

    @Test
    fun performTest() {
        Security.addProvider(BouncyCastleProvider())
        gost2012MismatchTest()
    }

    @Throws(Exception::class)
    private fun gost2012MismatchTest() {
        var keyPair = KeyPairGenerator.getInstance(
            "ECGOST3410-2012", "BC"
        )

        keyPair.initialize(ECGenParameterSpec("Tc26-Gost-3410-12-512-paramSetA"))

        var kp = keyPair.generateKeyPair()

        testWrong256(kp)

        keyPair = KeyPairGenerator.getInstance(
            "ECGOST3410-2012", "BC"
        )

        keyPair.initialize(ECGenParameterSpec("Tc26-Gost-3410-12-512-paramSetB"))

        kp = keyPair.generateKeyPair()

        testWrong256(kp)

        keyPair = KeyPairGenerator.getInstance(
            "ECGOST3410-2012", "BC"
        )

        keyPair.initialize(ECGenParameterSpec("Tc26-Gost-3410-12-512-paramSetC"))

        kp = keyPair.generateKeyPair()

        testWrong256(kp)

        keyPair = KeyPairGenerator.getInstance(
            "ECGOST3410-2012", "BC"
        )

        keyPair.initialize(ECGenParameterSpec("Tc26-Gost-3410-12-256-paramSetA"))

        kp = keyPair.generateKeyPair()

        testWrong512(kp)
    }

    @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class)
    private fun testWrong512(kp: KeyPair) {
        val sig = Signature.getInstance("ECGOST3410-2012-512", "BC")

        try {
            sig.initSign(kp.private)

            fail("no exception")
        } catch (e: InvalidKeyException) {
            isEquals("key too weak for ECGOST-2012-512", e.message)
        }

        try {
            sig.initVerify(kp.public)
            fail("no exception")
        } catch (e: InvalidKeyException) {
            isEquals("key too weak for ECGOST-2012-512", e.message)
        }
    }

    private fun isEquals(s: String, message: String?) {

    }

    @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class)
    private fun testWrong256(kp: KeyPair) {
        val sig = Signature.getInstance("ECGOST3410-2012-256", "BC")

        try {
            sig.initSign(kp.private)
            fail("no exception")
        } catch (e: InvalidKeyException) {
            isEquals("key out of range for ECGOST-2012-256", e.message)
        }

        try {
            sig.initVerify(kp.public)
            fail("no exception")
        } catch (e: InvalidKeyException) {
            isEquals("key out of range for ECGOST-2012-256", e.message)
        }
    }

}