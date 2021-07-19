package org.ztech.lrparser

import org.junit.jupiter.api.Test

class ParserTest {

    @Test
    fun doFirstTest() {

        /*
// Расширенное множество терминальных символов (с null)
val TERMINAL = listOf('a', 'b', 'c', 'd', 'e')

// Множество нетерминальных символов
val NON_TERMINAL = listOf('S', 'A', 'B', 'C')
*/
// Множество продукций
//val PRODUCTION = mapOf(
//    'S' to listOf("ab", "bAc", "cdBCa"),
//    'A' to listOf("bAc", "B"),
//    'B' to listOf("dC", null, "Ae"),
//    'C' to listOf("aCb", "d", null)
//)

        val grammar = Grammar(
            terminals = listOf('a', 'b', 'c', 'd', 'e'),
            nonTerminals = listOf('S', 'A', 'B', 'C'),
            productions = mapOf(
                'S' to listOf("ab", "bAc", "cdBCa"),
                'A' to listOf("bAc", "B"),
                'B' to listOf("dC", null, "Ae"),
                'C' to listOf("aCb", "d", null)),
            startProduction = 'S'
        )
        val inputStack = mutableListOf<Char>()

        "ab".forEach { inputStack.push(it) }
        inputStack.reverse() // TODO - подумать надо api

        grammar.parse(inputStack)

        inputStack.clear()

        "bc".forEach { inputStack.push(it) }
        inputStack.reverse() // TODO - подумать надо api
        grammar.parse(inputStack)

        inputStack.clear()
        "dad".forEach { inputStack.push(it) }
        inputStack.reverse() // TODO - подумать надо api
        grammar.parse(inputStack)

    }
}