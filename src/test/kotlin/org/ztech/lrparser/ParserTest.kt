package org.ztech.lrparser

import org.junit.jupiter.api.Test

class ParserTest {

    @Test
    fun doFirstTest() {
        initProductionMap()
        initNullableList()
        println(PRODUCTION_MAP)
        println(NULLABLE_LIST)

        INPUT_STACK.clear()
        PRODUCTION_STACK.clear()
        ACCEPTED_STACK.clear()

        "ab".forEach { INPUT_STACK.push(it) }
        INPUT_STACK.reverse() // TODO - подумать надо api

        parse()

        INPUT_STACK.clear()
        PRODUCTION_STACK.clear()
        ACCEPTED_STACK.clear()

        "bc".forEach { INPUT_STACK.push(it) }
        INPUT_STACK.reverse() // TODO - подумать надо api
        parse()
        println(PRODUCTION_STACK)
        println(ACCEPTED_STACK)


        INPUT_STACK.clear()
        PRODUCTION_STACK.clear()
        ACCEPTED_STACK.clear()

        "dad".forEach { INPUT_STACK.push(it) }
        INPUT_STACK.reverse() // TODO - подумать надо api
        parse()
        println(PRODUCTION_STACK)
        println(ACCEPTED_STACK)

    }
}