package io.github.sulion.jared.bot

import io.github.sulion.jared.data.ExpenseCategory
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
        if (update.hasMessage() && update.message.hasText() && isExpenseRecord(update.message.text)) {
            update.message.text.lineSequence()
                .filter { isExpenseRecord(it) }
                .forEach {
                    val record = parser.parseExpenseMessage(update.message.from.userName, it)
                        .let { r ->
                            when (r) {
                                null -> "I don't understand... What do you mean \"${update.message.text}\"\n Currently I understand only ${categories()} as categories."
                                else -> "Have you spent ${r.amount} euro on ${r.category.name.toLowerCase()}? Good for you!"
                            }
                        }
                    when (update.message.from.userName) {
                        in config.allowedUsers -> SendMessage() // Create a SendMessage object with mandatory fields
                            .setChatId(update.message.chatId)
                            .setText(record)
                            .let { execute<Message, SendMessage>(it) }
                    }
                }
        }
    }

    private fun isExpenseRecord(text: String): Boolean =
        text.isNotEmpty() && text[0].isDigit()

    private fun categories(): String =
        ExpenseCategory.values().map { it.name.toLowerCase() }.joinToString(", ")
}

data class Config(
    val token: String,
    val allowedUsers: List<String>
)