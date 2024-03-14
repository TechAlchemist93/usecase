package com.tech.alchemist.usecase

import com.tech.alchemist.common.LoggerDelegate

abstract class UseCase<R> {
  private val log by LoggerDelegate()

  data class Requirement<T : Any>(
    val key: Context.Key<T>,
    val ifNotExists: (() -> T)?,
  )

  fun <T : Any> require(key: Context.Key<T>) = Requirement(key = key, ifNotExists = null)
  fun <T: Any> ifNotExists(key: Context.Key<T>, ifNotExists: () -> T) = Requirement(key = key, ifNotExists = ifNotExists)

  abstract fun ctxRequirements(): List<Requirement<*>>

  private fun verifyRequirements(ctx: Context) {
    val missingKeys = mutableListOf<String>()

    for ((key, ifNotExists) in ctxRequirements()) {
      if (!ctx.containsKey(key)) {
        //short circuit: Don't process new variables if errors exist
        if (ifNotExists != null && missingKeys.isEmpty()) {
          @Suppress("UNCHECKED_CAST")
          ctx[key as Context.Key<Any>] = ifNotExists()
        } else {
          missingKeys.add(key.name)
        }
      }
    }

    if (missingKeys.isNotEmpty()) {
      throw RuntimeException(
        "context requirements not satisfied. " +
            "Missing keys: ${missingKeys.joinToString("")}"
      )
    }
  }

  fun execute(ctx: Context): Result<R> {
    ctx[CURRENT_CLASS] = this.javaClass.simpleName

    return Result.runCatching {
      log.trace("Starting ${ctx[CURRENT_CLASS]}")
      verifyRequirements(ctx)
      unsafeExecute(ctx)
    }
      .onFailure {
        Result.runCatching { onFailure(ctx, it) }
          .onFailure { ex -> log.error("onFailure() handler error: ", ex) }
      }
      .onSuccess {
        Result.runCatching { onSuccess(ctx, it) }
          .onFailure { ex -> log.error("onSuccess() handler error: ", ex) }
      }
  }

  protected abstract fun unsafeExecute(ctx: Context): R

  protected open fun onSuccess(ctx: Context, output: R) {
    log.trace("${ctx[CURRENT_CLASS]} completed successfully")
  }

  protected open fun onFailure(ctx: Context, error: Throwable?) {
    log.error("${ctx[CURRENT_CLASS]} failed!", error)
  }
}