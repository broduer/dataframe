package org.jetbrains.kotlinx.dataframe.api

import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.columns.ColumnGroup
import org.jetbrains.kotlinx.dataframe.columns.ColumnPath
import org.jetbrains.kotlinx.dataframe.columns.ColumnReference
import org.jetbrains.kotlinx.dataframe.columns.ColumnSet
import org.jetbrains.kotlinx.dataframe.columns.ColumnWithPath
import org.jetbrains.kotlinx.dataframe.columns.SingleColumn
import org.jetbrains.kotlinx.dataframe.columns.asColumnSet
import org.jetbrains.kotlinx.dataframe.impl.columns.TransformableColumnSet
import org.jetbrains.kotlinx.dataframe.impl.columns.TransformableSingleColumn
import org.jetbrains.kotlinx.dataframe.impl.columns.recursivelyImpl
import org.jetbrains.kotlinx.dataframe.impl.columns.transform
import org.jetbrains.kotlinx.dataframe.impl.columns.tree.flattenRecursively
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

// region ColumnsSelectionDsl
// TODO make path modification optional
// TODO rename to inclNestCols or includingNestedColumns
// TODO explore scoping function
public interface RecursivelyColumnsSelectionDsl {

    // region recursively

    /**
     * ## Recursively / Rec
     *
     * Modifies the previous call to run not only on the current column set,
     * but also on all columns inside [column groups][ColumnGroup].
     *
     * `df.`[select][DataFrame.select]` { `[colsOf][SingleColumn.colsOf]`<`[String][String]`>() }`
     *
     * returns all columns of type [String] in the top-level, as expected. However, what if you want ALL
     * columns of type [String] even if they are inside a nested [column group][ColumnGroup]? Then you can use [recursively]:
     *
     * `df.`[select][DataFrame.select]` { `[colsOf][SingleColumn.colsOf]`<`[String][String]`>().`[recursively][TransformableColumnSet.recursively]`() }`
     *
     * This will return the columns of type [String] in all levels.
     *
     * More examples:
     *
     * `df.`[select][DataFrame.select]` { `[first][ColumnsSelectionDsl.firstChild]` { col -> col.`[any][DataColumn.any]` { it == "Alice" } }.`[recursively][TransformableSingleColumn.recursively]`() }`
     *
     * `df.`[select][DataFrame.select]` { `[cols][ColumnsSelectionDsl.cols]` { "name" in it.`[name][ColumnReference.name]` }.`[rec][TransformableColumnSet.rec]`() }`
     *
     * `df.`[select][DataFrame.select]` { `[valueCols][ColumnsSelectionDsl.valueCols]`().`[recursively][TransformableColumnSet.recursively]`() }`
     *
     * #### Examples for this overload:
     *
     * {@includeArg [CommonRecursivelyDocs.Examples]}
     *
     * @param [includeTopLevel\] Whether to include the top-level columns in the result. `true` by default.
     * @see [DataFrame.flatten\]
     */
    private interface CommonRecursivelyDocs {

        /** Example argument */
        interface Examples
    }

    /**
     * ## Recursively / Rec
     *
     * Modifies the previous call to run not only on the current column set,
     * but also on all columns inside [column groups][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() }`
     *
     * returns all columns of type [String] in the top-level, as expected. However, what if you want ALL
     * columns of type [String] even if they are inside a nested [column group][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]? Then you can use [recursively][org.jetbrains.kotlinx.dataframe.api.RecursivelyColumnsSelectionDsl.recursively]:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>().`[recursively][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableColumnSet.recursively]`() }`
     *
     * This will return the columns of type [String] in all levels.
     *
     * More examples:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[first][ColumnsSelectionDsl.firstChild]` { col -> col.`[any][org.jetbrains.kotlinx.dataframe.DataColumn.any]` { it == "Alice" } }.`[recursively][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableSingleColumn.recursively]`() }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[cols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols]` { "name" in it.`[name][org.jetbrains.kotlinx.dataframe.columns.ColumnReference.name]` }.`[rec][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableColumnSet.rec]`() }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.valueCols]`().`[recursively][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableColumnSet.recursively]`() }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][DataFrame.select]` { `[colsOf][SingleColumn.colsOf]`<`[String][String]`>().`[recursively][TransformableColumnSet.recursively]`() }`
     *
     * `df.`[select][DataFrame.select]` { myColumnGroup.`[all][ColumnsSelectionDsl.all]`().`[rec][TransformableColumnSet.rec]`() }`
     *
     * `df.`[select][DataFrame.select]` { `[groups][ColumnsSelectionDsl.groups]`().`[recursively][TransformableColumnSet.recursively]`() }`
     *
     * @param [includeTopLevel] Whether to include the top-level columns in the result. `true` by default.
     * @see [DataFrame.flatten]
     */
    public fun <C> TransformableColumnSet<C>.recursively(): ColumnSet<C> =
        recursivelyImpl(includeTopLevel = true, includeGroups = true)

    /** ## Recursively / Rec
     *
     * Modifies the previous call to run not only on the current column set,
     * but also on all columns inside [column groups][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() }`
     *
     * returns all columns of type [String] in the top-level, as expected. However, what if you want ALL
     * columns of type [String] even if they are inside a nested [column group][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]? Then you can use [recursively][org.jetbrains.kotlinx.dataframe.api.RecursivelyColumnsSelectionDsl.recursively]:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>().`[recursively][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableColumnSet.recursively]`() }`
     *
     * This will return the columns of type [String] in all levels.
     *
     * More examples:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[first][ColumnsSelectionDsl.firstChild]` { col -> col.`[any][org.jetbrains.kotlinx.dataframe.DataColumn.any]` { it == "Alice" } }.`[recursively][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableSingleColumn.recursively]`() }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[cols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols]` { "name" in it.`[name][org.jetbrains.kotlinx.dataframe.columns.ColumnReference.name]` }.`[rec][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableColumnSet.rec]`() }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.valueCols]`().`[recursively][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableColumnSet.recursively]`() }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>().`[recursively][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableColumnSet.recursively]`() }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[all][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.all]`().`[rec][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableColumnSet.rec]`() }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[groups][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.groups]`().`[recursively][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableColumnSet.recursively]`() }`
     *
     * @param [includeTopLevel] Whether to include the top-level columns in the result. `true` by default.
     * @see [DataFrame.flatten][org.jetbrains.kotlinx.dataframe.DataFrame.flatten]
     */
    public fun <C> TransformableColumnSet<C>.rec(): ColumnSet<C> = recursively()

    /**
     * ## Recursively / Rec
     *
     * Modifies the previous call to run not only on the current column set,
     * but also on all columns inside [column groups][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() }`
     *
     * returns all columns of type [String] in the top-level, as expected. However, what if you want ALL
     * columns of type [String] even if they are inside a nested [column group][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]? Then you can use [recursively][org.jetbrains.kotlinx.dataframe.api.RecursivelyColumnsSelectionDsl.recursively]:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>().`[recursively][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableColumnSet.recursively]`() }`
     *
     * This will return the columns of type [String] in all levels.
     *
     * More examples:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[first][ColumnsSelectionDsl.firstChild]` { col -> col.`[any][org.jetbrains.kotlinx.dataframe.DataColumn.any]` { it == "Alice" } }.`[recursively][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableSingleColumn.recursively]`() }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[cols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols]` { "name" in it.`[name][org.jetbrains.kotlinx.dataframe.columns.ColumnReference.name]` }.`[rec][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableColumnSet.rec]`() }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.valueCols]`().`[recursively][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableColumnSet.recursively]`() }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][DataFrame.select]` { `[first][ColumnsSelectionDsl.firstChild]` { col -> col.`[any][DataColumn.any]` { it == "Alice" } }.`[recursively][TransformableSingleColumn.recursively]`() }`
     *
     * `df.`[select][DataFrame.select]` { `[single][ColumnsSelectionDsl.singleChild]` { it.name == "myCol" }.`[rec][TransformableSingleColumn.rec]`() }`
     *
     * @param [includeTopLevel] Whether to include the top-level columns in the result. `true` by default.
     * @see [DataFrame.flatten]
     */
    public fun TransformableSingleColumn<*>.recursively(): SingleColumn<*> =
        recursivelyImpl(includeTopLevel = true, includeGroups = true)

    /** ## Recursively / Rec
     *
     * Modifies the previous call to run not only on the current column set,
     * but also on all columns inside [column groups][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() }`
     *
     * returns all columns of type [String] in the top-level, as expected. However, what if you want ALL
     * columns of type [String] even if they are inside a nested [column group][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]? Then you can use [recursively][org.jetbrains.kotlinx.dataframe.api.RecursivelyColumnsSelectionDsl.recursively]:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>().`[recursively][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableColumnSet.recursively]`() }`
     *
     * This will return the columns of type [String] in all levels.
     *
     * More examples:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[first][ColumnsSelectionDsl.firstChild]` { col -> col.`[any][org.jetbrains.kotlinx.dataframe.DataColumn.any]` { it == "Alice" } }.`[recursively][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableSingleColumn.recursively]`() }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[cols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols]` { "name" in it.`[name][org.jetbrains.kotlinx.dataframe.columns.ColumnReference.name]` }.`[rec][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableColumnSet.rec]`() }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.valueCols]`().`[recursively][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableColumnSet.recursively]`() }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[first][ColumnsSelectionDsl.firstChild]` { col -> col.`[any][org.jetbrains.kotlinx.dataframe.DataColumn.any]` { it == "Alice" } }.`[recursively][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableSingleColumn.recursively]`() }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[single][ColumnsSelectionDsl.singleChild]` { it.name == "myCol" }.`[rec][org.jetbrains.kotlinx.dataframe.impl.columns.TransformableSingleColumn.rec]`() }`
     *
     * @param [includeTopLevel] Whether to include the top-level columns in the result. `true` by default.
     * @see [DataFrame.flatten][org.jetbrains.kotlinx.dataframe.DataFrame.flatten]
     */
    public fun TransformableSingleColumn<*>.rec(): SingleColumn<*> = recursively()

    // endregion
    
    // region dfs

    @Deprecated(
        message = "dfs is deprecated, use recursively instead.",
        replaceWith = ReplaceWith("this.cols(predicate).recursively(includeTopLevel = false)"),
        level = DeprecationLevel.WARNING,
    )
    public fun <C> ColumnSet<C>.dfs(predicate: (ColumnWithPath<*>) -> Boolean): ColumnSet<*> = dfsInternal(predicate)

    @Deprecated(
        message = "dfs is deprecated, use recursively instead.",
        replaceWith = ReplaceWith("this.cols(predicate).recursively()"),
        level = DeprecationLevel.WARNING,
    )
    public fun SingleColumn<DataRow<*>>.dfs(predicate: (ColumnWithPath<*>) -> Boolean): ColumnSet<*> =
        ensureIsColGroup().asColumnSet().dfsInternal(predicate)

    @Deprecated(
        message = "dfs is deprecated, use recursively instead.",
        replaceWith = ReplaceWith("this.cols(predicate).recursively()"),
        level = DeprecationLevel.WARNING,
    )
    public fun ColumnsSelectionDsl<*>.dfs(predicate: (ColumnWithPath<*>) -> Boolean): ColumnSet<*> =
        asSingleColumn().dfs(predicate)

    @Deprecated(
        message = "dfs is deprecated, use recursively instead.",
        replaceWith = ReplaceWith("this.cols(predicate).recursively()"),
        level = DeprecationLevel.WARNING,
    )
    public fun String.dfs(predicate: (ColumnWithPath<*>) -> Boolean): ColumnSet<*> =
        columnGroup(this).dfs(predicate)

    @Deprecated(
        message = "dfs is deprecated, use recursively instead.",
        replaceWith = ReplaceWith("this.cols(predicate).recursively()"),
        level = DeprecationLevel.WARNING,
    )
    public fun <C> KProperty<C>.dfs(predicate: (ColumnWithPath<*>) -> Boolean): ColumnSet<*> =
        columnGroup(this).dfs(predicate)

    @Deprecated(
        message = "dfs is deprecated, use recursively instead.",
        replaceWith = ReplaceWith("this.cols(predicate).recursively()"),
        level = DeprecationLevel.WARNING,
    )
    public fun ColumnPath.dfs(predicate: (ColumnWithPath<*>) -> Boolean): ColumnSet<*> =
        columnGroup(this).dfs(predicate)

    // endregion

    // region allDfs

    @Deprecated(
        message = "allDfs is deprecated, use recursively instead.",
        replaceWith = ReplaceWith("this.cols { includeGroups || !it.isColumnGroup() }.recursively()"),
        level = DeprecationLevel.WARNING,
    )
    public fun ColumnSet<*>.allDfs(includeGroups: Boolean = false): ColumnSet<*> =
        if (includeGroups) dfs { true } else dfs { !it.isColumnGroup() }

    @Deprecated(
        message = "allDfs is deprecated, use recursively instead.",
        replaceWith = ReplaceWith("this.cols { includeGroups || !it.isColumnGroup() }.recursively()"),
        level = DeprecationLevel.WARNING,
    )
    public fun SingleColumn<DataRow<*>>.allDfs(includeGroups: Boolean = false): ColumnSet<*> =
        if (includeGroups) dfs { true } else dfs { !it.isColumnGroup() }

    @Deprecated(
        message = "allDfs is deprecated, use recursively instead.",
        replaceWith = ReplaceWith("this.cols { includeGroups || !it.isColumnGroup() }.recursively()"),
        level = DeprecationLevel.WARNING,
    )
    public fun ColumnsSelectionDsl<*>.allDfs(includeGroups: Boolean = false): ColumnSet<*> =
        if (includeGroups) dfs { true } else dfs { !it.isColumnGroup() }

    @Deprecated(
        message = "allDfs is deprecated, use recursively instead.",
        replaceWith = ReplaceWith("this.cols { includeGroups || !it.isColumnGroup() }.recursively()"),
        level = DeprecationLevel.WARNING,
    )
    public fun String.allDfs(includeGroups: Boolean = false): ColumnSet<*> =
        columnGroup(this).allDfs(includeGroups)

    @Deprecated(
        message = "allDfs is deprecated, use recursively instead.",
        replaceWith = ReplaceWith("this.cols { includeGroups || !it.isColumnGroup() }.recursively()"),
        level = DeprecationLevel.WARNING,
    )
    public fun KProperty<DataRow<*>>.allDfs(includeGroups: Boolean = false): ColumnSet<*> =
        columnGroup(this).allDfs(includeGroups)

    @Deprecated(
        message = "allDfs is deprecated, use recursively instead.",
        replaceWith = ReplaceWith("this.cols { includeGroups || !it.isColumnGroup() }.recursively()"),
        level = DeprecationLevel.WARNING,
    )
    public fun ColumnPath.allDfs(includeGroups: Boolean = false): ColumnSet<*> =
        columnGroup(this).allDfs(includeGroups)
    
    // endregion
    
    // region dfsOf
    @Deprecated(
        message = "Use recursively() instead",
        replaceWith = ReplaceWith(
            "this.colsOf(type, predicate).recursively()",
            "org.jetbrains.kotlinx.dataframe.columns.recursively",
            "org.jetbrains.kotlinx.dataframe.api.colsOf",
        ),
    )
    public fun <C> String.dfsOf(type: KType, predicate: (ColumnWithPath<C>) -> Boolean = { true }): ColumnSet<*> =
        columnGroup(this).dfsOf(type, predicate)

    @Deprecated(
        message = "Use recursively() instead",
        replaceWith = ReplaceWith(
            "this.colsOf(type, predicate).recursively()",
            "org.jetbrains.kotlinx.dataframe.columns.recursively",
            "org.jetbrains.kotlinx.dataframe.api.colsOf",
        ),
    )
    public fun <C> KProperty<DataRow<*>>.dfsOf(
        type: KType,
        predicate: (ColumnWithPath<C>) -> Boolean = { true },
    ): ColumnSet<*> = columnGroup(this).dfsOf(type, predicate)

    // endregion
}

@Deprecated("Replaced with recursively()")
internal fun ColumnSet<*>.dfsInternal(predicate: (ColumnWithPath<*>) -> Boolean) =
    transform { it.filter { it.isColumnGroup() }.flatMap { it.children().flattenRecursively().filter(predicate) } }

@Deprecated(
    message = "Use recursively() instead",
    replaceWith = ReplaceWith(
        "this.colsOf(type, predicate).recursively()",
        "org.jetbrains.kotlinx.dataframe.columns.recursively",
        "org.jetbrains.kotlinx.dataframe.api.colsOf",
    ),
)
public fun <C> ColumnSet<*>.dfsOf(type: KType, predicate: (ColumnWithPath<C>) -> Boolean = { true }): ColumnSet<*> =
    dfsInternal { it.isSubtypeOf(type) && predicate(it.cast()) }

@Deprecated(
    message = "Use recursively() instead",
    replaceWith = ReplaceWith(
        "this.colsOf(type, predicate).recursively()",
        "org.jetbrains.kotlinx.dataframe.columns.recursively",
        "org.jetbrains.kotlinx.dataframe.api.colsOf",
    ),
)
public fun <C> SingleColumn<DataRow<*>>.dfsOf(
    type: KType,
    predicate: (ColumnWithPath<C>) -> Boolean = { true },
): ColumnSet<*> =
    ensureIsColGroup().asColumnSet().dfsInternal { it.isSubtypeOf(type) && predicate(it.cast()) }

@Deprecated(
    message = "Use recursively() instead",
    replaceWith = ReplaceWith(
        "this.colsOf(type, predicate).recursively()",
        "org.jetbrains.kotlinx.dataframe.columns.recursively",
        "org.jetbrains.kotlinx.dataframe.api.colsOf",
    ),
)
public fun <C> ColumnsSelectionDsl<*>.dfsOf(
    type: KType,
    predicate: (ColumnWithPath<C>) -> Boolean = { true },
): ColumnSet<*> =
    this.asSingleColumn().dfsOf(type, predicate)

@Deprecated(
    message = "Use recursively() instead",
    replaceWith = ReplaceWith(
        "this.colsOf<C>(filter).recursively(includeTopLevel = false)",
        "org.jetbrains.kotlinx.dataframe.columns.recursively",
        "org.jetbrains.kotlinx.dataframe.api.colsOf",
    ),
)
public inline fun <reified C> ColumnSet<*>.dfsOf(noinline filter: (ColumnWithPath<C>) -> Boolean = { true }): ColumnSet<C> =
    dfsOf(typeOf<C>(), filter) as ColumnSet<C>

@Deprecated(
    message = "Use recursively() instead",
    replaceWith = ReplaceWith(
        "this.colsOf<C>(filter).recursively()",
        "org.jetbrains.kotlinx.dataframe.columns.recursively",
        "org.jetbrains.kotlinx.dataframe.api.colsOf",
    ),
)
public inline fun <reified C> SingleColumn<DataRow<*>>.dfsOf(noinline filter: (ColumnWithPath<C>) -> Boolean = { true }): ColumnSet<C> =
    dfsOf(typeOf<C>(), filter) as ColumnSet<C>

@Deprecated(
    message = "Use recursively() instead",
    replaceWith = ReplaceWith(
        "this.colsOf<C>(filter).recursively()",
        "org.jetbrains.kotlinx.dataframe.columns.recursively",
        "org.jetbrains.kotlinx.dataframe.api.colsOf",
    ),
)
public inline fun <reified C> ColumnsSelectionDsl<*>.dfsOf(noinline filter: (ColumnWithPath<C>) -> Boolean = { true }): ColumnSet<C> =
    dfsOf(typeOf<C>(), filter) as ColumnSet<C>

// endregion