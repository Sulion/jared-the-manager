package io.github.sulion.jared.data

import java.math.BigDecimal
import java.time.LocalDate

annotation class DefaultConstructor

@DefaultConstructor
data class ExpenseRecord(
    val msgId: Int,
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
