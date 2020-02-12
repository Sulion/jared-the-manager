package io.github.sulion.jared.processing

import io.github.sulion.jared.data.ExpenseCategory
import io.github.sulion.jared.processing.Classificator
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

class Mocks {
    val classificator: Classificator = mockk()
    private val categorySlot = slot<String>()

    init {
        every { classificator.classify(capture(categorySlot)) } answers
                { ExpenseCategory.valueOf(categorySlot.captured.toUpperCase()) }
    }
}