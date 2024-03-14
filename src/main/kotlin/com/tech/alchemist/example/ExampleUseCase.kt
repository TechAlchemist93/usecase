package com.tech.alchemist.example

import com.tech.alchemist.usecase.UseCase
import com.tech.alchemist.usecase.Context
import java.time.LocalDate

class ExampleUseCase(
  //autowired services go here
): UseCase<Any>() {

  val DATE = Context.Key<LocalDate>("date")
  val TEST_ID = Context.Key<String>("test_id")
  val TEST_NAME = Context.Key<String>("test_name")
  val TEST_OBJ = Context.Key<TestObj>("test_obj", mapOf(
    TEST_ID to { it.id },
    TEST_NAME to { it.name }
  ))

  override fun ctxRequirements(): List<Requirement<*>> {
    return listOf(
      require(TEST_ID),
      ifNotExists(DATE) { LocalDate.now() }
    )
  }

  fun execute(test: TestObj, date: LocalDate): Result<Any> {
    return execute(
      Context(
      TEST_OBJ to test,
      DATE to date
    )
    )
  }

  override fun unsafeExecute(ctx: Context): Any {
    val testId = ctx[TEST_ID]
    val date = ctx[DATE]

    //DO STUFF
    TODO()
  }
}