package io.github.sulion.jared.bot

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update


const val MY_NAME = "jared_bot"

class JaredBot(private val config: Config) : TelegramLongPollingBot() {
    override fun getBotUsername(): String = MY_NAME

    override fun getBotToken(): String = config.token

    override fun onUpdateReceived(update: Update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.message.hasText()) {
            when (update.message.from.userName) {
                in config.allowedUsers -> SendMessage() // Create a SendMessage object with mandatory fields
                    .setChatId(update.message.chatId)
                    .setText("Did you say \"${update.message.text}\"?")
                    .let { execute<Message, SendMessage>(it) }
            }
        }
    }
}

data class Config(
    val token: String,
    val allowedUsers: List<String>
)