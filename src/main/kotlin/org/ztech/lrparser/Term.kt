package org.ztech.lrparser

/**
 * Терминальный символ, поступающий из лексического анализатора.
 * @name - имя терминального символа
 * @value - значение символа
 */
data class Term(val name: String, val value: String = name)
