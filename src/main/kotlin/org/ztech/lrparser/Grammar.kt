package org.ztech.lrparser

import kotlin.Exception

/**
 * Класс, определяющий грамматику
 */
class Grammar(
    /**
     * Список терминальных символов. Допустимые значения a..z
     */
    val terminals: Set<Char>,

    /**
     * Список нетерминальных символов. Допустимые значения A..Z
     */
    val nonTerminals: Set<Char>,

    /**
     * Множество продукций, выводимых из данного нетерминала. Для продукций вида
     * A->ε (пустой символ) добавляется пустая строка.
     */
    val productions: Map<Char, Set<String>>,

    /**
     * Стартовая продукция
     */
    val startProduction: Char
) {

    companion object {
        /**
         * Символ, используемый в функциях first и follow для представления пустого символа
         */
        const val EPSILON: Char = 'ε'

        /**
         * Маркер конца строки, применяемый для описания конца строки в функции follow
         */
        const val EOF: Char = '$'
    }

    /**
     * Отображение first(γ) - Множество терминалов (включая ε), с которых может начинаться любой
     * (терминальный или нетерминальный) символ γ.
     */
    private val first = mutableMapOf<Char, MutableSet<Char>>()

    /**
     * @see #first
     */
    fun getFirst(): Map<Char, Set<Char>> = first.toMap()

    /**
     * Отображение follow(A) - Множество терминалов (включая ε и $), которые могут следовать непосредственно
     * за нетерминальным символом A
     */
    private val follow = mutableMapOf<Char, MutableSet<Char>>()

    /**
     * @see #follow
     */
    fun getFollow(): Map<Char, Set<Char>> = follow.toMap()

    /**
     * Таблица синтаксического разбора
     */
    private val mTable = mutableMapOf<Char, MutableMap<Char, String>>()

    /**
     * @see #mTable
     */
    fun getMTable(): Map<Char, Map<Char, String>> = mTable.toMap()

    init {
        // Проверяем терминальные символы
        if (terminals.isEmpty()) throw Exception("Список терминальных символов пустой")

        if (terminals.any { it !in 'a'..'z' })
            throw Exception(
                "Список терминальных символов должен содержать только латинские " +
                    "символы в нижнем регистре."
            )

        // Проверяем нетерминальные символы
        if (nonTerminals.isEmpty()) throw Exception("Список нетерминальных символов пустой")
        if (nonTerminals.any { it !in 'A'..'Z' })
            throw Exception(
                "Список нетерминальных символов должен содержать только латинские " +
                    "символы в верхнем регистре."
            )

        // Проверим что для каждого нетерминального символа имеется продукция
        nonTerminals.forEach {
            if (it !in productions.keys || productions[it]!!.isEmpty()) {
                throw Exception("Для нетерминального символа $it не определено ни одной продукции.")
            }
        }

        // Проверяем продукции.
        productions.forEach { entry ->
            // Проверим левую часть продукции
            if (entry.key !in nonTerminals) throw Exception(
                "Левая часть продукции должна содержать только нетерминальные символы. " +
                    "${entry.key}"
            )

            // Правая часть продукций должна состоять только из символов грамматики
            for (rightSidesSet in entry.value) {
                rightSidesSet.forEach {
                    if (it !in terminals && it !in nonTerminals && it != EPSILON) {
                        throw Exception(
                            "Найдена продукция ${entry.key} -> $it содержащая символы, не относящиеся к грамматике"
                        )
                    }
                }
            }
        }

        // Инициализируем множество first
        initFirst()

        // Инициализация множества follow
        initFollow()

        // Инициализация таблицы разбора
        initMTable()
    }

    /**
     * Функция построения таблицы предиктивного синтаксического анализа
     * см Ахо, Сети, Ульман Компиляторы. Принципы, технологии, инструменты. 2ed. 2008
     */
    private fun initMTable() {

        // Для каждой продукции грамматики A -> α выполним:
        productions.forEach { (A, prodsOfA) ->
            prodsOfA.forEach { prod ->
                val alphaFirst = first[prod.first()] ?: listOf(EPSILON)

                // Для каждого терминала a из FIRST(α) добавим A -> α в M[A, a]
                val recordOfA = mTable.getOrDefault(A, mutableMapOf())
                alphaFirst.forEach { a ->
                    recordOfA[a] = "$A->$prod"
                }

                // Если ε присутствует в FIRST(α), то для каждого b из FOLLOW(A) добавляем  A -> α в M[A, b]
                val followA = follow.getValue(A)

                if (EPSILON in alphaFirst) {
                    followA.forEach { b ->
                        recordOfA[b] = "$A->$prod"
                    }
                }

                // Если ε присутствует в FIRST(α) и $ присутствует в FOLLOW(A), то добавляем A -> α в M[A, $]
                if (EPSILON in alphaFirst && EOF in followA) {
                    recordOfA[EOF] = "$A->$prod"
                }
                mTable[A] = recordOfA
            }
        }
    }

    /**
     * Функция инициализации множества first.
     * см. Ахо, Сети, Ульман Компиляторы. Принципы, технологии, инструменты. 2ed. 2008
     */
    private fun initFirst() {
        var next = true

        // 1. first от терминального символа содержит только себя
        terminals.forEach { first[it] = mutableSetOf(it) }

        // Цикл до тех пор, пока ни к одному из множеств first не смогут быть добавлены ни терминалы ни ε
        while (next) {
            next = false

            // Проходимся по всем нетерминалам
            nonTerminals.forEach each@{ nonTerminal ->

                // Заполняем first для данного нетерминала
                val nTermFirst = first.getOrDefault(nonTerminal, mutableSetOf())

                // Проходимся по всем продукциям, определенным для данного нетерминала
                val productionSet = productions.getValue(nonTerminal)

                productionSet.forEach prod@{ production ->

                    // 2. Двигаемся по продукциям. Если нашли продукцию вида X->ABC, проходимся по всем символам продукции
                    var addEpsilon = true
                    production.forEach { symbolInProduction ->

                        // Проверяем first(symbolInProduction)
                        first[symbolInProduction]?.also {
                            // Если first[symbolInProduction] не пустой, добавляем все элементы к nTermFirst
                            // если список изменился - устанавливаем флаг next
                            it.filterNot { ch -> ch == EPSILON }.forEach { ch ->
                                if (!nTermFirst.contains(ch)) {
                                    nTermFirst += ch
                                    next = true
                                }
                            }

                            // Переходить к следующему символу в продукции ABC можно только если ε содержится
                            // в first[ch]
                            if (!it.contains(EPSILON)) {
                                addEpsilon = false
                                return@prod
                            }
                        } ?: run {
                            // Если follow[symbolInProduction] не определен, проверять дальше не имеет смысла
                            if (symbolInProduction != EPSILON) {
                                addEpsilon = false
                                return@prod
                            }
                        }
                    }
                    // 3. Если нашли продукцию вида X->ε и ε не принадлежит first(X) добавляем ε к first(X)
                    // (либо пройдя по непустой продукции X -> ABC обнаружили, что ε присутствует и в first(A) и в
                    //  first(B) и в first(C))
                    if ((production == EPSILON.toString() || addEpsilon) && !nTermFirst.contains(EPSILON)) {
                        nTermFirst += EPSILON
                        next = true
                    }
                }

                first[nonTerminal] = nTermFirst
            }
        }
    }

    /**
     * Функция инициализации множества follow
     * см. Ахо, Сети, Ульман Компиляторы. Принципы, технологии, инструменты. 2ed. 2008
     */
    private fun initFollow() {

        // 1. Поместим $ в FOLLOW(S)
        val nTermFollow = follow.getOrDefault(startProduction, mutableSetOf())
        nTermFollow += EOF
        follow[startProduction] = nTermFollow

        do {
            var changed = false
            nonTerminals.forEach { A ->
                run {
                    val aProduction = productions.getValue(A)
                    // Проверяем каждую продукцию.
                    aProduction.forEach { prod ->
                        // Просматриваем продукцию и находим нетерминальные символы
                        prod.forEachIndexed { ind, B ->
                            if (B in nonTerminals) {
                                val followB = follow.getOrDefault(B, mutableSetOf())

                                if (ind < prod.length - 1) {
                                    // запоминаем размер до
                                    val sizeBefore = followB.size

                                    // добавляем все элементы FIRST(β) кроме ε в множество FOLLOW(B)
                                    val firstBetta = first.getValue(prod[ind + 1])

                                    // 2. Если имеется продукция вида A -> αBβ, то все элементы множества FIRST(β)
                                    // кроме ε, помещаются в множество FOLLOW(B)
                                    followB.addAll(firstBetta.filterNot { it == EPSILON })

                                    // 3. Если имеется продукция вида A -> αBβ, где FIRST(β) содержит ε,
                                    // то все элементы множества FOLLOW(A) помещаются в множество FOLLOW(B)
                                    if (EPSILON in firstBetta) {
                                        followB.addAll(follow.getOrDefault(A, mutableSetOf()))
                                    }

                                    // проверяем размер после; если размеры различаются - значит были добавления
                                    if (sizeBefore != followB.size) changed = true
                                }
                                // 3. Если имеется продукция вида A -> αB, то все элементы множества FOLLOW(A)
                                // помещаются в множество FOLLOW(B)
                                if (ind == prod.length - 1) {
                                    // Случай A -> αB

                                    // запоминаем размер до
                                    val sizeBefore = followB.size

                                    // добавляем элементы множества FOLLOW(A)
                                    followB.addAll(follow.getOrDefault(A, mutableSetOf()))

                                    // проверяем размер после; если размеры различаются - значит были добавления
                                    if (sizeBefore != followB.size) changed = true
                                }
                                // Если были изменения - надо актуализировать отображение follow
                                if (changed) follow[B] = followB
                            }
                        }
                    }
                }
            }
        } while (changed)
    }
}

/*
    init {
        // Проверяем терминальные символы
        if (terminals.isEmpty()) throw Exception("Список терминальных символов пустой")
        if (terminals.any{ it !in 'a' .. 'z' })
            throw Exception("Список терминальных символов должен содержать только латинские символы в нижнем регистре")

        // Проверяем нетерминальные символы
        if (nonTerminals.isEmpty()) throw Exception("Список нетерминальных символов пустой")
        if (nonTerminals.any{ it !in 'A' .. 'Z' })
            throw Exception("Список нетерминальных символов должен содержать только латинские символы в верхнем регистре")

        if (startProduction !in nonTerminals)
            throw Exception("Начальная продукция должна присутствовать в списке нетерминальных символов")


        // Заполнение productionMap
        initProductionMap()

        // Заполнение nullableTerminals
        initNullableList()
    }





    /**
     * Проходимся по  всем продукциям данного нетерминала
     *    если продукция начинается с терминального символа - добавляем запись о том, что данный терминал достижим
     *                   из исходного нетерминала по продукции с номером
     *    если продукция начинается с нетерминального символа - делаем рекурсивное обращение к продукциям
     *                   нетерминального символа
     */
    private fun checkNonTerminals(
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
        productionStackMap: MutableMap<Char, LinkedHashMap<Char, Stack<Production>>>

    ) {
        for ((index, prod) in productions[nTerm]!!.withIndex()) {
            when (val first = prod?.first()) {
                in terminals -> {
                    if (first == null) continue
                    // Добавляем запись о том, что терминал достижим из нетерминала nTerm из продукции index
                    val productionStack = productionStackMap.getOrPut(nTerm) {
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
                in nonTerminals -> {
                    // Избегаем рекурсии типа
                    //       A->B...
                    //       B->A...
                    if (!processed.contains(first)) {
                        // Делаем новую копию
                        val stack = mutableListOf<Char>()
                        stack.addAll(processed)
                        stack.add(nTerm)
                        checkNonTerminals(first!!, stack, productionStackMap)

                        // Обработали нетерминал first.
                        // Если карта продукций для first не пуста
                        productionStackMap[first]?.let {
                            for (entry in it.entries) {
                                // it имеет тип LinkedHashMap<Char, Stack<Position>>
                                val productionStack = productionStackMap.getOrPut(nTerm) {
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
     * Функция заполнения productionMap.
     */
    private fun initProductionMap() {
        for (prod in nonTerminals) {
            val tempMap = mutableMapOf<Char, LinkedHashMap<Char, Stack<Production>>>()
            val processed = mutableListOf<Char>()
            checkNonTerminals(prod, processed, tempMap)
            productionMap[prod] = tempMap[prod]!!
        }
    }

    /**
     * Функция заполнения nullableTerminals
     */
    private fun initNullableList() {
        // Цикл до тех пор, пока не проверили все
        while(true) {
            var newNTermFound = false
            loop@ for (nterm in nonTerminals) {
                // Если нетерминал уже в списке - пропускаем его
                if (nterm in nullableTerminals) continue
                val productions = productions[nterm]!!

                // Имеется продукция вида A -> null
                if (null in productions) {
                    nullableTerminals += nterm
                    newNTermFound = true
                    // дальше не интересно
                    break@loop
                }

                for (nullterm in nullableTerminals) {
                    // Если имеется продукция вида A->B, и B уже в NULLABLE_LIST
                    if (productions.contains(nullterm.toString())) {
                        nullableTerminals +=nterm
                        newNTermFound = true
                        // дальше не интересно
                        break@loop
                    }
                }
            }

            if (!newNTermFound) {
                // Больше ничего не нашли. Можно выходить.
                return
            }
        }
    }

    /**
     * Основная функция разбора. Вызывается после инициализации.
     */
    fun parse(inputStack: Stack<Char>) {

        this.productionStack.clear()
        this.acceptedStack.clear()

        while (true) {
            if (inputStack.isEmpty()) {
                if (productionStack.isEmpty()) {
                    if (acceptedStack.size == 1 && acceptedStack.peek() == startProduction) {
                        // Выход. Все отлично.
                        return
                    }

                    // Ошибка разбора. Скорее всего ошибка работы парсера.
                    throw Exception(
                        "Ошибка разбора. Входной поток пуст. Стек продукций пуст. " +
                                "Стек допущенных терминалов и нетерминалов: $acceptedStack"
                    )
                }

                // PRODUCTION_STACK не пустой
                // Возможно сделать сверту по текущей продукции
                val currentProduction = productionStack.peek()!!

                // Если позиция текущей продукции последняя - делаем свертку
                if (currentProduction.position == productions[currentProduction.nTerm]!![currentProduction.production]!!.length) {
                    // Делаем свертку
                    // TODO вынести в отдельную функцию
                    productionStack.pop()

                    // Заменяем разобранную подтсроку на терминал
                    repeat(currentProduction.position) { acceptedStack.pop() }
                    acceptedStack.push(currentProduction.nTerm)
                    if (productionStack.size>0) productionStack.peek()!!.position++
                    continue
                }

                // Скорее всего ошибка разбора xml.
                // Определяем список допустимых терминалов.
                val currentSymbol =
                    productions[currentProduction.nTerm]!![currentProduction.production]!![currentProduction.position]

                val acceptedSymbols = mutableSetOf<Char>()
                if (currentSymbol in terminals) {
                    // Допустимым может быть только один терминал
                    acceptedSymbols.add(currentSymbol)
                } else {
                    // currentSymbol - Нетерминал

                    // Возможны два случая:
                    // Если имеется продукция $currentSymbol в списке NULLABLE_LIST, то ситуация допустимая, в противном случае - ошибка
                    if (currentSymbol in nullableTerminals){
                        // Записываем нетерминал
                        productionStack.peek()!!.position++
                        acceptedStack.push(currentSymbol)
                        continue
                    }

                    // Если продукции $currentSymbol -> null то ситуация не допустимая.
                    // Допустимыми могут быть все терминалы, с которых может начинаться данный нетерминал.
                    acceptedSymbols.addAll(productionMap[currentSymbol]!!.keys)
                }
                throw Exception("Ошибка разбора xml. Входной поток пуст. Ожидаемые значения: $acceptedSymbols")
            } else {
                // Оцениваем очередной символ
                val nextChar = inputStack.peek()

                if (productionStack.isEmpty()) {
                    // Нужно выбрать правильную продукцию из продукци
                    val accepted = productionMap[startProduction]!!.keys
                    if (nextChar !in accepted){
                        // Начальная продукация не может начинаться с символа $nextChar
                        throw Exception("Ошибка разбора xml. Начальная продукция не может начинаться с символа $nextChar." +
                                " Ожидаемые значения: $accepted")
                    }

                    // Добавляем продукции, которые приведут к данному символу
                    productionMap[startProduction]!![nextChar]!!.forEach { productionStack.push(it) }
                }

                val currentProduction = productionStack.peek()!!

                // Если позиция текущей продукции последняя - делаем свертку
                if (currentProduction.position == productions[currentProduction.nTerm]!![currentProduction.production]!!.length) {
                    // Делаем свертку
                    // TODO вынести в отдельную функцию
                    productionStack.pop()

                    // Заменяем разобранную подтсроку на терминал
                    repeat(currentProduction.position) { acceptedStack.pop() }
                    acceptedStack.push(currentProduction.nTerm)
                    productionStack.peek()!!.position++
                    continue
                }

                // Определяем список допустимых терминалов.
                val currentSymbol =
                    productions[currentProduction.nTerm]!![currentProduction.production]!![currentProduction.position]

                if (currentSymbol in terminals) {
                    // Допустимым может быть только один терминал
                    if (currentSymbol == nextChar) {
                        // Все хорошо, идем дальше
                        currentProduction.position ++
                        acceptedStack.push(nextChar)
                        inputStack.pop()
                        continue
                    } else {
                        // Ошибка разбора xml
                        throw Exception("Ошибка разбора xml. Допустимый символ $currentSymbol, ожидается $nextChar.")
                    }
                } else {
                    // currentSymbol - Нетерминал

                    // Нужно выбрать правильную продукцию из продукци
                    val accepted = productionMap[currentSymbol]!!.keys
                    if (nextChar in accepted) {
                        // Допускаем и идем дальше
                        // Добавляем продукции, которые приведут к данному символу
                        productionMap[startProduction]!![nextChar]!!.forEach { productionStack.push(it) }
                        productionStack.peek()!!.position++
                        acceptedStack.push(nextChar!!)
                        inputStack.pop()
                        continue
                    }

                    // Следующий символ не допустим.
                    // Если имеется продукция $currentSymbol в списке NULLABLE_LIST, то ситуация допустимая, в противном случае - ошибка
                    if (currentSymbol in nullableTerminals) {
                        // Записываем нетерминал
                        productionStack.peek()!!.position++
                        acceptedStack.push(currentSymbol)
                        continue
                    } else {
                        // Ошибка разбора xml
                        throw Exception("Ошибка разбора xml. Ожидаемые значения: $accepted, найден $nextChar")
                    }
                }
            }
        }
    }

}

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

// Стартовая продукция
//const val START_PRODUCTION = 'S'

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

//
///**
// * Карта соответствия
// * Нетерминал ->  { допустимый символ to стек продукций до данного символа
// *                 .......
// *                }
// * Используется при аналезе. Позволяет определить список допустимых символов в данном положении и
// * необходимые продукции для достижения данного символа.
// */
//val PRODUCTION_MAP = mutableMapOf<Char, LinkedHashMap<Char, Stack<Production>>>()
//
///**
// * Список нуллабельных нетерминалов.
// * (Нетерминальных символов, которые могут быть выведены из null:
// *   то есть
// *     A -> null, либо рекурсии вида
// *     A -> B
// *     B -> null)
// *
// */
//val NULLABLE_LIST = mutableListOf<Char>()
//
//
///**
// * Стек позиций. Начинаем со стартовой позиции
// */
//val PRODUCTION_STACK = mutableListOf<Production>()
//
///**
// * Стек допущенных(разобранных) терминалов и нетерминалов
// */
//val ACCEPTED_STACK = mutableListOf<Char>()
//
*/
