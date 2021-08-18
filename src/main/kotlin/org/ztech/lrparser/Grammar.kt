package org.ztech.lrparser

/**
 * Класс, определяющий грамматику
 */
class Grammar(
    /**
     * Список терминальных символов.
     */
    val terminals: Set<String>,

    /**
     * Список нетерминальных символов.
     */
    val nonTerminals: Set<String>,

    /**
     * Множество продукций, выводимых из данного нетерминала. Для продукций вида
     * A->ε (пустой символ) добавляется пустая строка.
     */
    val productions: Map<String, List<List<String>>>,

    /**
     * Стартовая продукция
     */
    val startProduction: String
) {

    companion object {
        /**
         * Символ, используемый в функциях first и follow для представления пустого символа
         */
        const val EPSILON: String = "ε"

        /**
         * Маркер конца строки, применяемый для описания конца строки в функции follow
         */
        const val EOF: String = "$"
    }

    /**
     * Отображение first(γ) - Множество терминалов (включая ε), с которых может начинаться любой
     * (терминальный или нетерминальный) символ γ.
     */
    private val first = mutableMapOf<String, MutableSet<String>>()

    /**
     * @see #first
     */
    fun getFirst(): Map<String, Set<String>> = first.toMap()

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
        require(terminals.isNotEmpty()) { "Список терминальных символов пустой" }

        // Проверяем что терминальные символы не содержат символов EPSILON и EOF
        require(!terminals.contains(EPSILON)) { "Список терминальных символов содержит строку $EPSILON" }
        require(!terminals.contains(EOF)) { "Список терминальных символов содержит строку $EOF" }

        // Проверяем нетерминальные символы
        require(terminals.isNotEmpty()) { "Список нетерминальных символов пустой" }
        require(!terminals.contains(EPSILON)) { "Список нетерминальных символов содержит строку $EPSILON" }
        require(!terminals.contains(EOF)) { "Список нетерминальных символов содержит строку $EOF" }

        // Проверяем что нет пересечений между терминальными и нетерминальными символами
        require((terminals intersect nonTerminals).isEmpty()) {
            "Списки терминальный и нетерминальных символов не должны пересекаться"
        }

        nonTerminals.forEach {
            require(it in productions.keys) { "Для нетерминала $it не определено ни одной продукции" }
        }

        require(startProduction in nonTerminals) {
            "Стартовая продукция $startProduction должна присутствовать в списке нетерминалов"
        }

        // Проверяем продукции.
        productions.forEach { entry ->
            require(entry.key in nonTerminals) {
                "Левая честь продукции $entry не содержится в списке нетерминалов"
            }

            // Правая часть продукций должна состоять только из символов грамматики
            entry.value.forEach {
                production ->
                production.forEach {
                    require(it in terminals || it in nonTerminals || it == EPSILON && production.size == 1) {
                        "Правая часть продукции ${entry.key} -> $production " +
                            "должна содержать только символы грамматики или $EPSILON."
                    }
                }
            }
        }

        // Инициализируем множество first
        initFirst()

        /* // Инициализация множества follow
         initFollow()

         // Инициализация таблицы разбора
         initMTable() */
    }

    /**
     * Функция инициализации множества first.
     * см. Ахо, Сети, Ульман Компиляторы. Принципы, технологии, инструменты. 2ed. 2008
     */
    private fun initFirst() {

        // 1. first от терминального символа содержит только себя
        terminals.forEach { first[it] = mutableSetOf(it) }
        first[EPSILON] = mutableSetOf(EPSILON)

        // Цикл до тех пор, пока ни к одному из множеств first не смогут быть добавлены ни терминалы ни ε
        do {
            // Флаг, по которому будем выходить из цикла
            var isChanged = false

            // Проходимся по всем продукциям
            productions.forEach {
                entry ->
                // Заполняем first нетерминала entry.key
                val nTermFirst = first.getOrDefault(entry.key, mutableSetOf())

                // по данной переменной будем проверять - были ли изменения в first(entry.key)
                var nTermFirstSize = nTermFirst.size

                // Список продукций нетерминала entry.key
                val nTermProds = entry.value

                nTermProds.forEach prods@{ nTermProd ->
                    nTermProd.forEach { symbol ->

                        // если список не определён, просматривать дальше пока нет смысла
                        val firstA = first[symbol] ?: return@prods

                        // Двигаемся по продукциям. Если нашли продукцию вида X->ABC,
                        // к first(X) добавляем first(A); если ε содержится в first(A), переходим к B и т.д.

                        // добавляем все кроме символа EPSILON
                        nTermFirst.addAll(firstA)

                        if (EPSILON !in firstA) {
                            // переходить к B не надо
                            return@prods
                        }
                    }
                }

                if (nTermFirstSize != nTermFirst.size) {
                    // Обновляем first для нетерминала entry.key
                    first[entry.key] = nTermFirst
                    isChanged = true
                }
            }
        } while (isChanged)

/*
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
                    if ((production == EPSILON || addEpsilon) && !nTermFirst.contains(EPSILON)) {
                        nTermFirst += EPSILON
                        next = true
                    }
                }

                first[nonTerminal] = nTermFirst
            }
        } */
    }

    /*/**
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
    } */
}
