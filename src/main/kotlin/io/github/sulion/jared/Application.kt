package io.github.sulion.jared

import com.fasterxml.jackson.core.JsonParseException
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.sulion.jared.bot.Config
import io.github.sulion.jared.bot.JaredBot
import io.github.sulion.jared.processing.ExpenseWriter
import io.github.sulion.jared.report.ReportService
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
import org.flywaydb.core.Flyway
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import java.time.LocalDateTime
import java.util.zip.DataFormatException


@KtorExperimentalAPI
fun Application.main() {
    val config = HoconApplicationConfig(ConfigFactory.load())
    val dataSource = hikari(config)
    Flyway.configure().dataSource(dataSource).load().migrate()
    val reportService = ReportService(dataSource)
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
            call.respond(
                HttpStatusCode.UnsupportedMediaType,
                "The message is not a well-formed JSON or a Content-type header is missing"
            )
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
        get("/report/monthly/{year}/{month}") {
            val today = LocalDateTime.now()
            val year = call.parameters["year"]?.toInt() ?: today.year
            val month = call.parameters["month"]?.toInt() ?: today.month.value
            call.respondText(
                contentType = ContentType.Text.Html,
                status = HttpStatusCode.OK,
                text = reportService.reportFor(year, month)
            )
        }
    }

    val botConfig = Config(
        token = config.property("ktor.jared.tg-bot-token").getString(),
        name = config.property("ktor.jared.tg-bot-name").getString(),
        allowedUsers = config.property("ktor.jared.users").getString().split(",")
    )
    ApiContextInitializer.init()
    TelegramBotsApi().also {
        it.registerBot(JaredBot(botConfig, ExpenseWriter(dataSource)))
    }
}

@KtorExperimentalAPI
private fun hikari(config: HoconApplicationConfig): HikariDataSource {
    val hikariConfig = HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        jdbcUrl = config.property("ktor.jared.jdbc.url").getString()
        username = config.property("ktor.jared.jdbc.user").getString()
        password = config.property("ktor.jared.jdbc.password").getString()
        maximumPoolSize = 3
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
    }
    hikariConfig.validate()
    return HikariDataSource(hikariConfig)
}