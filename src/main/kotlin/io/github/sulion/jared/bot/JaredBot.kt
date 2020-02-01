package io.github.sulion.jared.bot

import io.github.sulion.jared.data.ExpenseCategory
import io.github.sulion.jared.processing.ExpenseWriter
import io.github.sulion.jared.processing.PhraseParser
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update


class JaredBot(
    private val config: Config,
    private val expenseWriter: ExpenseWriter
) : TelegramLongPollingBot() {
    private val parser = PhraseParser()
    override fun getBotUsername(): String = config.name

    override fun getBotToken(): String = config.token

    override fun onUpdateReceived(update: Update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.message.hasText() && isExpenseRecord(update.message.text)) {
            logger.debug("Got message: {}", update.message.text)
            update.message.text.lineSequence()
                .filter { isExpenseRecord(it) }
                .forEach {
                    val record = parser.parseExpenseMessage(update.message.messageId, update.message.from.userName, it)
                        .let { r ->
                            when (r) {
                                null -> "I don't understand... What do you mean \"${update.message.text}\"\n Currently I understand only ${categories()} as categories."
                                else -> {
                                    expenseWriter.writeExpense(update.message.from.id, r)
                                    "Have you spent ${r.amount} euro on ${r.category.name.toLowerCase()}? Good for you!"
                                }
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
        ExpenseCategory.values().joinToString(", ") { it.name.toLowerCase() }

    companion object {
        val logger = LoggerFactory.getLogger(JaredBot::class.java)
    }
}

data class Config(
    val token: String,
    val name: String,
    val allowedUsers: List<String>
)