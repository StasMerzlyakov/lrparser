package org.ztech.lrparser

import java.util.NoSuchElementException

/**
 * Alias для MutableList
 */
typealias Stack<T> = MutableList<T>

/**
 * Добавление элемента в стек.
 * @param item добавляемый элемент
 */
fun <T> Stack<T>.push(item: T) = add(item)

/**
 * Pops удаление элемента из стека.
 * @return item удаляемый элемент или null
 */
fun <T> Stack<T>.pop(): T? = if (isNotEmpty()) removeAt(lastIndex) else null

/**
 * Peeks возвращает без удаления последний элемент стека.
 * @return последний элемент стека или null
 */
fun <T> Stack<T>.peek(): T? = if (isNotEmpty()) this[lastIndex] else null

/**
 * Peeks возвращает без удаления предпоследний элемент стека.
 * @return последний элемент стека или null
 */
fun <T> Stack<T>.peekPrev(): T? = if (isNotEmpty()) this[lastIndex] else null

/**
 * Итератор с функцией peek. Используется в StructuredParser
 */
interface PeekIterator<T> : Iterator<T> {
    /**
     * Функция показа следующего токена
     */
    fun peek(): T?
}

/**
 * Реализация PeekIterator
 */
open class PeekIteratorImpl<T> (val iterator: Iterator<T>) : PeekIterator<T> {

    private var peeked = false

    private var peekedValue: T? = null

    override fun hasNext(): Boolean {
        return if (peeked) {
            true
        } else {
            iterator.hasNext()
        }
    }

    override fun next(): T {
        if (peeked) {
            peeked = false
            return peekedValue!!
        } else {
            if (iterator.hasNext()) {
                return iterator.next()
            }
        }
        throw NoSuchElementException()
    }

    override fun peek(): T? {
        return when {
            peeked -> peekedValue
            iterator.hasNext() -> {
                peeked = true
                peekedValue = iterator.next()
                peekedValue
            }
            else -> {
                null
            }
        }
    }
}

infix fun <T> Collection<T>.deepEqualTo(other: Collection<T>): Boolean {
    // check collections aren't same
    if (this !== other) {
        // fast check of sizes
        if (this.size != other.size) return false
        val areNotEqual = this.asSequence()
            .zip(other.asSequence())
            // check this and other contains same elements at position
            .map { (fromThis, fromOther) -> fromThis == fromOther }
            // searching for first negative answer
            .contains(false)
        if (areNotEqual) return false
    }
    // collections are same or they are contains same elements with same order
    return true
}
