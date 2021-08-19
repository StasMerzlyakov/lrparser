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
                startProduction = "S"
            )
        }

        // Список нетерминальных символов пустой.
        assertThrows<Exception>({ "Should throw an Exception" }) {
            Grammar(
                terminals = setOf("a"),
                nonTerminals = setOf(),
                productions = mapOf(),
                startProduction = "S"
            )
        }

        // Списки нетерминальных и терминальных символов имеют пересечения.
        assertThrows<Exception>({ "Should throw an Exception" }) {
            Grammar(
                terminals = setOf("a"),
                nonTerminals = setOf(),
                productions = mapOf(),
                startProduction = "S"
            )
        }

        // Проверим что для каждого нетерминального символа имеется продукция
        assertThrows<Exception>({ "Should throw an Exception" }) {
            Grammar(
                terminals = setOf("a"),
                nonTerminals = setOf("A", "B", "S"),
                productions = mapOf("A" to listOf(listOf("a"))),
                startProduction = "S"
            )
        }

        // Левая часть продукции должна содержать только нетерминальные символы.
        assertThrows<Exception>({ "Should throw an Exception" }) {
            Grammar(
                terminals = setOf("a", "b", "s"),
                nonTerminals = setOf("A", "B", "S"),
                productions = mapOf(
                    "A" to listOf(listOf("a")),
                    "B" to listOf(listOf("b")),
                    "S" to listOf(listOf("s")),
                    "a" to listOf(listOf("A"))
                ),
                startProduction = "S"
            )
        }

        // Правая часть продукций должна состоять только из символов грамматики.
        assertThrows<Exception>({ "Should throw an Exception" }) {
            Grammar(
                terminals = setOf("a", "b", "s"),
                nonTerminals = setOf("A", "B", "S"),
                productions = mapOf(
                    "A" to listOf(listOf("a")),
                    "B" to listOf(listOf("b")),
                    "S" to listOf(listOf("s"), listOf("e")),
                ),
                startProduction = "S"
            )
        }

        // Стартовая продукция должна присутствовать в списке нетерминалов.
        assertThrows<Exception>({ "Should throw an Exception" }) {
            Grammar(
                terminals = setOf("a", "b", "s"),
                nonTerminals = setOf("A", "B", "S"),
                productions = mapOf(
                    "A" to listOf(listOf("a")),
                    "B" to listOf(listOf("b")),
                    "S" to listOf(listOf("s"), listOf("e")),
                ),
                startProduction = "S1"
            )
        }
    }

    private fun <E> Set<E>.equalsTo(set: Set<E>): Boolean = this.containsAll(set) && set.containsAll(this)

    /**
     * Тест на правильность работы функции initFirst
     */
    @Test
    fun doFirstTest() {
        /**
         * S -> AB
         * B -> aAB | ε
         * A -> CD
         * D -> bCD | ε
         * C -> dSe | c
         */
        val grammar = Grammar(
            terminals = setOf("a", "b", "c", "d", "e"),
            nonTerminals = setOf("S", "A", "B", "C", "D"),
            productions = mapOf(
                "S" to listOf(listOf("A", "B")),
                "B" to listOf(listOf("a", "A", "B"), listOf("ε")),
                "A" to listOf(listOf("C", "D")),
                "D" to listOf(listOf("b", "C", "D"), listOf("ε")),
                "C" to listOf(listOf("d", "S", "e"), listOf("c"))
            ),
            startProduction = "S"
        )

        val firstMap = grammar.getFirst()
        println(firstMap)

        Assertions.assertTrue(firstMap.getValue("a").equalsTo(setOf("a")))
        Assertions.assertTrue(firstMap.getValue("b").equalsTo(setOf("b")))
        Assertions.assertTrue(firstMap.getValue("c").equalsTo(setOf("c")))
        Assertions.assertTrue(firstMap.getValue("d").equalsTo(setOf("d")))
        Assertions.assertTrue(firstMap.getValue("e").equalsTo(setOf("e")))

        Assertions.assertTrue(firstMap.getValue("S").equalsTo(setOf("d", "c")))
        Assertions.assertTrue(firstMap.getValue("B").equalsTo(setOf("a", "ε")))
        Assertions.assertTrue(firstMap.getValue("A").equalsTo(setOf("d", "c")))
        Assertions.assertTrue(firstMap.getValue("D").equalsTo(setOf("b", "ε")))
        Assertions.assertTrue(firstMap.getValue("C").equalsTo(setOf("d", "c")))
    }

    /**
     * Тест на правильность работы функции initFirst
     */
    @Test
    fun doFirstTest2() {
        /**
         * E  -> T E′
         * E′ -> + T E′ | ε
         * T  -> F T′
         * T′ -> * F T′ | ε
         * F -> ( E ) | id
         */
        val grammar = Grammar(
            terminals = setOf("id", "(", ")", "*", "+"),
            nonTerminals = setOf("E", "E′", "T", "T′", "F"),
            productions = mapOf(
                "E" to listOf(listOf("T", "E′")),
                "E′" to listOf(listOf("+", "T", "E′"), listOf("ε")),
                "T" to listOf(listOf("F", "T′")),
                "T′" to listOf(listOf("*", "F", "T′"), listOf("ε")),
                "F" to listOf(listOf("(", "E", ")"), listOf("id"))
            ),
            startProduction = "E"
        )

        val firstMap = grammar.getFirst()
        println(firstMap)

        Assertions.assertTrue(firstMap.getValue("(").equalsTo(setOf("(")))
        Assertions.assertTrue(firstMap.getValue(")").equalsTo(setOf(")")))
        Assertions.assertTrue(firstMap.getValue("+").equalsTo(setOf("+")))
        Assertions.assertTrue(firstMap.getValue("*").equalsTo(setOf("*")))
        Assertions.assertTrue(firstMap.getValue("id").equalsTo(setOf("id")))

        Assertions.assertTrue(firstMap.getValue("F").equalsTo(setOf("(", "id")))
        Assertions.assertTrue(firstMap.getValue("T").equalsTo(setOf("(", "id")))
        Assertions.assertTrue(firstMap.getValue("E").equalsTo(setOf("(", "id")))
        Assertions.assertTrue(firstMap.getValue("E′").equalsTo(setOf("+", "ε")))
        Assertions.assertTrue(firstMap.getValue("T′").equalsTo(setOf("*", "ε")))
    }

    @Test
    fun doFirstTest3() {
        /**
         * E  -> T E′
         * E′ -> + T E′ | ε
         * T  -> F T′
         * T′ -> * F T′ | ε
         * F -> ( E ) | id
         */

        val grammar = Grammar(
            terminals = setOf("id", "(", ")", "*", "+"),
            nonTerminals = setOf("E", "E′", "T", "T′", "F"),
            productions = mapOf(
                "E" to listOf(listOf("T", "E′")),
                "E′" to listOf(listOf("+", "T", "E′"), listOf("ε")),
                "T" to listOf(listOf("F", "T′")),
                "T′" to listOf(listOf("*", "F", "T′"), listOf("ε")),
                "F" to listOf(listOf("(", "E", ")"), listOf("id"))
            ),
            startProduction = "E"
        )

        Assertions.assertTrue(grammar.first(listOf("F", "T′")).equalsTo(setOf("(", "id")))
        Assertions.assertTrue(grammar.first(listOf("E′", "F")).equalsTo(setOf("(", "id", "+")))
        Assertions.assertTrue(grammar.first(listOf("E′", "T′")).equalsTo(setOf("*", "+", "ε")))
    }

    /**
     * Тест на правильность работы функции initFollow
     */
    @Test
    fun doFollowTest() {
        /**
         * E  -> T E′
         * E′ -> + T E′ | ε
         * T  -> F T′
         * T′ -> * F T′ | ε
         * F -> ( E ) | id
         */
        val grammar = Grammar(
            terminals = setOf("id", "(", ")", "*", "+"),
            nonTerminals = setOf("E", "E′", "T", "T′", "F"),
            productions = mapOf(
                "E" to listOf(listOf("T", "E′")),
                "E′" to listOf(listOf("+", "T", "E′"), listOf("ε")),
                "T" to listOf(listOf("F", "T′")),
                "T′" to listOf(listOf("*", "F", "T′"), listOf("ε")),
                "F" to listOf(listOf("(", "E", ")"), listOf("id"))
            ),
            startProduction = "E"
        )

        val followMap = grammar.getFollow()
        println(followMap)

        Assertions.assertTrue(followMap.getValue("E").equalsTo(setOf(")", "$")))
        Assertions.assertTrue(followMap.getValue("E′").equalsTo(setOf(")", "$")))
        Assertions.assertTrue(followMap.getValue("T").equalsTo(setOf("+", ")", "$")))
        Assertions.assertTrue(followMap.getValue("T′").equalsTo(setOf("+", ")", "$")))
        Assertions.assertTrue(followMap.getValue("F").equalsTo(setOf("+", "*", ")", "$")))
    }

    /**
     * Тест на правильность генерации таблицы синтаксического разбора
     */
    @Test
    fun doMTableTest() {
        /**
         * E  -> T E′
         * E′ -> + T E′ | ε
         * T  -> F T′
         * T′ -> * F T′ | ε
         * F -> ( E ) | id
         */
        val grammar = Grammar(
            terminals = setOf("id", "(", ")", "*", "+"),
            nonTerminals = setOf("E", "E′", "T", "T′", "F"),
            productions = mapOf(
                "E" to listOf(listOf("T", "E′")),
                "E′" to listOf(listOf("+", "T", "E′"), listOf("ε")),
                "T" to listOf(listOf("F", "T′")),
                "T′" to listOf(listOf("*", "F", "T′"), listOf("ε")),
                "F" to listOf(listOf("(", "E", ")"), listOf("id"))
            ),
            startProduction = "E"
        )

        val mTable = grammar.getMTable()

        Assertions.assertTrue(mTable.getValue("E").getValue("id") == "E->TE′")
        Assertions.assertTrue(mTable.getValue("E").getValue("(") == "E->TE′")
        Assertions.assertTrue(mTable.getValue("E").size == 2)

        Assertions.assertTrue(mTable.getValue("E′").getValue("+") == "E′->+TE′")
        Assertions.assertTrue(mTable.getValue("E′").getValue(")") == "E′->ε")
        Assertions.assertTrue(mTable.getValue("E′").getValue("$") == "E′->ε")
        Assertions.assertTrue(mTable.getValue("E′").size == 3)

        Assertions.assertTrue(mTable.getValue("T").getValue("id") == "T->FT′")
        Assertions.assertTrue(mTable.getValue("T").getValue("(") == "T->FT′")
        Assertions.assertTrue(mTable.getValue("T").size == 2)

        Assertions.assertTrue(mTable.getValue("T′").getValue("+") == "T′->ε")
        Assertions.assertTrue(mTable.getValue("T′").getValue("*") == "T′->*FT′")
        Assertions.assertTrue(mTable.getValue("T′").getValue(")") == "T′->ε")
        Assertions.assertTrue(mTable.getValue("T′").getValue("$") == "T′->ε")
        Assertions.assertTrue(mTable.getValue("T′").size == 4)

        Assertions.assertTrue(mTable.getValue("F").getValue("id") == "F->id")
        Assertions.assertTrue(mTable.getValue("F").getValue("(") == "F->(E)")
        Assertions.assertTrue(mTable.getValue("F").size == 2)

        Assertions.assertTrue(mTable.size == 5)
    }
}
