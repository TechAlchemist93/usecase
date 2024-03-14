package com.tech.alchemist.usecase

import java.util.*

@Suppress("UNCHECKED_CAST")
class Context(vararg entries: Pair<Key<*>, Any>) {
  private val internalMap = mutableMapOf<Key<*>, Any>()

  init {
    entries.forEach { (key, value) -> set(key as Key<Any>, value) }
  }

  data class Key<T>(
    val name: String,
    val subKeys: Map<Key<*>, (T) -> Any>? = null
  )

  fun isEmpty(): Boolean = internalMap.isEmpty()
  fun containsKey(key: Key<*>): Boolean = internalMap.containsKey(key)
  fun containsValue(value: Any): Boolean = internalMap.containsValue(value)
  val entries: Set<Map.Entry<Key<*>, Any?>> get() = internalMap.entries
  val values: Collection<Any?> get() = internalMap.values
  val keys: Set<Key<*>> get() = internalMap.keys
  val size: Int get() = internalMap.size

  operator fun <T> get(key: Key<T>): T {
    return internalMap[key] as? T ?: throw NoSuchElementException(key.name)
  }

  operator fun <T: Any> set(key: Key<T>, value: T) {
    key.subKeys?.forEach{(key, func) ->
      set(key as Key<Any>, func(value))
    }

    internalMap[key] = value
  }

  fun <T> remove(key: Key<T>): T? = internalMap.remove(key) as? T
  fun clear() = internalMap.clear()

}
