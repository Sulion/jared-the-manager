package io.github.sulion.jared.report

import io.github.sulion.jared.config.DSL_CONFIG
import io.github.sulion.jared.data.ExpenseCategory
import io.github.sulion.jared.data.ExpenseRecord
import io.github.sulion.jared.models.Tables.EXPENSES
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import javax.sql.DataSource

class ReportService(val dataSource: DataSource) {
    fun reportFor(year: Int, month: Int): String {
        //val st = YearMonth(year, month).toLocalDate(1).toDate()
        //val fs = YearMonth(year, month).plusMonths(1).toLocalDate(1).toDate()
        with(dataSource.connection) {
            val create: DSLContext = DSL.using(this, SQLDialect.POSTGRES_10, DSL_CONFIG.settings)
            return create.selectFrom(EXPENSES)
                .orderBy(EXPENSES.TRANSACTION_DATE, EXPENSES.CATEGORY)
                .fetch().map {
                    ExpenseRecord(
                        amount = it.getValue(EXPENSES.AMOUNT),
                        authorizedBy = it.getValue(EXPENSES.AUTHORIZED_BY),
                        category = ExpenseCategory.valueOf(it.getValue(EXPENSES.CATEGORY)),
                        date = it.getValue(EXPENSES.TRANSACTION_DATE).toLocalDate(),
                        comment = it.getValue(EXPENSES.COMMENT)
                    )
                }.joinToString("\n")
        }
    }

}