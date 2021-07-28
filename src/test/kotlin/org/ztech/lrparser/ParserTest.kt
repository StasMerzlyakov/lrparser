package org.ztech.lrparser

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ParserTest {

    /**
     * Тест на корректность проверки грамматик
     */
    @Test
    fun checkGrammar() {

        // Список терминальных символов пустой
        assertThrows<Exception>({ "Should throw an Exception" }) {
            Grammar(
                terminals = setOf(),
                nonTerminals = setOf(),
                productions = mapOf(),
                startProduction = 'S'
            )
        }

        // Список терминальных символов должен содержать только латинские символы в нижнем регистре.
        assertThrows<Exception>({ "Should throw an Exception" }) {
            Grammar(
                terminals = setOf('A'),
                nonTerminals = setOf(),
                productions = mapOf(),
                startProduction = 'S'
            )
        }

        // Список нетерминальных символов пустой.
        assertThrows<Exception>({ "Should throw an Exception" }) {
            Grammar(
                terminals = setOf('a'),
                nonTerminals = setOf(),
                productions = mapOf(),
                startProduction = 'S'
            )
        }

        // Список нетерминальных символов должен содержать только латинские символы в верхнем регистре.
        assertThrows<Exception>({ "Should throw an Exception" }) {
            Grammar(
                terminals = setOf('a'),
                nonTerminals = setOf('b'),
                productions = mapOf(),
                startProduction = 'S'
            )
        }

        // Проверим что для каждого нетерминального символа имеется продукция
        assertThrows<Exception>({ "Should throw an Exception" }) {
            Grammar(
                terminals = setOf('a'),
                nonTerminals = setOf('A', 'B', 'S'),
                productions = mapOf('A' to setOf("a")),
                startProduction = 'S'
            )
        }

        // Левая часть продукции должна содержать только нетерминальные символы.
        assertThrows<Exception>({ "Should throw an Exception" }) {
            Grammar(
                terminals = setOf('a', 'b', 's'),
                nonTerminals = setOf('A', 'B', 'S'),
                productions = mapOf(
                    'A' to setOf("a"), 'B' to setOf("b"),
                    'S' to setOf("s"), 'a' to setOf("A")
                ),
                startProduction = 'S'
            )
        }

        // Правая часть продукций должна состоять только из символов грамматики.
        assertThrows<Exception>({ "Should throw an Exception" }) {
            Grammar(
                terminals = setOf('a', 'b', 's'),
                nonTerminals = setOf('A', 'B', 'S'),
                productions = mapOf(
                    'A' to setOf(""), 'B' to setOf("b"),
                    'S' to setOf("s", "e")
                ),
                startProduction = 'S'
            )
        }
    }

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
