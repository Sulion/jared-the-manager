package io.github.sulion.jared.data

import org.jetbrains.exposed.sql.Table
import java.math.BigDecimal
import java.time.LocalDate

annotation class DefaultConstructor

@DefaultConstructor
data class ExpenseRecord(
    val amount: BigDecimal,
    val authorizedBy: String,
    val category: ExpenseCategory,
    val comment: String,
    val date: LocalDate
)

data class Clarification(
    val field: String,
    val value: String
)

enum class ExpenseCategory {
    GROCERY, HEALTH, SELFCARE, TRAVEL, LUNCH, EVENTS, GIFTS, CLOTHES, RENT
}


object Expenses : Table() {
    val id = integer("ID").primaryKey()
    val authorizedBy = text("AUTHORIZED_BY")
    val amount = decimal(name = "AMOUNT", precision = 10, scale = 2)
    val category = text("CATEGORY")
    val transactionDate = datetime("TRANSACTION_DATE")
    val comment = text("COMMENT")
}