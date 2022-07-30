package com.zwendo.restrikt.test

import org.junit.jupiter.api.Test

class RestriktTest {

    @Test
    fun `assert that not annotated classes are visible`() {
        TestingObject.VisibleClass // should break if class was not visible
    }

    @Test
    fun `assert that not annotated functions are visible`() {
        TestingObject.visibleFunction() // should break if function was not visible
    }

    @Test
    fun `assert that not annotated properties are visible`() {
        TestingObject.visibleProperty // should break if property was not visible
    }

    // following tests will prevent tests to compile if they are uncommented

    /*
        @Test
        fun `assert that annotated classes are hidden`() {
            TestingObject.HiddenClass
        }

        @Test
        fun `assert that annotated functions are hidden`() {
            TestingObject.hiddenFunction()
        }

        @Test
        fun `assert that annotated properties are hidden`() {
            TestingObject.hiddenProperty
        }
    */

}
