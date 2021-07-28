package org.ztech.lrparser

import java.util.NoSuchElementException

/**
 * Алиас для MutableList
 */
typealias Stack<T> = MutableList<T>

/**
 * Добавляение элемента в стэк.
 * @param item добавялемый элемент
 */
fun <T> Stack<T>.push(item: T) = add(item)

/**
 * Pops идаление элемента из стэка.
 * @return item удаляемый элемент или null
 */
fun <T> Stack<T>.pop(): T? = if (isNotEmpty()) removeAt(lastIndex) else null

/**
 * Peeks возвращает без удаления последний элемент стека.
 * @return последний элемент стека или null
 */
fun <T> Stack<T>.peek(): T? = if (isNotEmpty()) this[lastIndex] else null

/**
 * Peeks возвращает без удаления предпопоследний элемент стека.
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
