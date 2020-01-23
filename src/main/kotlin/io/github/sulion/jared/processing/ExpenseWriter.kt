package io.github.sulion.jared.processing

import io.github.sulion.jared.config.DSL_CONFIG
import io.github.sulion.jared.data.ExpenseRecord
import io.github.sulion.jared.models.Tables.EXPENSES
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import java.sql.Date
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.sql.DataSource

class ExpenseWriter(private val dataSource: DataSource) {
    private val executor = Executors.newFixedThreadPool(4)

    fun writeExpense(msgId: Int, record: ExpenseRecord): Future<*> = executor.submit {
        with(dataSource.connection) {
            val create: DSLContext = DSL.using(this, SQLDialect.POSTGRES_10, DSL_CONFIG.settings)
            create.transaction { c ->
                try {
                    val t = DSL.using(c).insertInto(
                        EXPENSES,
                        EXPENSES.ID,
                        EXPENSES.AMOUNT,
                        EXPENSES.AUTHORIZED_BY,
                        EXPENSES.CATEGORY,
                        EXPENSES.TRANSACTION_DATE,
                        EXPENSES.COMMENT
                    )
                        .values(
                            msgId,
                            record.amount,
                            record.authorizedBy,
                            record.category.name,
                            Date.valueOf(record.date),
                            record.comment
                        ).execute()
                    logger.info(t.toString())
                } catch (ex: Exception) {
                    logger.error("On write:", ex)
                    throw ex
                }
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(ExpenseWriter::class.java)
    }
}