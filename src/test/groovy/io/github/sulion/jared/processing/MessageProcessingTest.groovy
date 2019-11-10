package io.github.sulion.jared.processing

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.sulion.jared.data.ExpenseCategory
import io.github.sulion.jared.data.ExpenseRecord
import spock.lang.Shared
import spock.lang.Specification

class MessageProcessingTest extends Specification {
    def parser = new PhraseParser()
    @Shared
    def mapper = new ObjectMapper()

    def "parse valid messages"() {
        given:
        def record = parser.parseExpenseMessage(authorized, message)
        expect:
        record.amount == amount &&
                record.authorizedBy == user &&
                record.category == ExpenseCategory.valueOf(category)
        where:
        authorized | message             || amount | category  | user   | date
        "anna"     | "89€ grocery 06.10" || 89     | "GROCERY" | "anna" | "2019-10-06"

    }

    def toRecord(String param) {
        return mapper.readValue(param, ExpenseRecord.class)
    }
}
