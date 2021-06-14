package org.ztech.lrparser

import org.junit.jupiter.api.Test

class ParserTest {

    @Test
    fun doFirstTest(){
        println("A : " + getNonTerminals('A'))
        println("B : " + getNonTerminals('B'))
        println("C : " + getNonTerminals('C'))
        println("S : " + getNonTerminals('S'))
    }
}