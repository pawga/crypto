package com.pawga.controllers

import com.pawga.domain.service.CryptoAsymmetricSigner
import com.pawga.domain.service.CryptoSymmetricSigner
import com.pawga.domain.service.ModeIv
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Part
import io.micronaut.http.annotation.Post
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.http.multipart.StreamingFileUpload
import io.micronaut.http.server.types.files.StreamedFile
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.*

@Controller("/crypto")
class CryptoController(
    private val cryptoService: CryptoAsymmetricSigner,
    private val cryptoSymmetricService: CryptoSymmetricSigner,
) {

    init {
        cryptoService.generateKeyPair()
        cryptoSymmetricService.generateKey(256)
    }

    @Get(uri = "/generate-key-pair")
    @Operation(
        summary = "generate key pair",
    )
    @Tag(name = "generate")
    fun generateKeyPair(): HttpResponse<*> {
        cryptoService.generateKeyPair()
        return HttpResponse.ok<Any>()
    }

    @Get(uri = "/export/public-key")
    @Operation(
        summary = "Export Public Key",
    )
    @Tag(name = "export")
    fun exportPublicPem(): StreamedFile {
        val file = File.createTempFile("public-", ".pem")
        file.outputStream().use {
            cryptoService.exportPublicKey(it)
        }
        return StreamedFile(file.inputStream(), MediaType.TEXT_PLAIN_TYPE)
    }

    @Get(uri = "/export/private-key")
    @Operation(
        summary = "Export Private key",
        description = "Attention! Dangerous operation! Shown for educational purposes!"
    )
    @Tag(name = "export")
    fun exportPrivatePem(): StreamedFile {
        val file = File.createTempFile("private-", ".pem")
        file.outputStream().use {
            cryptoService.exportPrivateKey(it)
        }
        return StreamedFile(file.inputStream(), MediaType.TEXT_PLAIN_TYPE)
    }

    @Post(
        value = "/import/public-key",
        consumes = [MediaType.MULTIPART_FORM_DATA],
        produces = [MediaType.TEXT_PLAIN]
    )
    @ExecuteOn(
        TaskExecutors.BLOCKING
    )
    @Throws(
        IOException::class
    )
    @Operation(
        summary = "Import Public Key",
    )
    @Tag(name = "import")
    fun importPublicKey(data: StreamingFileUpload): HttpResponse<Any> {
        data.asInputStream().use { stream ->
            cryptoService.importPublicKey(stream.readAllBytes())
        }
        return HttpResponse.ok()
    }

    @Post(
        value = "/import/private-key",
        consumes = [MediaType.MULTIPART_FORM_DATA],
        produces = [MediaType.TEXT_PLAIN]
    )
    @ExecuteOn(
        TaskExecutors.BLOCKING
    )
    @Throws(
        IOException::class
    )
    @Operation(
        summary = "Import Private Key",
        description = "Attention! Dangerous operation! Shown for educational purposes!"
    )
    @Tag(name = "import")
    fun importPrivateKey(data: StreamingFileUpload): HttpResponse<Any> {
        data.asInputStream().use { stream ->
            cryptoService.importPrivateKey(stream.readAllBytes())
        }
        return HttpResponse.ok()
    }

    @Post(
        value = "/import/key-pair",
        consumes = [MediaType.MULTIPART_FORM_DATA],
        produces = [MediaType.TEXT_PLAIN]
    )
    @Operation(
        summary = "Import Key Pair",
        description = "Attention! Dangerous operation! Shown for educational purposes!"
    )
    @Tag(name = "import")
    fun importKeyPair(
        @Part("public") public: CompletedFileUpload,
        @Part("private") private: CompletedFileUpload
    ): HttpResponse<Any> {
        cryptoService.importKeyPair(private.inputStream.readAllBytes(), public.inputStream.readAllBytes())
        return HttpResponse.ok()
    }

    @Post(
        value = "/encrypt-file",
        consumes = [MediaType.MULTIPART_FORM_DATA],
        produces = [MediaType.APPLICATION_OCTET_STREAM]
    )
    @Operation(
        summary = "File encryption",
        description = """
            When using RSA, the size of the encrypted file is limited. maximum bytes = key length in bits / 8 - 11
            So, either use a larger key or you encrypt the data with a symmetric key, 
            and encrypt that key with rsa (which is the recommended approach).
            """
    )
    @Tag(name = "encrypt-decrypt")
    fun encrypt(data: CompletedFileUpload): StreamedFile {
        val inputStream = cryptoService.encrypt(data.bytes).inputStream()
        return StreamedFile(inputStream, MediaType.APPLICATION_OCTET_STREAM_TYPE)
    }

    @Post(
        value = "/decrypt-file",
        consumes = [MediaType.MULTIPART_FORM_DATA],
        produces = [MediaType.APPLICATION_OCTET_STREAM]
    )
    @Operation(
        summary = "File decryption",
    )
    @Tag(name = "encrypt-decrypt")
    fun decrypt(data: CompletedFileUpload): StreamedFile {
        val inputStream = cryptoService.decrypt(data.bytes).inputStream()
        return StreamedFile(inputStream, MediaType.APPLICATION_OCTET_STREAM_TYPE)
    }

    @Post(
        value = "/sign-file",
        consumes = [MediaType.MULTIPART_FORM_DATA],
        produces = [MediaType.APPLICATION_OCTET_STREAM]
    )
    @Operation(
        summary = "Calculate file signature",
    )
    @Tag(name = "sign")
    fun sign(data: CompletedFileUpload): StreamedFile {
        val inputStream = cryptoService.sign(data.bytes).inputStream()
        return StreamedFile(inputStream, MediaType.APPLICATION_OCTET_STREAM_TYPE)
    }

    @Post(
        value = "/verify-sign",
        consumes = [MediaType.MULTIPART_FORM_DATA],
        produces = [MediaType.APPLICATION_JSON]
    )
    @Operation(
        summary = "Verify file signature",
    )
    @Tag(name = "sign")
    fun verify(
        @Part("data") data: CompletedFileUpload,
        @Part("sig") sig: CompletedFileUpload
    ): HttpResponse<Boolean> {
        val verify = cryptoService.verify(data.bytes, sig.bytes)
        log.debug("verify is $verify")
        return HttpResponse.ok(verify)
    }

//symmetric-key

    @Get(uri = "/generate-symmetric-key")
    @Operation(
        summary = "generate symmetric key",
    )
    @Tag(name = "symmetric-generate")
    fun generateSymmetricKey(): HttpResponse<*> {
        cryptoSymmetricService.generateKey(256, ModeIv.RIGHTLY)
        return HttpResponse.ok<Any>()
    }

    @Post(
        value = "/symmetric-encrypt-file",
        consumes = [MediaType.MULTIPART_FORM_DATA],
        produces = [MediaType.APPLICATION_OCTET_STREAM]
    )
    @Operation(
        summary = "File encryption symmetric mode",
    )
    @Tag(name = "symmetric-encrypt-decrypt")
    fun symmetricEncrypt(data: CompletedFileUpload): StreamedFile {
        val inputStream = cryptoSymmetricService.encrypt(data.bytes).inputStream()
        return StreamedFile(inputStream, MediaType.APPLICATION_OCTET_STREAM_TYPE)
    }

    @Post(
        value = "/symmetric-decrypt-file",
        consumes = [MediaType.MULTIPART_FORM_DATA],
        produces = [MediaType.APPLICATION_OCTET_STREAM]
    )
    @Operation(
        summary = "File decryption symmetric mode",
    )
    @Tag(name = "symmetric-encrypt-decrypt")
    fun symmetricDecrypt(data: CompletedFileUpload): StreamedFile {
        val inputStream = cryptoSymmetricService.decrypt(data.bytes).inputStream()
        return StreamedFile(inputStream, MediaType.APPLICATION_OCTET_STREAM_TYPE)
    }

    @Get(uri = "/export/symmetric-key")
    @Operation(
        summary = "Export Symmetric Key",
    )
    @Tag(name = "symmetric-export")
    fun exportSymmetricKey(): StreamedFile {
        val file = File.createTempFile("symmetric-key-", ".pem")
        file.outputStream().use {
            cryptoSymmetricService.exportKey(it)
        }
        return StreamedFile(file.inputStream(), MediaType.APPLICATION_OCTET_STREAM_TYPE)
    }

    @Get(uri = "/export/symmetric-iv")
    @Operation(
        summary = "Export Symmetric IV",
    )
    @Tag(name = "symmetric-export")
    fun exportSymmetricIV(): StreamedFile {
        val file = File.createTempFile("symmetric-iv-", ".pem")
        file.outputStream().use {
            cryptoSymmetricService.exportIvParameterSpec(it)
        }
        return StreamedFile(file.inputStream(), MediaType.APPLICATION_OCTET_STREAM_TYPE)
    }

    @Post(
        value = "/import/symmetric-key-iv",
        consumes = [MediaType.MULTIPART_FORM_DATA],
        produces = [MediaType.APPLICATION_OCTET_STREAM]
    )
    @Operation(
        summary = "Import Symmetric Key Iv",
        description = "Attention! Dangerous operation! Shown for educational purposes!"
    )
    @Tag(name = "symmetric-import")
    fun importKeyIv(
        @Part("key") key: CompletedFileUpload,
        @Part("iv") iv: CompletedFileUpload
    ): HttpResponse<Any> {
        cryptoSymmetricService.import(key.inputStream.readAllBytes(), iv.inputStream.readAllBytes())
        return HttpResponse.ok()
    }

    @Post(
        value = "/import/symmetric-key",
        consumes = [MediaType.MULTIPART_FORM_DATA],
        produces = [MediaType.APPLICATION_OCTET_STREAM]
    )
    @Operation(
        summary = "Import Symmetric Key",
        description = "Attention! Dangerous operation! Shown for educational purposes!"
    )
    @Tag(name = "symmetric-import")
    fun importKey(
        @Part("key") key: CompletedFileUpload,
    ): HttpResponse<Any> {
        cryptoSymmetricService.importKey(key.inputStream.readAllBytes())
        return HttpResponse.ok()
    }

    @Post(
        value = "/import/symmetric-iv",
        consumes = [MediaType.MULTIPART_FORM_DATA],
        produces = [MediaType.APPLICATION_OCTET_STREAM]
    )
    @Operation(
        summary = "Import Symmetric Iv",
    )
    @Tag(name = "symmetric-import")
    fun importIv(
        @Part("iv") iv: CompletedFileUpload
    ): HttpResponse<Any> {
        cryptoSymmetricService.importIvParameterSpec(iv.inputStream.readAllBytes())
        return HttpResponse.ok()
    }

//companion

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }
}