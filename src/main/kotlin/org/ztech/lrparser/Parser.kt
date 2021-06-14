package org.ztech.lrparser

/**
 * Данный файл должен в будущем генерироваться
 */


// Расширенное множество терминальных символов
val TERMINAL = listOf('a','b','c', 'd', null)

// Множество нетерминальных симовлов
val NON_TERMINAL = listOf('S', 'A', 'B', 'C')

// Множество продукций
val PRODUCTION = mapOf(
    'S' to listOf("ab","bAc", "cdBCa"),
    'A' to listOf("bAc", "B"),
    'B' to listOf("dC", null),
    'C' to listOf("aCb","d", null)
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

/**
 * Функция обработки множеств TERMINAL, NON_TERMINAL, PRODUCTION для генерации множеств символов,
 * с которых могут начинаться нетерминальные символы
 */
fun getNonTerminals(nTerm: Char): Set<Char?> {
    val productions = PRODUCTION[nTerm]!!
    val result = mutableSetOf<Char?>()

    for(prod in productions) {
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










