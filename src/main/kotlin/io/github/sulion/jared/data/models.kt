package io.github.sulion.jared.data

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

enum class ExpenseCategory {
    GROCERY, HEALTH, SELFCARE, TRAVEL, LUNCH, EVENTS, GIFTS, CLOTHES
}
