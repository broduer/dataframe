[//]: # (title: Create DataFrame)
<!---IMPORT org.jetbrains.kotlinx.dataframe.samples.api.Create-->

This section describes ways to create [`DataFrame`](DataFrame.md).

### emptyDataFrame

Returns [`DataFrame`](DataFrame.md) with no rows and no columns.

<!---FUN createEmptyDataFrame-->

```kotlin
val df = emptyDataFrame<Any>()
```

<!---END-->

### dataFrameOf

Returns [`DataFrame`](DataFrame.md) with given column names and values.

<!---FUN createDataFrameOf-->

```kotlin
// DataFrame with 2 columns and 3 rows
val df = dataFrameOf("name", "age")(
    "Alice", 15,
    "Bob", 20,
    "Charlie", 100
)
```

<!---END-->

<!---FUN createDataFrameOfPairs-->

```kotlin
// DataFrame with 2 columns and 3 rows
val df = dataFrameOf(
    "name" to listOf("Alice", "Bob", "Charlie"),
    "age" to listOf(15, 20, 100)
)
```

<!---END-->

<!---FUN createDataFrameFromColumns-->

```kotlin
val name by columnOf("Alice", "Bob", "Charlie")
val age by columnOf(15, 20, 22)

// DataFrame with 2 columns
val df = dataFrameOf(name, age)
```

<!---END-->

<!---FUN createDataFrameFromNamesAndValues-->

```kotlin
val names = listOf("name", "age")
val values = listOf(
    "Alice", 15,
    "Bob", 20,
    "Charlie", 22
)
val df = dataFrameOf(names, values)
```

<!---END-->

<!---FUN createDataFrameWithFill-->

```kotlin
// Multiplication table
dataFrameOf(1..10) { x -> (1..10).map { x * it } }
```

<!---END-->

<!---FUN createDataFrameWithRandom-->

```kotlin
// 5 columns filled with 7 random double values:
val names = (1..5).map { "column$it" }
dataFrameOf(names).randomDouble(7)

// 5 columns filled with 7 random double values between 0 and 1 (inclusive)
dataFrameOf(names).randomDouble(7, 0.0..1.0).print()

// 5 columns filled with 7 random int values between 0 and 100 (inclusive)
dataFrameOf(names).randomInt(7, 0..100).print()
```

<!---END-->

<!---FUN createDataFrameFillConstant-->

```kotlin
val names = listOf("first", "second", "third")

// DataFrame with 3 columns, fill each column with 15 `true` values
val df = dataFrameOf(names).fill(15, true)
```

<!---END-->

### toDataFrame

[`DataFrame`](DataFrame.md) from `Iterable<DataColumn>`:

<!---FUN createDataFrameFromIterable-->

```kotlin
val name by columnOf("Alice", "Bob", "Charlie")
val age by columnOf(15, 20, 22)

listOf(name, age).toDataFrame()
```

<dataFrame src="org.jetbrains.kotlinx.dataframe.samples.api.Create.createDataFrameFromIterable.html"/>
<!---END-->

`DataFrame` from `Map<String, List<*>>`:

<!---FUN createDataFrameFromMap-->

```kotlin
val map = mapOf("name" to listOf("Alice", "Bob", "Charlie"), "age" to listOf(15, 20, 22))

// DataFrame with 2 columns
map.toDataFrame()
```

<dataFrame src="org.jetbrains.kotlinx.dataframe.samples.api.Create.createDataFrameFromMap.html"/>
<!---END-->

[`DataFrame`](DataFrame.md) from [`Iterable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-iterable/) of [basic types](https://kotlinlang.org/docs/basic-types.html) (except arrays):

Return type of these overloads is a typed DataFrame. Its data schema defines column that can be used right after conversion for additional computations

<!---FUN readDataFrameFromValues-->

```kotlin
val names = listOf("Alice", "Bob", "Charlie")
val df: DataFrame<ValueProperty<String>> = names.toDataFrame()
df.add("length") { value.length }
```

<!---END-->

[`DataFrame`](DataFrame.md) from [`Iterable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-iterable/) of objects:

<!---FUN readDataFrameFromObject-->

```kotlin
data class Person(val name: String, val age: Int)

val persons = listOf(Person("Alice", 15), Person("Bob", 20), Person("Charlie", 22))

val df = persons.toDataFrame()
```

<!---END-->

Scans object properties using reflection and creates [ValueColumn](DataColumn.md#valuecolumn) for every property. 
Scope of properties for scanning is defined at compile-time by formal types of objects in [`Iterable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-iterable/), so properties of implementation classes will not be scanned.

Specify `depth` parameter to perform deep object graph traversal and convert nested objects into [ColumnGroups](DataColumn.md#columngroup) and [FrameColumns](DataColumn.md#framecolumn):

<!---FUN readDataFrameFromDeepObject-->

```kotlin
data class Name(val firstName: String, val lastName: String)
data class Score(val subject: String, val value: Int)
data class Student(val name: Name, val age: Int, val scores: List<Score>)

val students = listOf(
    Student(Name("Alice", "Cooper"), 15, listOf(Score("math", 4), Score("biology", 3))),
    Student(Name("Bob", "Marley"), 20, listOf(Score("music", 5)))
)

val df = students.toDataFrame(maxDepth = 1)
```

<!---END-->

For detailed control over object graph transformation use configuration DSL. It allows you to exclude particular properties or classes from object graph traversal, compute additional columns and configure column grouping.

<!---FUN readDataFrameFromDeepObjectWithExclude-->

```kotlin
val df = students.toDataFrame {
    // add column
    "year of birth" from { 2021 - it.age }

    // scan all properties
    properties(maxDepth = 1) {
        exclude(Score::subject) // `subject` property will be skipped from object graph traversal
        preserve<Name>() // `Name` objects will be stored as-is without transformation into DataFrame
    }

    // add column group
    "summary" {
        "max score" from { it.scores.maxOf { it.value } }
        "min score" from { it.scores.minOf { it.value } }
    }
}
```

<!---END-->

### DynamicDataFrameBuilder

Previously mentioned dataframe constructors throw an exception when column names are duplicated. 
When implementing a custom operation involving multiple dataframes, computed columns or parsing some third-party data,
it might be desirable to disambiguate column names instead of throwing an exception. 

<!---FUN duplicatedColumns-->

```kotlin
fun peek(vararg dataframes: AnyFrame): AnyFrame {
    val builder = DynamicDataFrameBuilder()
    for (df in dataframes) {
        df.columns().firstOrNull()?.let { builder.add(it) }
    }
    return builder.toDataFrame()
}

val col by columnOf(1, 2, 3)
peek(dataFrameOf(col), dataFrameOf(col))
```

<dataFrame src="org.jetbrains.kotlinx.dataframe.samples.api.Create.duplicatedColumns.html"/>
<!---END-->

