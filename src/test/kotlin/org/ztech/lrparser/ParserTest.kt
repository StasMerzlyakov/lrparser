package org.ztech.lrparser

import org.junit.jupiter.api.Assertions
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

    /**
     * Тест на провильность работы функции initFirst
     */
    @Test
    fun doFirstTest() {
        /**
         * S -> AB
         * B -> aAB | ε
         * A -> CD
         * D -> bCD | ε
         * C -> dSf | e
         */
        val grammar = Grammar(
            terminals = setOf('a', 'b', 'c', 'd', 'e', 'f'),
            nonTerminals = setOf('S', 'A', 'B', 'C', 'D'),
            productions = mapOf(
                'S' to setOf("AB"),
                'B' to setOf("aAB", ""),
                'A' to setOf("CD"),
                'D' to setOf("bCD", ""),
                'C' to setOf("dSd", "e")
            ),
            startProduction = 'S'
        )

        val firstMap = grammar.getFirst()
        println(firstMap)

        Assertions.assertTrue(firstMap.getValue('a').equalsTo(setOf('a')))
        Assertions.assertTrue(firstMap.getValue('b').equalsTo(setOf('b')))
        Assertions.assertTrue(firstMap.getValue('c').equalsTo(setOf('c')))
        Assertions.assertTrue(firstMap.getValue('d').equalsTo(setOf('d')))
        Assertions.assertTrue(firstMap.getValue('f').equalsTo(setOf('f')))

        Assertions.assertTrue(firstMap.getValue('S').equalsTo(setOf('d', 'e')))
        Assertions.assertTrue(firstMap.getValue('B').equalsTo(setOf('a', Grammar.EPSILON)))
        Assertions.assertTrue(firstMap.getValue('A').equalsTo(setOf('d', 'e')))
        Assertions.assertTrue(firstMap.getValue('D').equalsTo(setOf('b', Grammar.EPSILON)))
        Assertions.assertTrue(firstMap.getValue('C').equalsTo(setOf('d', 'e')))
    }

    private fun <E> Set<E>.equalsTo(set: Set<E>): Boolean = this.containsAll(set) && set.containsAll(this)
}
