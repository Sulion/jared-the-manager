package io.github.sulion.jared

import com.fasterxml.jackson.core.JsonParseException
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.sulion.jared.bot.Config
import io.github.sulion.jared.bot.JaredBot
import io.github.sulion.jared.processing.Classificator
import io.github.sulion.jared.processing.ExpenseWriter
import io.github.sulion.jared.processing.PhraseParser
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
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.koin.Logger.slf4jLogger
import org.koin.core.module.Module
import org.koin.ktor.ext.inject
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import java.time.LocalDateTime
import java.util.zip.DataFormatException
import javax.sql.DataSource


@KtorExperimentalAPI
fun Application.main() {
    val config = HoconApplicationConfig(ConfigFactory.load())
    val jaredModule = createModule()
    install(DefaultHeaders)
    install(CallLogging)
    install(Koin) {
        slf4jLogger()
        properties(config.toMap())
        modules(jaredModule)
    }
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
        val reportService: ReportService by inject()
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
    val api: TelegramBotsApi by inject()
    val jaredBot: JaredBot by inject()
    api.registerBot(jaredBot)
}

const val JDBC_URL = "JDBC_URL"
const val JDBC_USER = "JDBC_USER"
const val JDBC_PASSWORD = "JDBC_PASSWORD"
const val TG_BOT_TOKEN = "TG_BOT_TOKEN"
const val TG_BOT_NAME = "TG_BOT_NAME"
const val TG_ALLOWED_USERS = "TG_ALLOWED_USERS"

@KtorExperimentalAPI
private fun HoconApplicationConfig.toMap(): Map<String, Any> =
    mapOf(
        JDBC_URL to property("ktor.jared.jdbc.url").getString(),
        JDBC_USER to property("ktor.jared.jdbc.user").getString(),
        JDBC_PASSWORD to property("ktor.jared.jdbc.password").getString(),
        TG_BOT_TOKEN to property("ktor.jared.tg-bot-token").getString(),
        TG_BOT_NAME to property("ktor.jared.tg-bot-name").getString(),
        TG_ALLOWED_USERS to property("ktor.jared.users").getString().split(",")
    )


private fun createModule(): Module {
    return module {
        single<DataSource> {
            HikariConfig().apply {
                driverClassName = "org.postgresql.Driver"
                jdbcUrl = getProperty(JDBC_URL)
                username = getProperty(JDBC_USER)
                password = getProperty(JDBC_PASSWORD)
                maximumPoolSize = 3
                connectionTestQuery = "Select 1"
                isAutoCommit = false
            }.also { it.validate() }
                .let { HikariDataSource(it) }
                .also { Flyway.configure().dataSource(it).load().migrate() }
        }
        single { Classificator(get()) }
        single { ReportService(get()) }
        single {
            Config(
                token = getProperty(TG_BOT_TOKEN),
                name = getProperty(TG_BOT_NAME),
                allowedUsers = getProperty(TG_ALLOWED_USERS)
            )
        }
        single { ExpenseWriter(get()) }
        single { PhraseParser(get()) }
        single { JaredBot(get(), get(), get(), get()) }
        single {
            ApiContextInitializer.init()
            TelegramBotsApi()
        }
    }
}