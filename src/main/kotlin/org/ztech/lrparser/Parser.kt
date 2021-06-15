package org.ztech.lrparser

import kotlin.system.exitProcess

/**
 * Данный файл должен в будущем генерироваться
 */


// Расширенное множество терминальных символов
val TERMINAL = listOf('a', 'b', 'c', 'd', 'e', null)

// Множество нетерминальных симовлов
val NON_TERMINAL = listOf('S', 'A', 'B', 'C')

// Множество продукций
val PRODUCTION = mapOf(
    'S' to listOf("ab", "bAc", "cdBCa"),
    'A' to listOf("bAc", "B"),
    'B' to listOf("dC", null, "Ae"),
    'C' to listOf("aCb", "d", null)
)

// Стартовая продукция
const val START_PRODUCTION = 'S'

/**
 * Генератор потока токенов
 */
interface ITokenizer : PeekIterator<Char> {

    /**
     * Функция обработки ошибки.
     * @param errorToken - токен, недопустимый в данной ситуации (null - в случае eof)
     * @param
     */
    open fun processError(errorToken: Char?, expected: List<Char>)
}

// Список символов, с которых могут начинаться терминальные символы
val NON_TERMINAL_STARTS = mapOf<Char, MutableSet<Char?>>(
    'S' to mutableSetOf(),
    'A' to mutableSetOf(),
    'B' to mutableSetOf(),
    'C' to mutableSetOf(),
)

data class Position(
    val nTerm: Char, // Продукция (нетерминальный символ)
    val production: Int, // Номер продукции данного терминала
    val position: Int = 0 // Номер положения курсора. Используется при обработке.
)

/**
 * Карта соответствия
 * Нетерминал ->  { допустимый символ to стек продукций до данного символа
 *                 .......
 *                }
 * Используется при аналезе. Позволяет определить список допустимых символов и
 * необходимые продукции для достижения данного символа.
 */

val PRODUCTION_MAP = mutableMapOf<Char, LinkedHashMap<Char, Stack<Position>>>()


/**
 * Функция заполнения PRODUCTION_MAP.
 */
fun initProductionMap() {
    for(prod in NON_TERMINAL) {
        val productionMap = mutableMapOf<Char, LinkedHashMap<Char, Stack<Position>>>()
        val processed = mutableListOf<Char>()
        checkNonTerminals(prod, processed, productionMap)
        PRODUCTION_MAP[prod] = productionMap[prod]!!
    }
}


/**
 * Проходимся по  всем продукциям данного нетерминала
 *    если продукция начинается с терминального символа - добавляем запись о том, что данный терминал достижим
 *                   из исходного нетерминала по продукции с номером
 *    если продукция начинается с нетерминального символа - делаем рекурсивное обращение к продукциям
 *                   нетерминального символа
 */
fun checkNonTerminals(nTerm: Char, processed: Stack<Char>, productionMap: MutableMap<Char, LinkedHashMap<Char, Stack<Position>>>) {
    val productions = PRODUCTION[nTerm]!!
    for ((index, prod) in productions.withIndex()) {
        when (val first = prod?.first()) {
            in TERMINAL -> {
                if (first == null) continue
                // Добавляем запись о том, что терминал достижим из нетерминала nTerm из продукции index
                val productionStack = productionMap.getOrPut(nTerm) {
                    linkedMapOf()
                }.getOrPut(first) {
                    mutableListOf()
                }
                if (productionStack.isNotEmpty()) {
                    println(
                        "Терминал $first достижим из нетерминала $nTerm по продукции $index и по продукции " +
                                "${productionStack.first().production}.\nПриведите грамматику к виду LR(1)."
                    )
                    exitProcess(1) // Завершение процесса
                }
                productionStack.add(
                    Position(
                        nTerm = nTerm,
                        production = index
                    )
                )
            }
            in NON_TERMINAL -> {
                // Избегаем рекурсии типа
                //       A->B...
                //       B->A...
                if (!processed.contains(first)) {
                    // Делаем новую копию
                    val stack = mutableListOf<Char>()
                    stack.addAll(processed)
                    stack.add(nTerm)
                    checkNonTerminals(first!!, stack, productionMap)

                    // Обработали нетерминал first.
                    // Если карта продукций для first не пуста
                    productionMap[first]?.let {
                        for (entry in it.entries) {
                            // it имеет тип LinkedHashMap<Char, Stack<Position>>
                            val productionStack = productionMap.getOrPut(nTerm) {
                                linkedMapOf()
                            }.getOrPut(entry.key) {
                                mutableListOf()
                            }
                            if (productionStack.isNotEmpty()) {
                                println(
                                    "Терминал ${entry.key} достижим из нетерминала $nTerm по продукции $index и по продукции " +
                                            "${productionStack.first().production}.\nПриведите грамматику к виду LR(1)."
                                )
                                exitProcess(1) // Завершение процесса
                            }
                            productionStack.add(
                                Position(
                                    nTerm = nTerm,
                                    production = index
                                )
                            )
                            productionStack.addAll(entry.value)
                        }
                    }
                }
            }
        }
    }
}


/**
 * Функция обработки множеств TERMINAL, NON_TERMINAL, PRODUCTION для генерации множеств символов,
 * с которых могут начинаться нетерминальные символы
 */
fun getNonTerminals(nTerm: Char): Set<Char?> {
    val productions = PRODUCTION[nTerm]!!
    val result = mutableSetOf<Char?>()

    for (prod in productions) {
        when (val first = prod?.first()) {
            in TERMINAL -> result.add(first)
            in NON_TERMINAL -> if (NON_TERMINAL_STARTS[first]!!.isEmpty()) {
                result.addAll(getNonTerminals(first!!))
            } else {
                result.addAll(NON_TERMINAL_STARTS[first]!!)
            }
        }
    }
    NON_TERMINAL_STARTS[nTerm]!!.addAll(result)
    return result
}










