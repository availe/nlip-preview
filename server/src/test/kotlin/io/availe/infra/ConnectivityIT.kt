package io.availe.infra

import org.jooq.impl.DSL
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConnectivityIT {
    @Test
    fun pingDatabase() {
        val record = Jooq.dsl.fetchOne(DSL.select(DSL.inline(1)))
        val value = record?.value1()
        assertEquals(1, value)
    }
}
