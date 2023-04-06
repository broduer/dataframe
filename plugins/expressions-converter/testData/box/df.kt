package org.jetbrains.kotlinx.dataframe.explainer

import org.jetbrains.kotlinx.dataframe.*
import org.jetbrains.kotlinx.dataframe.api.*

fun callChain(df: DataFrame<*>) {
    val df1 = df
        .filter { "age"<Int>() > 20 }
        .groupBy("value")
        .sum()

    val df2 = df
        .filter { "age"<Int>() > 20 }
        .groupBy("value")
        .sum()
}

interface Person

class Wrapper {
    val df = dataFrameOf("name", "age", "city", "weight")(
        "Alice", 15, "London", 54,
        "Bob", 45, "Dubai", 87,
        "Charlie", 20, "Moscow", null,
        "Charlie", 40, "Milan", null,
        "Bob", 30, "Tokyo", 68,
        "Alice", 20, null, 55,
        "Charlie", 30, "Moscow", 90
    )

    val typed: DataFrame<Person> = df.cast()

    fun ff() {
        val name by column<String>()
        val aggregated = typed.groupBy { name() }.aggregate {
            (if (name().first().startsWith("A")) first() else null) into "agg"
        }["agg"]
    }
}


object PluginCallback {
    var action: (String, String, Any, String, String?, String? String?, Int) -> Unit = @Disable { _, _, _, _, _, _, _, _  -> Unit }
    @Disable
    fun doAction(string: String, name: String, df: Any, id: String, receiverId: String?, containingClassFqName: String?, containingFunName: String?, statemenIndex: Int) {
        action(string, name, df, id, receiverId, containingClassFqName, containingFunName, statemenIndex)
    }
}

//fun callChainTransformed(df: DataFrame<*>) {
//    val df1 = df
//        .filter { "age"<Int>() > 20 }
//        .also { PluginCallback.doAction(""".filter { "age"<Int>() > 20 }""", "filter", it) }
//}

//fun callChainTransformed(df: DataFrame<*>) {
//    val df1 = df
//        .filter { "age"<Int>() > 20 }
//        .also { PluginCallback.action(""".filter { "age"<Int>() > 20 }""", it) }
//        .groupBy("something")
//        .sum()
//        .also { PluginCallback.action(""".groupBy("something").sum()""", it) }
//}

annotation class Disable

@Disable
fun box(): String {
    val age by columnOf(10, 21, 30, 1)
    val value by columnOf("a", "b", "c", "c")
    val df = dataFrameOf(age, value)
    PluginCallback.action = { str, _, df, id, receiverId, containingClassFqName, containingFunName, statementIndex ->
        println("== Call ==")
        if (df is AnyFrame) {
            println(str)
            df.print()
            println(id)
            println(receiverId)
            println(containingClassFqName)
            println(containingFunName)
            println("statementIndex = ${statementIndex}")
        } else {
            println(df::class)
        }
        println("== End ==")
    }
    println("CallChain")
    callChain(df)
    println("ff")
    Wrapper().ff()
//    callChainTransformed(df)
    return "OK"
}