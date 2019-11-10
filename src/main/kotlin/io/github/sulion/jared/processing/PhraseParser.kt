package io.github.sulion.jared.processing

import io.github.sulion.jared.data.ExpenseCategory
import io.github.sulion.jared.data.ExpenseRecord
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
    fun parseExpenseMessage(user: String, message: String): ExpenseRecord {
        val groups = VALID_MSG_PATTERN.matchEntire(message)!!.groupValues
        return ExpenseRecord(
            amount = BigDecimal(groups[1]),
            authorizedBy = user,
            category = ExpenseCategory.valueOf(groups[2].toUpperCase()),
            date = LocalDate.parse(groups[3], DATE_PATTERN),
            comment = message
        )
    }
}