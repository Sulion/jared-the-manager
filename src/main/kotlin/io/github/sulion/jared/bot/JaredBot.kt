package io.github.sulion.jared.bot

import io.github.sulion.jared.data.ExpenseCategory
import io.github.sulion.jared.data.ExpenseRecord
import io.github.sulion.jared.processing.ExpenseWriter
import io.github.sulion.jared.processing.PhraseParser
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update


class JaredBot(
    private val config: Config,
    private val expenseWriter: ExpenseWriter,
    private val parser: PhraseParser
) : TelegramLongPollingBot() {
    override fun getBotUsername(): String = config.name

    override fun getBotToken(): String = config.token

    override fun onUpdateReceived(update: Update) {
        // We check if the update has a message and the message has text
        when (type(update)) {
            MsgType.ORIGINAL -> handleOriginal(update.message)
            MsgType.UPDATE -> handleUpdate(update.editedMessage)
            MsgType.UNKNOWN -> TODO()
        }
    }

    private fun handleOriginal(message: Message) {
        logger.debug("Got message: {}", message.text)
        insertNewExpenseRecord(message)
    }

    private fun insertNewExpenseRecord(message: Message) {
        val results: Pair<List<Any>, List<Any>> = message.text.lineSequence()
            .map { parser.parseExpenseMessage(message.messageId, message.from.userName, it) ?: it }
            .partition { it is ExpenseRecord }
        expenseWriter.writeExpense(message.from.id, results.first as List<ExpenseRecord>)

        results.first.forEach { respondOK(message.chatId, it as ExpenseRecord) }
        results.second.forEach { respondFail(message.chatId, it as String) }

    }

    private fun handleUpdate(message: Message) {
        logger.debug("Got update message: {}", message.text)
        expenseWriter.deleteExpense(message.from.id, message.messageId).get()
        insertNewExpenseRecord(message)
    }

    private fun respondFail(chatId: Long, text: String) {
        val msg =
            "I don't understand... What do you mean \"${text}\"\n Currently I understand only ${categories()} as categories."
        SendMessage() // Create a SendMessage object with mandatory fields
            .setChatId(chatId)
            .setText(msg)
            .let { execute<Message, SendMessage>(it) }
    }

    private fun respondOK(chatId: Long, record: ExpenseRecord) {
        val msg = "Have you spent ${record.amount} euro on ${record.category.name.toLowerCase()}? Good for you!"
        SendMessage() // Create a SendMessage object with mandatory fields
            .setChatId(chatId)
            .setText(msg)
            .let { execute<Message, SendMessage>(it) }
    }

    private enum class MsgType { ORIGINAL, UPDATE, UNKNOWN }

    private fun type(update: Update): MsgType {
        if (
            update.hasMessage()
            && update.message.hasText()
            && isExpenseRecord(update.message.text)
            && update.message.from.userName in config.allowedUsers
        ) {
            return MsgType.ORIGINAL
        } else if (
            update.hasEditedMessage() && update.editedMessage.hasText()
            && isExpenseRecord(update.editedMessage.text)
            && update.editedMessage.from.userName in config.allowedUsers
        ) {
            return MsgType.UPDATE
        }
        return MsgType.UNKNOWN
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