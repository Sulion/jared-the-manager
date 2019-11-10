package io.github.sulion.jared.processing

import io.github.sulion.jared.data.ExpenseCategory
import io.github.sulion.jared.data.ExpenseRecord
import org.apache.commons.lang3.math.NumberUtils
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

val VALID_MSG_PATTERN = """([0-9.,]+)€ ([a-zA-Z]+) .*?([0-9]{2}\.[0-9]{2})""".toRegex()

val DATE_PATTERN = DateTimeFormatterBuilder()
    .appendPattern("dd.MM")
    .parseDefaulting(ChronoField.YEAR, LocalDate.now().year.toLong())
    .toFormatter()

class PhraseParser {
    fun parseExpenseMessage(user: String, message: String): ExpenseRecord? =
        VALID_MSG_PATTERN.matchEntire(message)
            ?.groupValues
            ?.let { toResult(it, user, message) }

    private fun toResult(
        params: List<String>,
        user: String,
        message: String
    ): ExpenseRecord? =
        if (validate(params)) {
            ExpenseRecord(
                amount = BigDecimal(params[1].replace(",", ".")),
                authorizedBy = user,
                category = ExpenseCategory.valueOf(params[2].toUpperCase()),
                date = LocalDate.parse(params[3], DATE_PATTERN),
                comment = message
            )
        } else null

    private fun validate(params: List<String>): Boolean =
        params.size >= 4 &&
                NumberUtils.isParsable(params[1].replace(",", ".")) &&
                params[2] in ExpenseCategory.values().map { it.name.toLowerCase() }
}