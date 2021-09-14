package org.ztech.lrparser

/**
 * Интерфейс входного потока.
 */
interface ITermStream : PeekIterator<Term> {

    /**
     * Функция обработки ошибки. Вызывается синтаксическим анализатором при возникновении ошибки при разборе.
     * @param found символ, поданный на вход анализатора или null, если входной поток завершен
     * @param expected список терминальных, ожидаемых анализатором
     */
    fun error(found: Term?, expected: Set<String>)
}
