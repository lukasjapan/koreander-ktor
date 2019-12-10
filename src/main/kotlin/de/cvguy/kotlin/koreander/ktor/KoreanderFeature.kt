package de.cvguy.kotlin.koreander.ktor

import de.cvguy.kotlin.koreander.CompiledTemplate
import de.cvguy.kotlin.koreander.Koreander
import io.ktor.application.*
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.response.ApplicationSendPipeline
import io.ktor.response.respond
import io.ktor.util.AttributeKey
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import kotlin.reflect.KType

data class KoreanderContent(val template: String, val model: Any, val type: KType)

class KoreanderFeature {
    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Koreander, KoreanderFeature> {
        override val key = AttributeKey<KoreanderFeature>("koreander")
        override fun install(pipeline: ApplicationCallPipeline, configure: Koreander.() -> Unit): KoreanderFeature {
            val koreander = Koreander().apply(configure)
            val cache = mutableMapOf<String, CompiledTemplate>()

            pipeline.sendPipeline.intercept(ApplicationSendPipeline.Transform) { value ->
                if (value is KoreanderContent) {
                    val compiled = cache.getOrPut(value.template) { koreander.compile(value.template, value.type) }
                    val output = koreander.unsafeRender(compiled, value.model)
                    proceedWith(TextContent(output, ContentType.Text.Html))
                }
            }

            return KoreanderFeature()
        }
    }
}

suspend inline fun <reified T: Any> ApplicationCall.respondKorRes(template: String, model: T, charset: Charset = StandardCharsets.UTF_8) {
    respond(
        KoreanderContent(
            KoreanderContent::class.java.getResource(
                template
            ).readText(charset), model, Koreander.typeOf(model)
        )
    )
}
suspend inline fun <reified T: Any> ApplicationCall.respondKorRaw(template: String, model: T) {
    respond(
        KoreanderContent(
            template,
            model,
            Koreander.typeOf(model)
        )
    )
}
suspend inline fun <reified T: Any> ApplicationCall.respondKor(template: String, model: T, charset: Charset = StandardCharsets.UTF_8) {
    respond(
        KoreanderContent(
            File(template).readText(charset),
            model,
            Koreander.typeOf(model)
        )
    )
}
suspend inline fun <reified T: Any> ApplicationCall.respondKor(template: File, model: T, charset: Charset = StandardCharsets.UTF_8) {
    respond(
        KoreanderContent(
            template.readText(charset),
            model,
            Koreander.typeOf(model)
        )
    )
}
suspend inline fun <reified T: Any> ApplicationCall.respondKor(template: URL, model: T, charset: Charset = StandardCharsets.UTF_8) {
    respond(
        KoreanderContent(
            template.readText(charset),
            model,
            Koreander.typeOf(model)
        )
    )
}
suspend inline fun <reified T: Any> ApplicationCall.respondKor(template: InputStream, model: T, charset: Charset = StandardCharsets.UTF_8) {
    respond(
        KoreanderContent(
            template.bufferedReader(charset).use { it.readText() },
            model,
            Koreander.typeOf(model)
        )
    )
}