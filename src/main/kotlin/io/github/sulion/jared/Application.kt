package io.github.sulion.jared

import com.fasterxml.jackson.core.JsonParseException
import com.typesafe.config.ConfigFactory
import io.github.sulion.jared.bot.Config
import io.github.sulion.jared.bot.JaredBot
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.config.HoconApplicationConfig
import io.ktor.features.*
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.util.KtorExperimentalAPI
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import java.util.zip.DataFormatException


@UseExperimental(KtorExperimentalAPI::class)
fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    install(StatusPages) {
        exception<DataFormatException> { ex ->
            call.respond(HttpStatusCode.BadRequest, ex.message!!)
        }
        exception<JsonParseException> {
            call.respond(HttpStatusCode.UnsupportedMediaType, "The message is not a well-formed JSON")
        }
        exception<UnsupportedMediaTypeException> {
            call.respond(HttpStatusCode.UnsupportedMediaType, "The message is not a well-formed JSON or a Content-type header is missing")
        }
        exception<Exception> { cause ->
            call.respond(HttpStatusCode.InternalServerError, "Unexpected server error")
            throw cause // Rethrow to log it
        }
    }
    install(ContentNegotiation) {
        jackson {}
    }
    install(Routing) {
        get("/") {
            call.respondText("Use POST method to convert working hours data to human-readable text.", ContentType.Text.Plain)
        }
    }

    val config = HoconApplicationConfig(ConfigFactory.load())
    val botConfig = Config(
        token = config.property("ktor.jared.tg-bot-token").getString(),
        allowedUsers = config.property("ktor.jared.users").getString().split(",")
    )
    ApiContextInitializer.init()
    val botsApi = TelegramBotsApi().also {
        it.registerBot(JaredBot(botConfig))
    }
}