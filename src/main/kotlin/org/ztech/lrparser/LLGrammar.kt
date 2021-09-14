package org.ztech.lrparser

/**
 * LL грамматика (нисходящий разбор).
 * Синтаксический анализатор, управляемый таблицей синтаксического анализа.
 * Ахо, Сети, Ульман. Компилляторы. Принципы, технологии, инструменты. 2ed.
 * (4.4.4 Нерекурсивный предиктивный синтаксический анализ)
 */
class LLGrammar(
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
) : IGrammar {

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
     * @see #initFirst
     */
    fun getFirst(): Map<String, Set<String>> = first.toMap()

    /**
     * Отображение follow(A) - Множество терминалов (включая ε и $), которые могут следовать непосредственно
     * за нетерминальным символом A
     */
    private val follow = mutableMapOf<String, MutableSet<String>>()

    /**
     * @see #initFirst
     */
    fun getFollow(): Map<String, Set<String>> = follow.toMap()

    /**
     * Таблица синтаксического разбора.
     */
    private val mTable = mutableMapOf<String, MutableMap<String, List<String>>>()

    /**
     * @see #initMTable
     */
    fun getMTable(): Map<String, Map<String, List<String>>> = mTable.toMap()

    init {
        // Проверяем терминальные символы
        require(terminals.isNotEmpty()) { "Список терминальных символов пустой" }

        // Проверяем что терминальные символы не содержат символов EPSILON и EOF
        require(!terminals.contains(EPSILON)) { "Список терминальных символов содержит строку $EPSILON" }
        require(!terminals.contains(EOF)) { "Список терминальных символов содержит строку $EOF" }

        // Проверяем нетерминальные символы
        require(nonTerminals.isNotEmpty()) { "Список нетерминальных символов пустой" }
        require(!nonTerminals.contains(EPSILON)) { "Список нетерминальных символов содержит строку $EPSILON" }
        require(!nonTerminals.contains(EOF)) { "Список нетерминальных символов содержит строку $EOF" }

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

        // Инициализация множества follow
        initFollow()

        // Инициализация таблицы разбора
        initMTable()
    }

    /**
     * Функция инициализации множества first.
     * см. 4.4.2 Функции FIRST и FOLLOW
     * Ахо, Сети, Ульман Компиляторы. Принципы, технологии, инструменты. 2ed. 2008
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
                val nTermFirstSize = nTermFirst.size

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
    }

    /**
     * Вспомогательная функция вычисления FIRST для продукции
     */
    fun first(prod: List<String>): Set<String> {
        val resultSet = mutableSetOf<String>()

        // Проходимся по всем элементам prod.
        prod.forEachIndexed {
            ind, element ->
            val elementFirst = first.getValue(element)
            resultSet.addAll(
                elementFirst.filter {
                    it != EPSILON
                }
            )
            if (!elementFirst.contains(EPSILON)) {
                // Дальше просматривать не нужно
                return resultSet
            } else {
                // Просмотрели все и ε входит в FOLLOW для всех элементов => ε надо добавить к результату
                if (prod.lastIndex == ind) resultSet.add(EPSILON)
            }
        }
        if (resultSet.isEmpty()) {
            resultSet.add(EPSILON)
        }
        return resultSet
    }

    /**
     * Функция построения таблицы предиктивного синтаксического анализа
     * см. 4.4.3 LL(1)-грамматики
     * Ахо, Сети, Ульман Компиляторы. Принципы, технологии, инструменты. 2ed. 2008
     */
    /**
     * Вспомогательная функция
     */
    private fun createMTableRule(term: String, prod: List<String>): List<String> {
        return if (prod.size == 1 && EPSILON in prod) {
            // Правило вида A -> ε
            listOf()
        } else {
            // Правило вида A -> abc
            prod
        }
    }

    private fun initMTable() {

        // Для каждой продукции грамматики A -> α выполним:
        productions.forEach { (A, prodsOfA) ->
            prodsOfA.forEach { prod ->

                // Для каждого терминала a из FIRST(α) добавим A->α в M[A, a]
                val prodFirst = first(prod)
                val recordOfA = mTable.getOrDefault(A, mutableMapOf())
                prodFirst.forEach { a ->
                    recordOfA[a] = createMTableRule(A, prod)
                }

                // Если ε присутствует в FIRST(α), то для каждого b из FOLLOW(A) добавляем  A->α в M[A, b]
                val followA = follow.getValue(A)

                if (EPSILON in prodFirst) {
                    followA.forEach { b ->
                        recordOfA[b] = createMTableRule(A, prod)
                    }
                }

                // Если ε присутствует в FIRST(α) и $ присутствует в FOLLOW(A), то добавляем A -> α в M[A, $]
                if (EPSILON in prodFirst && EOF in followA) {
                    recordOfA[EOF] = createMTableRule(A, prod)
                }

                // В таблице разбора оставим только терминалы
                recordOfA.remove(EPSILON)
                mTable[A] = recordOfA
            }
        }
    }

    /**
     * Функция инициализации множества follow
     * см. 4.4.2 Функции FIRST и FOLLOW
     * Ахо, Сети, Ульман Компиляторы. Принципы, технологии, инструменты. 2ed. 2008
     */
    private fun initFollow() {

        // 1. Поместим $ в FOLLOW(S)
        follow[startProduction] = mutableSetOf(EOF)

        // Цикл до тех пор, пока ни к одному из множеств follow не смогут быть добавлены ни терминалы ни $
        do {
            // Флаг, по которому будем выходить из цикла
            var isChanged = false

            // Проходимся по всем продукциям
            productions.forEach {
                entry ->

                // Для каждого нетерминала A
                val A = entry.key
                val followA = follow.getOrDefault(A, mutableSetOf())

                // Список продукций нетерминала entry.key
                val nTermProds = entry.value

                nTermProds.forEach {
                    // Просматриваем продукцию и находим нетерминальные символы
                    prod ->
                    prod.forEachIndexed { ind, B ->
                        if (B in nonTerminals) {
                            val followB = follow.getOrDefault(B, mutableSetOf())

                            // по данной переменной будем проверять - были ли изменения в follow(B)
                            val followBSize = followB.size

                            // Если имеется продукция вида A -> αBβ, то все элементы множества
                            // FIRST(β) кроме ε, помещаются в множество FOLLOW(B)

                            // Если имеется продукция вида A -> αB или A -> αBβ, где FIRST(β) содержит ε, то
                            // все элементы из FOLLOW(A) помещаются в FOLLOW(B)

                            when {
                                ind < prod.size - 1 -> {
                                    val firstBetta = first.getValue(prod[ind + 1])
                                    followB.addAll(firstBetta.filter { it != EPSILON })

                                    if (firstBetta.contains(EPSILON)) {
                                        followB.addAll(followA)
                                    }
                                }
                                else -> followB.addAll(followA)
                            }

                            if (followBSize != followB.size) {
                                follow[B] = followB
                                isChanged = true
                            }
                        }
                    }
                }
            }
        } while (isChanged)
    }

    override fun process(termStream: ITermStream) {

        // Стек, содержащий терминальные и нетерминальные символы
        val stack = mutableListOf(EOF, startProduction)

        // Символ на вершине стека
        var z = stack.peek()

        if (!termStream.hasNext()) {
            // Если во входном потоке символов больше нет - нужно сообщить об ошибке
            termStream.error(null, follow.getValue(z!!))
            return
        }
        var a = termStream.peek()!!

        while (z != EOF) {
            val x = z!! // чтоб не мусорить в коде лишними `!!`

            // если X равен a
            if (x == a.name) {
                // Снимаем символ со стека и переходим к следующему символу во входном потоке
                stack.pop()
                termStream.next()
                a = if (termStream.hasNext()) {
                    termStream.peek()!!
                } else {
                    // Если во входном потоке символов больше нет - значит a = EOF
                    Term(EOF)
                }
                z = stack.peek()
            } else if (x in terminals) {
                // если X - терминал - ошибка
                termStream.error(a, setOf(x))
                return
            } else {
                val tableRow = mTable.getValue(x) // строка M-таблицы для нетерминала X

                // если M[X, a] - запись об ошибке
                if (a.name !in tableRow.keys) {
                    termStream.error(a, follow.getValue(x))
                    return
                }
                // если M[X, a] = X -> Y1 Y2 Y3...YK
                else {
                    // выводим продукцию M[X, a]
                    println("$x->${tableRow.getValue(a.name).joinToString(" ")} ")

                    // снимаем символ со стека X
                    stack.pop()

                    // помещаем в стек YK, YK-1, ... Y1
                    stack.addAll(tableRow.getValue(a.name).reversed())

                    z = stack.peek()
                }
            }
        }
    }
}
