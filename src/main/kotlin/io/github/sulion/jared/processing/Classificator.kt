package io.github.sulion.jared.processing

import io.github.sulion.jared.config.DSL_CONFIG
import io.github.sulion.jared.data.ExpenseCategory
import io.github.sulion.jared.models.Tables.CLASSIFICATOR
import org.apache.commons.codec.digest.DigestUtils
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.util.*
import javax.sql.DataSource

class Classificator(private val dataSource: DataSource) {

    fun classify(term: String): ExpenseCategory? =
        dataSource.connection.use {
            DSL.using(it, SQLDialect.POSTGRES_10, DSL_CONFIG.settings)
                .selectFrom(CLASSIFICATOR)
                .where(CLASSIFICATOR.KEYWORD.startsWith(term))
                .fetch(CLASSIFICATOR.CATEGORY)
                .firstOrNull()
                ?.toUpperCase()
                ?.toCategory()
        }


    fun extendClassification(term: String, category: ExpenseCategory) {
        dataSource.connection.use {
            DSL.using(it, SQLDialect.POSTGRES_10, DSL_CONFIG.settings).transaction { c ->
                DSL.using(c)
                    .insertInto(CLASSIFICATOR, CLASSIFICATOR.HASH, CLASSIFICATOR.KEYWORD, CLASSIFICATOR.CATEGORY)
                    .values(term.digest(), term, category.name.toLowerCase())
            }
        }
    }

    private fun String.toCategory() =
        ExpenseCategory.valueOf(this)

    private fun String.digest() =
        String(Base64.getEncoder().encode(DigestUtils.sha256(this)))
}