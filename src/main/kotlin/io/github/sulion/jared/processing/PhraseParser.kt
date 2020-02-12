package io.github.sulion.jared.processing

import io.github.sulion.jared.data.ExpenseRecord
import org.apache.commons.lang3.math.NumberUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

val VALID_MSG_PATTERN = """([0-9.,]+)€ ([a-zA-Z]+) .*?([0-9]{1,2}\.[0-9]{2})""".toRegex()

val DATE_PATTERN: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendPattern("dd.MM")
    .parseDefaulting(ChronoField.YEAR, LocalDate.now().year.toLong())
    .toFormatter()

val LAX_DATE_PATTERN: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendPattern("d.MM")
    .parseDefaulting(ChronoField.YEAR, LocalDate.now().year.toLong())
    .toFormatter()

class PhraseParser(val classificator: Classificator) {
    fun parseExpenseMessage(msgId: Int, user: String, message: String): ExpenseRecord? =
        VALID_MSG_PATTERN.matchEntire(message)
            ?.groupValues
            ?.let { toResult(msgId, it, user, message) }

    private fun toResult(
        msgId: Int,
        params: List<String>,
        user: String,
        message: String
    ): ExpenseRecord? =
        if (validate(params)) {
            val category = classificator.classify(params[2].toLowerCase())
            if (category == null) {
                logger.error("Category {} is not recognised")
                null
            } else {
                ExpenseRecord(
                    msgId = msgId,
                    amount = BigDecimal(params[1].replace(",", ".")),
                    authorizedBy = user,
                    category = category,
                    date = LocalDate.parse(params[3], if (params[3].length == 5) DATE_PATTERN else LAX_DATE_PATTERN),
                    comment = message
                )
            }
        } else null

    private fun validate(params: List<String>): Boolean =
        params.size >= 4 &&
                NumberUtils.isParsable(params[1].replace(",", "."))
    companion object {
        val logger: Logger = LoggerFactory.getLogger(PhraseParser::class.java)
    }
}