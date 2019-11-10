package io.github.sulion.jared.bot

import io.github.sulion.jared.processing.PhraseParser
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update


const val MY_NAME = "jared_bot"

class JaredBot(private val config: Config) : TelegramLongPollingBot() {
    val parser = PhraseParser()
    override fun getBotUsername(): String = MY_NAME

    override fun getBotToken(): String = config.token

    override fun onUpdateReceived(update: Update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.message.hasText()) {
            val record = parser.parseExpenseMessage(update.message.from.userName, update.message.text)
            when (update.message.from.userName) {
                in config.allowedUsers -> SendMessage() // Create a SendMessage object with mandatory fields
                    .setChatId(update.message.chatId)
                    .setText("Have you spent ${record.amount} on ${record.category.name.toLowerCase()}? Good for you!")
                    .let { execute<Message, SendMessage>(it) }
            }
        }
    }
}

data class Config(
    val token: String,
    val allowedUsers: List<String>
)