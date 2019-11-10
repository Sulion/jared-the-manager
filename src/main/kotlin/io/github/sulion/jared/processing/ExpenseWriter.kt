package io.github.sulion.jared.processing

import io.github.sulion.jared.data.ExpenseRecord
import io.github.sulion.jared.data.Expenses
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.sql.DataSource

class ExpenseWriter(private val dataSource: DataSource) {
    private val executor = Executors.newFixedThreadPool(4)

    fun writeExpense(msgId: Int, record: ExpenseRecord): Future<*> = executor.submit {
        Database.connect(dataSource)
        transaction {
            Expenses.insert {
                it[id] = msgId
                it[amount] = record.amount
                it[authorizedBy] = record.authorizedBy
                it[category] = record.category.name
                it[transactionDate] = DateTime.parse(record.date.toString())
                it[comment] = record.comment
            }
        }
    }
}