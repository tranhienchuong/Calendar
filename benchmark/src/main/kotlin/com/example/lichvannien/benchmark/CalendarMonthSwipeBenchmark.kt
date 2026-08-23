package com.example.lichvannien.benchmark

import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.regex.Pattern

@LargeTest
@RunWith(AndroidJUnit4::class)
class CalendarMonthSwipeBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @OptIn(ExperimentalMetricApi::class)
    @Test
    fun swipeToNextMonth() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        iterations = ITERATIONS,
        setupBlock = {
            pressHome()
            startActivityAndWait()
            requireCalendarHeader(device)
        },
    ) {
        val headerBeforeSwipe = requireCalendarHeader(device)
        val expectedHeader = headerBeforeSwipe.next()

        val y = (device.displayHeight * 0.55f).toInt()
        device.swipe(
            (device.displayWidth * 0.80f).toInt(),
            y,
            (device.displayWidth * 0.20f).toInt(),
            y,
            SWIPE_STEPS,
        )

        check(device.wait(Until.hasObject(By.text(expectedHeader.asText())), HEADER_TIMEOUT_MS)) {
            "Expected ${expectedHeader.asText()} after the month swipe, but CalendarScreen did not reach it."
        }
    }

    private fun requireCalendarHeader(device: UiDevice): MonthHeader {
        check(device.wait(Until.hasObject(By.text(HEADER_PATTERN)), HEADER_TIMEOUT_MS)) {
            "CalendarScreen header was not found. Complete onboarding before running the benchmark."
        }
        val headerObject = checkNotNull(device.findObject(By.text(HEADER_PATTERN)))
        val text = checkNotNull(headerObject.text) { "CalendarScreen header has no text." }
        val match = checkNotNull(HEADER_PATTERN.matcher(text).takeIf { it.matches() }) {
            "Unexpected calendar header: $text"
        }
        return MonthHeader(
            month = checkNotNull(match.group(1)).toInt(),
            year = checkNotNull(match.group(2)).toInt(),
        )
    }

    private data class MonthHeader(val month: Int, val year: Int) {
        fun next(): MonthHeader = if (month == 12) MonthHeader(month = 1, year = year + 1) else MonthHeader(month + 1, year)

        fun asText(): String = "Tháng $month, $year"
    }

    private companion object {
        const val TARGET_PACKAGE = "com.example.lichvannien"
        const val ITERATIONS = 10
        const val HEADER_TIMEOUT_MS = 5_000L
        const val SWIPE_STEPS = 60
        val HEADER_PATTERN: Pattern = Pattern.compile("Tháng\\s+(\\d{1,2})\\s*,\\s*(\\d{4})")
    }
}
