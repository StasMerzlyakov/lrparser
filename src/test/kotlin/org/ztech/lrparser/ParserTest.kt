package org.ztech.lrparser

import org.junit.jupiter.api.Test

class ParserTest {

    @Test
    fun doFirstTest() {
        initProductionMap()
        initNullableList()
        println(PRODUCTION_MAP)
        println(NULLABLE_LIST)

        val inputStack = mutableListOf<Char>()
        PRODUCTION_STACK.clear()
        ACCEPTED_STACK.clear()

        "ab".forEach { inputStack.push(it) }
        inputStack.reverse() // TODO - подумать надо api

        parse(inputStack)

        inputStack.clear()
        PRODUCTION_STACK.clear()
        ACCEPTED_STACK.clear()

        "bc".forEach { inputStack.push(it) }
        inputStack.reverse() // TODO - подумать надо api
        parse(inputStack)
        println(PRODUCTION_STACK)
        println(ACCEPTED_STACK)


        inputStack.clear()
        PRODUCTION_STACK.clear()
        ACCEPTED_STACK.clear()

        "dad".forEach { inputStack.push(it) }
        inputStack.reverse() // TODO - подумать надо api
        parse(inputStack)
        println(PRODUCTION_STACK)
        println(ACCEPTED_STACK)

    }
}