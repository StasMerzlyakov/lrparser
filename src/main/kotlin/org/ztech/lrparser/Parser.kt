package org.ztech.lrparser

import kotlin.system.exitProcess

/**
 * Данный файл должен в будущем генерироваться
 */


// Расширенное множество терминальных символов (с null)
val TERMINAL = setOf('a', 'b', 'c', 'd', 'e', null)

// Множество нетерминальных симовлов
val NON_TERMINAL = setOf('S', 'A', 'B', 'C')

// Множество продукций
val PRODUCTION = mapOf(
    'S' to setOf("ab", "bAc", "cdBCa"),
    'A' to setOf("bAc", "B"),
    'B' to setOf("dC", null, "Ae"),
    'C' to setOf("aCb", "d", null)
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

/**
 * Тип данных, используемый при разборе.
 */
data class Production(
    val nTerm: Char, // Продукция (нетерминальный символ)
    val production: Int, // Номер продукции данного терминала
    var position: Int = 0 // Номер положения курсора. Используется при обработке.
)

/**
 * Карта соответствия
 * Нетерминал ->  { допустимый символ to стек продукций до данного символа
 *                 .......
 *                }
 * Используется при аналезе. Позволяет определить список допустимых символов в данном положении и
 * необходимые продукции для достижения данного символа.
 */

val PRODUCTION_MAP = mutableMapOf<Char, LinkedHashMap<Char, Stack<Production>>>()

/**
 * Функция заполнения PRODUCTION_MAP.
 */
fun initProductionMap() {
    for (prod in NON_TERMINAL) {
        val productionMap = mutableMapOf<Char, LinkedHashMap<Char, Stack<Production>>>()
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
fun checkNonTerminals(
    /**
     * Проверяемый нетерминал
     */
    nTerm: Char,
    /**
     * Стек обработанных нетерминалов. Нужен для исключения рекурсии при обработке правил типа
     * A -> Bc
     * ...
     * B -> Ad
     */
    processed: Stack<Char>,
    /**
     * Формируемая карта продукций
     */
    productionMap: MutableMap<Char, LinkedHashMap<Char, Stack<Production>>>
) {
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
                    Production(
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
                                Production(
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
 * Стек позиций. Начинаем со стартовой позиции
 */
val PRODUCTION_STACK = mutableListOf<Production>()

/**
 * Стек допущенных(разобранных) терминалов и нетерминалов
 */
val ACCEPTED_STACK = mutableListOf<Char>()

/**
 * Входноые данные
 */
val INPUT_STACK = mutableListOf<Char>()














