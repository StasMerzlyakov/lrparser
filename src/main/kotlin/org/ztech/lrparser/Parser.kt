package org.ztech.lrparser

import java.lang.Exception
import kotlin.system.exitProcess

/**
 * Данный файл должен в будущем генерироваться
 */

// Расширенное множество терминальных символов (с null)
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
 * Входные данные
 */
val INPUT_STACK = mutableListOf<Char>()

/**
 * Основная функция разбора. Вызывается после инициализации.
 */
fun parse() {
    while (true) {
        if (INPUT_STACK.isEmpty()) {
            if (PRODUCTION_STACK.isEmpty()) {
                if (ACCEPTED_STACK.size == 1 && ACCEPTED_STACK.peek() == START_PRODUCTION) {
                    // Выход. Все отлично.
                    return
                }

                // Ошибка разбора. Скорее всего ошибка работы парсера.
                throw Exception(
                    "Ошибка разбора. Входной поток пуст. Стек продукций пуст. " +
                            "Стек допущенных терминалов и нетерминалов: $ACCEPTED_STACK"
                )
            }

            // PRODUCTION_STACK не пустой
            // Возможно сделать сверту по текущей продукции
            val currentProduction = PRODUCTION_STACK.peek()!!

            // Если позиция текущей продукции последняя - делаем свертку
            if (currentProduction.position == PRODUCTION[currentProduction.nTerm]!![currentProduction.production]!!.length) {
                // Делаем свертку
                // TODO вынести в отдельную функцию
                PRODUCTION_STACK.pop()

                // Заменяем разобранную подтсроку на терминал
                repeat(currentProduction.position) { ACCEPTED_STACK.pop() }
                ACCEPTED_STACK.push(currentProduction.nTerm)
                if (PRODUCTION_STACK.size>0) PRODUCTION_STACK.peek()!!.position++
                continue
            }

            // Скорее всего ошибка разбора xml.
            // Определяем список допустимых терминалов.
            val currentSymbol =
                PRODUCTION[currentProduction.nTerm]!![currentProduction.production]!![currentProduction.position]

            val acceptedSymbols = mutableSetOf<Char>()
            if (currentSymbol in TERMINAL) {
                // Допустимым может быть только один терминал
                acceptedSymbols.add(currentSymbol)
            } else {
                // currentSymbol - Нетерминал

                // Возможны два случая:
                // Если имеется продукция $currentSymbol -> null, то ситуация допустимая, в противном случае - ошибка
                if (PRODUCTION[currentSymbol]!!.contains(null)) {
                    // Записываем нетерминал
                    PRODUCTION_STACK.peek()!!.position++
                    ACCEPTED_STACK.push(currentSymbol)
                    continue
                }

                // Если продукции $currentSymbol -> null то ситуация не допустимая.
                // Допустимыми могут быть все терминалы, с которых может начинаться данный нетерминал.
                acceptedSymbols.addAll(PRODUCTION_MAP[currentSymbol]!!.keys)
            }
            throw Exception("Ошибка разбора xml. Входной поток пуст. Ожидаемые значения: $acceptedSymbols")
        } else {
            // Оцениваем очередной символ
            val nextChar = INPUT_STACK.peek()

            if (PRODUCTION_STACK.isEmpty()) {
                // Нужно выбрать правильную продукцию из продукци
                val accepted = PRODUCTION_MAP[START_PRODUCTION]!!.keys
                if (nextChar !in accepted){
                    // Начальная продукация не может начинаться с символа $nextChar
                    throw Exception("Ошибка разбора xml. Начальная продукция не может начинаться с символа $nextChar." +
                            " Ожидаемые значения: $accepted")
                }

                // Добавляем продукции, которые приведут к данному символу
                PRODUCTION_MAP[START_PRODUCTION]!![nextChar]!!.forEach { PRODUCTION_STACK.push(it) }
            }

            val currentProduction = PRODUCTION_STACK.peek()!!

            // Если позиция текущей продукции последняя - делаем свертку
            if (currentProduction.position == PRODUCTION[currentProduction.nTerm]!![currentProduction.production]!!.length) {
                // Делаем свертку
                // TODO вынести в отдельную функцию
                PRODUCTION_STACK.pop()

                // Заменяем разобранную подтсроку на терминал
                repeat(currentProduction.position) { ACCEPTED_STACK.pop() }
                ACCEPTED_STACK.push(currentProduction.nTerm)
                PRODUCTION_STACK.peek()!!.position++
                continue
            }

            // Определяем список допустимых терминалов.
            val currentSymbol =
                PRODUCTION[currentProduction.nTerm]!![currentProduction.production]!![currentProduction.position]

            if (currentSymbol in TERMINAL) {
                // Допустимым может быть только один терминал
                if (currentSymbol == nextChar) {
                    // Все хорошо, идем дальше
                    currentProduction.position ++
                    ACCEPTED_STACK.push(nextChar)
                    INPUT_STACK.pop()
                    continue
                } else {
                    // Ошибка разбора xml
                    throw Exception("Ошибка разбора xml. Допустимый символ $currentSymbol, ожидается $nextChar.")
                }
            } else {
                // currentSymbol - Нетерминал

                // Нужно выбрать правильную продукцию из продукци
                val accepted = PRODUCTION_MAP[currentSymbol]!!.keys
                if (nextChar in accepted) {
                    // Допускаем и идем дальше
                    // Добавляем продукции, которые приведут к данному символу
                    PRODUCTION_MAP[START_PRODUCTION]!![nextChar]!!.forEach { PRODUCTION_STACK.push(it) }
                    PRODUCTION_STACK.peek()!!.position++
                    ACCEPTED_STACK.push(nextChar!!)
                    INPUT_STACK.pop()
                    continue
                }

                // Следующий символ не допустим.
                // Если имеется продукция $currentSymbol -> null, то ситуация допустимая, в противном случае - ошибка
                if (PRODUCTION[currentSymbol]!!.contains(null)) {
                    // Записываем нетерминал
                    PRODUCTION_STACK.peek()!!.position++
                    ACCEPTED_STACK.push(currentSymbol)
                    continue
                } else {
                    // Ошибка разбора xml
                    throw Exception("Ошибка разбора xml. Ожидаемые значения: $accepted, найден $nextChar")
                }
            }
        }
    }
}
