package io.github.sulion.jared.report

import io.github.sulion.jared.data.ExpenseCategory
import io.github.sulion.jared.data.ExpenseRecord
import io.github.sulion.jared.data.Expenses
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.YearMonth
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.sql.DataSource

class ReportService(val dataSource: DataSource) {
    fun reportFor(year: Int, month: Int): String {
        Database.connect(dataSource)
        val st = YearMonth(year, month).toLocalDate(1).toDate()
        val fs = YearMonth(year, month).plusMonths(1).toLocalDate(1).toDate()
        return transaction {
            Expenses.selectAll()
                .orderBy(Expenses.transactionDate)
                .orderBy(Expenses.category)
                .map {
                    ExpenseRecord(
                        amount = it[Expenses.amount],
                        authorizedBy = it[Expenses.authorizedBy],
                        category = ExpenseCategory.valueOf(it[Expenses.category]),
                        date = LocalDateTime.ofEpochSecond(
                            it[Expenses.transactionDate].toInstant().millis / 1000,
                            0,
                            ZoneOffset.UTC
                        ).toLocalDate(),
                        comment = it[Expenses.comment]
                    )
                }.joinToString("\n")
        }
    }

}