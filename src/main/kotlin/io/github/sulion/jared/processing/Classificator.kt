package io.github.sulion.jared.processing

import io.github.sulion.jared.config.DSL_CONFIG
import io.github.sulion.jared.data.ExpenseCategory
import org.apache.commons.codec.digest.DigestUtils
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import javax.sql.DataSource

class Classificator(private val dataSource: DataSource) {

    fun classify(term: String): ExpenseCategory? {
        dataSource.connection.use {
            DSL.using(it, SQLDialect.POSTGRES_10, DSL_CONFIG.settings)

        }
        return null
    }

    fun extendClassification(term: String, category: ExpenseCategory) {
        dataSource.connection.use {
            DSL.using(it, SQLDialect.POSTGRES_10, DSL_CONFIG.settings).transaction { c ->
                DSL.using(c)
                DigestUtils.sha256(term)
            }

        }
    }
}