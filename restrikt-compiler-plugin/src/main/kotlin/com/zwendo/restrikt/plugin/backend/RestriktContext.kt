package com.zwendo.restrikt.plugin.backend

import com.zwendo.restrikt.plugin.backend.wrapper.KotlinClass
import kotlinx.metadata.jvm.KotlinClassMetadata
import org.jetbrains.kotlin.backend.common.peek
import org.jetbrains.kotlin.backend.common.pop
import org.jetbrains.kotlin.backend.common.push


internal object RestriktContext {

    private var actions = mutableListOf<() -> Unit>()
    private var classNameToData = hashMapOf<String, KotlinClass>()
    private val classStack = mutableListOf<KotlinClass>()

    /**
     * Adds an action to the list of actions to be executed when the context is reset.
     */
    fun addAction(action: () -> Unit) {
        actions.add(action)
    }

    /**
     * Called when a class has been parsed.
     */
    fun done() {
        classStack.pop()
        if (classStack.isNotEmpty()) return

        // if the class stack is empty,
        // we are done with the current class and can process the actions and clear data
        actions.forEach { it() }
        actions = mutableListOf()
        classNameToData = hashMapOf()
    }

    /**
     * Adds a class to the context.
     */
    fun storeClass(kmClass: KotlinClassMetadata) {
        val currentClass = classStack.peek()
            ?: throw AssertionError("Class $kmClass stack should not be empty")
        currentClass.setData(kmClass)
    }

    /**
     * Adds a class to the context stack.
     */
    fun visitNewClass(className: String): KotlinClass {
        val clazz = KotlinClass()
        classStack.push(clazz)
        classNameToData[className] = clazz
        return clazz
    }

    /**
     * Gets a class for the given fqName.
     */
    fun getClass(className: String?) = classNameToData[className]

    /**
     * Gets the current class.
     */
    val currentClass: KotlinClass
        get() = classStack.peek() ?: throw AssertionError("Class stack should not be empty")

}

