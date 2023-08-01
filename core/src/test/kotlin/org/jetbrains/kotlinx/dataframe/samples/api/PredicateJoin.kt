package org.jetbrains.kotlinx.dataframe.samples.api

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.FormattingDSL
import org.jetbrains.kotlinx.dataframe.api.Infer
import org.jetbrains.kotlinx.dataframe.api.JoinedDataRow
import org.jetbrains.kotlinx.dataframe.api.RGBColor
import org.jetbrains.kotlinx.dataframe.api.and
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.api.colsOf
import org.jetbrains.kotlinx.dataframe.api.column
import org.jetbrains.kotlinx.dataframe.api.convert
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.dataframe.api.excludePredicateJoin
import org.jetbrains.kotlinx.dataframe.api.filterPredicateJoin
import org.jetbrains.kotlinx.dataframe.api.fullPredicateJoin
import org.jetbrains.kotlinx.dataframe.api.innerPredicateJoin
import org.jetbrains.kotlinx.dataframe.api.leftPredicateJoin
import org.jetbrains.kotlinx.dataframe.api.predicateJoin
import org.jetbrains.kotlinx.dataframe.api.rightPredicateJoin
import org.jetbrains.kotlinx.dataframe.api.with
import org.jetbrains.kotlinx.dataframe.explainer.PluginCallbackProxy
import org.jetbrains.kotlinx.dataframe.explainer.TransformDataFrameExpressions
import org.jetbrains.kotlinx.dataframe.io.DataFrameHtmlData
import org.jetbrains.kotlinx.dataframe.io.DisplayConfiguration
import org.jetbrains.kotlinx.dataframe.io.renderValueForHtml
import org.jetbrains.kotlinx.dataframe.io.toHTML
import org.jetbrains.kotlinx.dataframe.jupyter.ChainedCellRenderer
import org.jetbrains.kotlinx.dataframe.jupyter.DefaultCellRenderer
import org.jetbrains.kotlinx.dataframe.jupyter.RenderedContent
import org.junit.Test
import java.time.format.DateTimeFormatter

class PredicateJoin {

    @DataSchema
    interface Campaigns {
        val name: String
        val startDate: LocalDate
        val endDate: LocalDate
    }

    @DataSchema
    interface Visits {
        val date: LocalDate
        val userId: Int
    }

    private val campaigns = dataFrameOf("name", "startDate", "endDate")(
        "Winter Sale", LocalDate(2023, 1, 1), LocalDate(2023, 1, 31),
        "Spring Sale", LocalDate(2023, 4, 1), LocalDate(2023, 4, 30),
        "Summer Sale", LocalDate(2023, 7, 1), LocalDate(2023, 7, 31),
        "Autumn Sale", LocalDate(2023, 10, 1), LocalDate(2023, 10, 31),
    ).cast<Campaigns>()

    private val visits = dataFrameOf("date", "usedId")(
        LocalDate(2023, 1, 10), 1,
        LocalDate(2023, 1, 20), 2,
        LocalDate(2023, 4, 15), 1,
        LocalDate(2023, 5, 1), 3,
        LocalDate(2023, 7, 10), 2,
    ).cast<Visits>()

    class ColoredValue<T>(val value: T, val backgroundColor: RGBColor, val textColor: RGBColor)

    private val renderer = object : ChainedCellRenderer(DefaultCellRenderer) {
        override fun maybeContent(value: Any?, configuration: DisplayConfiguration): RenderedContent? {
            return if (value is ColoredValue<*>) {
                if (value.value is LocalDate) {
                    RenderedContent.text(DateTimeFormatter.ofPattern("dd MMMM yyyy").format(value.value.toJavaLocalDate()))
                } else {
                    renderValueForHtml(value.value, configuration.cellContentLimit, configuration.decimalFormat)
                }
            } else {
                null
            }
        }

        override fun maybeTooltip(value: Any?, configuration: DisplayConfiguration): String? {
            return null
        }
    }

    private fun AnyFrame.uwrapColoredValues(): AnyFrame {
        return convert { colsOf<ColoredValue<*>?>().rec() }.with(Infer.Type) { it?.value }
    }

    private fun <T> T.colored(background: RGBColor, text: RGBColor) = ColoredValue(this, background, text)
    private fun <T> T.winter(background: RGBColor = RGBColor(179, 205, 224), text: RGBColor = RGBColor(0, 0, 51)) = ColoredValue(this, background, text)
    private fun <T> T.spring(background: RGBColor = RGBColor(204, 235, 197), text: RGBColor = RGBColor(0, 51, 0)) = ColoredValue(this, background, text)
    private fun <T> T.summer(background: RGBColor = RGBColor(176, 224, 230), text: RGBColor = RGBColor(25, 25, 112)) = ColoredValue(this, background, text)
    private fun <T> T.autumn(background: RGBColor = RGBColor(221, 160, 221), text: RGBColor = RGBColor(85, 26, 139)) = ColoredValue(this, background, text)

    private val coloredCampaigns = dataFrameOf("name", "startDate", "endDate")(
        "Winter Sale".winter(), LocalDate(2023, 1, 1).winter(), LocalDate(2023, 1, 31).winter(),
        "Spring Sale".spring(), LocalDate(2023, 4, 1).spring(), LocalDate(2023, 4, 30).spring(),
        "Summer Sale".summer(), LocalDate(2023, 7, 1).summer(), LocalDate(2023, 7, 31).summer(),
        "Autumn Sale".autumn(), LocalDate(2023, 10, 1).autumn(), LocalDate(2023, 10, 31).autumn(),
    )

    private val coloredVisits = dataFrameOf("date", "usedId")(
        LocalDate(2023, 1, 10).winter(),	1.winter(),
        LocalDate(2023, 1, 20).winter(),	2.winter(),
        LocalDate(2023, 4, 15).spring(),	1.spring(),
        LocalDate(2023, 5, 1).colored(FormattingDSL.white, FormattingDSL.black), 3.colored(FormattingDSL.white, FormattingDSL.black),
        LocalDate(2023, 7, 10).summer(),	2.summer(),
    )

    private fun AnyFrame.toColoredHTML() = toHTML(
        getFooter = { null },
        cellRenderer = renderer,
        configuration = DisplayConfiguration.DEFAULT.copy(
            cellFormatter = { row, col ->
                val value = row[col]
                if (value is ColoredValue<*>) {
                    background(value.backgroundColor) and textColor(value.textColor)
                } else {
                    background(white)
                }
            }
        )
    )

    private val joinExpression: JoinedDataRow<Any?, Any?>.(it: JoinedDataRow<Any?, Any?>) -> Boolean = {
        right[{ "date"<ColoredValue<LocalDate>>() }].value in
            "startDate"<ColoredValue<LocalDate>>().value.."endDate"<ColoredValue<LocalDate>>().value
    }

    private fun snippetOutput(coloredResult: DataFrame<Any?>, result: DataFrame<Any?>) {
        coloredCampaigns.uwrapColoredValues() shouldBe campaigns
        coloredVisits.uwrapColoredValues() shouldBe visits
        coloredResult.uwrapColoredValues() shouldBe result

        fun DataFrameHtmlData.wrap(title: String): DataFrameHtmlData {
            return copy(
                body = """
                    <div class="table-container">
                        <b>$title</b>
                        $body
                    </div>
                """.trimIndent()
            )
        }

        PluginCallbackProxy.overrideHtmlOutput(
            manualOutput = DataFrameHtmlData.tableDefinitions()
                .plus(coloredCampaigns.toColoredHTML().wrap("campaigns"))
                .plus(coloredVisits.toColoredHTML().wrap("visits"))
                .plus(coloredResult.toColoredHTML().wrap("result"))
                .plus(
                    DataFrameHtmlData(
                        style = """
                            body {
                                display: flex;
                                align-items: flex-start;
                                overflow-x: auto;
                                font-family: "JetBrains Mono", SFMono-Regular, Consolas, "Liberation Mono", Menlo, Courier, monospace;
                                font-size: 14px;
                            }

                            :root {
                                color: #19191C;
                                background-color: #fff;
                            }
                            
                            :root[theme="dark"] {
                                background-color: #19191C;
                                color: #FFFFFFCC
                            }
                            
                            .table-container {
                                margin-right: 20px; 
                            }
                            
                            .table-container:not(:last-child) {
                                margin-right: 20px; 
                            }
                            
                            td {
                                white-space: nowrap;
                            }
                        """.trimIndent()
                    )
                )
        )
    }

    @TransformDataFrameExpressions
    @Test
    fun predicateJoin_accessors() {
        val result = run {
            // SampleStart
            val date by column<LocalDate>()
            val startDate by column<LocalDate>()
            val endDate by column<LocalDate>()

            campaigns.innerPredicateJoin(visits) {
                right[date] in startDate()..endDate()
            }
            // SampleEnd
        }
        val coloredResult = coloredCampaigns.innerPredicateJoin(coloredVisits, joinExpression = joinExpression)
        snippetOutput(coloredResult, result)
    }

    @TransformDataFrameExpressions
    @Test
    fun filterPredicateJoin_accessors() {
        val result = run {
            // SampleStart
            val date by column<LocalDate>()
            val startDate by column<LocalDate>()
            val endDate by column<LocalDate>()

            campaigns.filterPredicateJoin(visits) {
                right[date] in startDate()..endDate()
            }
            // SampleEnd
        }
        val coloredResult = coloredCampaigns.filterPredicateJoin(coloredVisits, joinExpression = joinExpression)
        snippetOutput(coloredResult, result)
    }

    @TransformDataFrameExpressions
    @Test
    fun leftPredicateJoin_accessors() {
        val result = run {
            // SampleStart
            val date by column<LocalDate>()
            val startDate by column<LocalDate>()
            val endDate by column<LocalDate>()

            campaigns.leftPredicateJoin(visits) {
                right[date] in startDate()..endDate()
            }
            // SampleEnd
        }
        val coloredResult = coloredCampaigns.leftPredicateJoin(coloredVisits, joinExpression = joinExpression)
        snippetOutput(coloredResult, result)
    }

    @TransformDataFrameExpressions
    @Test
    fun rightPredicateJoin_accessors() {
        val result = run {
            // SampleStart
            val date by column<LocalDate>()
            val startDate by column<LocalDate>()
            val endDate by column<LocalDate>()

            campaigns.rightPredicateJoin(visits) {
                right[date] in startDate()..endDate()
            }
            // SampleEnd
        }
        val coloredResult = coloredCampaigns.rightPredicateJoin(coloredVisits, joinExpression = joinExpression)
        snippetOutput(coloredResult, result)
    }

    @TransformDataFrameExpressions
    @Test
    fun fullPredicateJoin_accessors() {
        val result = run {
            // SampleStart
            val date by column<LocalDate>()
            val startDate by column<LocalDate>()
            val endDate by column<LocalDate>()

            campaigns.fullPredicateJoin(visits) {
                right[date] in startDate()..endDate()
            }
            // SampleEnd
        }
        val coloredResult = coloredCampaigns.fullPredicateJoin(coloredVisits, joinExpression = joinExpression)
        snippetOutput(coloredResult, result)
    }

    @TransformDataFrameExpressions
    @Test
    fun excludePredicateJoin_accessors() {
        val result = run {
            // SampleStart
            val date by column<LocalDate>()
            val startDate by column<LocalDate>()
            val endDate by column<LocalDate>()

            campaigns.excludePredicateJoin(visits) {
                right[date] in startDate()..endDate()
            }
            // SampleEnd
        }
        val coloredResult = coloredCampaigns.excludePredicateJoin(coloredVisits, joinExpression = joinExpression)
        snippetOutput(coloredResult, result)
    }

    @TransformDataFrameExpressions
    @Test
    fun predicateJoin_strings() {
        val result =
            // SampleStart
            campaigns.innerPredicateJoin(visits) {
                right[{ "date"<LocalDate>() }] in "startDate"<LocalDate>().."endDate"<LocalDate>()
            }
        // SampleEnd
        val coloredResult = coloredCampaigns.innerPredicateJoin(coloredVisits, joinExpression = joinExpression)
        snippetOutput(coloredResult, result)
    }

    @TransformDataFrameExpressions
    @Test
    fun filterPredicateJoin_strings() {
        val result =
            // SampleStart
            campaigns.filterPredicateJoin(visits) {
                right[{ "date"<LocalDate>() }] in "startDate"<LocalDate>().."endDate"<LocalDate>()
            }
        // SampleEnd
        val coloredResult = coloredCampaigns.filterPredicateJoin(coloredVisits, joinExpression = joinExpression)
        snippetOutput(coloredResult, result)
    }

    @TransformDataFrameExpressions
    @Test
    fun leftPredicateJoin_strings() {
        val result =
            // SampleStart
            campaigns.leftPredicateJoin(visits) {
                right[{ "date"<LocalDate>() }] in "startDate"<LocalDate>().."endDate"<LocalDate>()
            }
        // SampleEnd
        val coloredResult = coloredCampaigns.leftPredicateJoin(coloredVisits, joinExpression = joinExpression)
        snippetOutput(coloredResult, result)
    }

    @TransformDataFrameExpressions
    @Test
    fun rightPredicateJoin_strings() {
        val result =
            // SampleStart
            campaigns.rightPredicateJoin(visits) {
                right[{ "date"<LocalDate>() }] in "startDate"<LocalDate>().."endDate"<LocalDate>()
            }
        // SampleEnd
        val coloredResult = coloredCampaigns.rightPredicateJoin(coloredVisits, joinExpression = joinExpression)
        snippetOutput(coloredResult, result)
    }

    @TransformDataFrameExpressions
    @Test
    fun fullPredicateJoin_strings() {
        val result =
            // SampleStart
            campaigns.fullPredicateJoin(visits) {
                right[{ "date"<LocalDate>() }] in "startDate"<LocalDate>().."endDate"<LocalDate>()
            }
        // SampleEnd
        val coloredResult = coloredCampaigns.fullPredicateJoin(coloredVisits, joinExpression = joinExpression)
        snippetOutput(coloredResult, result)
    }

    @TransformDataFrameExpressions
    @Test
    fun excludePredicateJoin_strings() {
        val result =
            // SampleStart
            campaigns.excludePredicateJoin(visits) {
                right[{ "date"<LocalDate>() }] in "startDate"<LocalDate>().."endDate"<LocalDate>()
            }
        // SampleEnd
        val coloredResult = coloredCampaigns.excludePredicateJoin(coloredVisits, joinExpression = joinExpression)
        snippetOutput(coloredResult, result)
    }

    @TransformDataFrameExpressions
    @Test
    fun predicateJoin_properties() {
        val result =
            // SampleStart
            campaigns.innerPredicateJoin(visits) {
                right.date in startDate..endDate
            }
        // SampleEnd
        val coloredResult = coloredCampaigns.innerPredicateJoin(coloredVisits, joinExpression = joinExpression)
        snippetOutput(coloredResult, result)
    }

    @TransformDataFrameExpressions
    @Test
    fun filterPredicateJoin_properties() {
        val result =
            // SampleStart
            campaigns.filterPredicateJoin(visits) {
                right.date in startDate..endDate
            }
        // SampleEnd
        val coloredResult = coloredCampaigns.filterPredicateJoin(coloredVisits, joinExpression = joinExpression)
        snippetOutput(coloredResult, result)
    }

    @TransformDataFrameExpressions
    @Test
    fun leftPredicateJoin_properties() {
        val result =
            // SampleStart
            campaigns.leftPredicateJoin(visits) {
                right.date in startDate..endDate
            }
        // SampleEnd
        val coloredResult = coloredCampaigns.leftPredicateJoin(coloredVisits, joinExpression = joinExpression)
        snippetOutput(coloredResult, result)
    }

    @TransformDataFrameExpressions
    @Test
    fun rightPredicateJoin_properties() {
        val result =
            // SampleStart
            campaigns.rightPredicateJoin(visits) {
                right.date in startDate..endDate
            }
        // SampleEnd
        val coloredResult = coloredCampaigns.rightPredicateJoin(coloredVisits, joinExpression = joinExpression)
        snippetOutput(coloredResult, result)
    }

    @TransformDataFrameExpressions
    @Test
    fun fullPredicateJoin_properties() {
        val result =
            // SampleStart
            campaigns.fullPredicateJoin(visits) {
                right.date in startDate..endDate
            }
        // SampleEnd
        val coloredResult = coloredCampaigns.fullPredicateJoin(coloredVisits, joinExpression = joinExpression)
        snippetOutput(coloredResult, result)
    }

    @TransformDataFrameExpressions
    @Test
    fun excludePredicateJoin_properties() {
        val result =
            // SampleStart
            campaigns.excludePredicateJoin(visits) {
                right.date in startDate..endDate
            }
        // SampleEnd
        val coloredResult = coloredCampaigns.excludePredicateJoin(coloredVisits, joinExpression = joinExpression)
        snippetOutput(coloredResult, result)
    }

    @TransformDataFrameExpressions
    @Test
    fun crossProduct() {
        val result =
            // SampleStart
            campaigns.predicateJoin(visits) { true }
        // SampleEnd
        val coloredResult = coloredCampaigns.predicateJoin(coloredVisits) { true }
        snippetOutput(coloredResult, result)
    }
}
