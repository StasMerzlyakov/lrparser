package org.ztech.lrparser

import org.junit.jupiter.api.Test

class ParserTest {

    @Test
    fun doFirstTest() {
        initProductionMap()
        println(PRODUCTION_MAP)

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
    }
}