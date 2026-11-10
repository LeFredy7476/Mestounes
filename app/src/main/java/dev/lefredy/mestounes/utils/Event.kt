package dev.lefredy.mestounes.utils

class Event0() {
    private val list = mutableListOf<() -> Unit>()
    fun add(listener: () -> Unit) { if (filter(listener)) list.add(listener) }
    operator fun plusAssign(listener: () -> Unit) { if (filter(listener)) list.add(listener) }
    fun call() { for (function in list) function() }
    fun clear() { list.clear() }
    var filter: (() -> Unit) -> Boolean = { _ -> true }
}

class Event1<A>() {
    private val list = mutableListOf<(A) -> Unit>()
    fun add(listener: (A) -> Unit) { if (filter(listener)) list.add(listener) }
    operator fun plusAssign(listener: (A) -> Unit) { if (filter(listener)) list.add(listener) }
    fun call(p1: A) { for (function in list) function(p1) }
    fun clear() { list.clear() }
    var filter: ((A) -> Unit) -> Boolean = { _ -> true }
}

class Event2<A, B>() {
    private val list = mutableListOf<(A, B) -> Unit>()
    fun add(listener: (A, B) -> Unit) { if (filter(listener)) list.add(listener) }
    operator fun plusAssign(listener: (A, B) -> Unit) { if (filter(listener)) list.add(listener) }
    fun call(p1: A, p2: B) { for (function in list) function(p1, p2) }
    fun clear() { list.clear() }
    var filter: ((A, B) -> Unit) -> Boolean = { _ -> true }
}

class Event3<A, B, C>() {
    private val list = mutableListOf<(A, B, C) -> Unit>()
    fun add(listener: (A, B, C) -> Unit) { if (filter(listener)) list.add(listener) }
    operator fun plusAssign(listener: (A, B, C) -> Unit) { if (filter(listener)) list.add(listener) }
    fun call(p1: A, p2: B, p3: C) { for (function in list) function(p1, p2, p3) }
    fun clear() { list.clear() }
    var filter: ((A, B, C) -> Unit) -> Boolean = { _ -> true }
}

class Event4<A, B, C, D>() {
    private val list = mutableListOf<(A, B, C, D) -> Unit>()
    fun add(listener: (A, B, C, D) -> Unit) { if (filter(listener)) list.add(listener) }
    operator fun plusAssign(listener: (A, B, C, D) -> Unit) { if (filter(listener)) list.add(listener) }
    fun call(p1: A, p2: B, p3: C, p4: D) { for (function in list) function(p1, p2, p3, p4) }
    fun clear() { list.clear() }
    var filter: ((A, B, C, D) -> Unit) -> Boolean = { _ -> true }
}