package org.ztech.lrparser

class ParserTest {

    /*@Test
    fun doFirstTest() {

        val grammar = Grammar(
            terminals = listOf('a', 'b', 'c', 'd', 'e'),
            nonTerminals = listOf('S', 'A', 'B', 'C'),
            productions = mapOf(
                'S' to listOf("ab", "bAc", "cdBCa"),
                'A' to listOf("bAc", "B"),
                'B' to listOf("dC", null, "Ae"),
                'C' to listOf("aCb", "d", null)
            ),
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
    }*/
}
