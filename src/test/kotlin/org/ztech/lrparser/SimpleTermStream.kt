package org.ztech.lrparser

/**
 * Простая реализация SimpleTermStream для использования в тестах
 */
class SimpleTermStream(iterator: Iterator<Term>) : PeekIteratorImpl<Term>(iterator), ITermStream {
    override fun error(found: Term?, expected: Set<String>) {
        val errStr = found ?.let {
            "Found ${it.name}(${it.value}). Expected ${expected.joinToString { ", "}}."
        } ?: "Found EOF. Expected ${expected.joinToString { ", "}}"
        throw Exception(errStr)
    }
}
