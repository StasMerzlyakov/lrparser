package org.ztech.lrparser

/**
 * Базовый интерфейс грамматики
 */
interface IGrammar {
    /**
     * Метод разбора
     */
    fun process(termStream: ITermStream)
}
