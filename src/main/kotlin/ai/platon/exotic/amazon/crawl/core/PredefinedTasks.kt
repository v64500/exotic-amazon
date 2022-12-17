package ai.platon.exotic.amazon.crawl.core

import ai.platon.pulsar.common.DateTimes
import ai.platon.pulsar.common.Priority13
import java.time.Duration
import java.time.Instant

/**
 * TODO: configurable
 * */
enum class PredefinedTask(
    val label: String,
    var priority: Priority13,

    /**
     * The task period
     * */
    var taskPeriod: Duration,
    /**
     * The expiry time of each urls
     * */
    var expires: Duration,

    /**
     * The dead time of tasks
     * */
    var deadTime: () -> Instant,
    /**
     * The start time to collect
     * */
    var startTime: () -> Instant,
    /**
     * The end time to collect, the collector should be removed if it passes the end time
     * */
    var endTime: () -> Instant,
    /**
     * The file name pattern
     * */
    var fileName: String? = null,
    /**
     * Ignore the run time restriction, if set to true, the task can run at any time
     * */
    var ignoreTTL: Boolean = false,
    /**
     * Refresh pages
     * */
    var refresh: Boolean = false,
    /**
     * Do not store the pages' content if they are large
     * */
    var storeContent: Boolean = false
) {
    MOVERS_AND_SHAKERS("movers-and-shakers", Priority13.HIGHER3,
        Duration.ofHours(1),
        Duration.ofHours(1),
        deadTime = { DateTimes.endOfHour() },
        startTime = { DateTimes.startOfHour() },
        endTime = { DateTimes.endOfHour() },
        "movers-and-shakers.txt"
    ),

    BEST_SELLERS("zgbs", Priority13.NORMAL,
        Duration.ofDays(1),
        Duration.ofDays(1),
        deadTime = { DateTimes.endOfDay() },
        startTime = { DateTimes.timePointOfDay(9) },
        endTime = { DateTimes.timePointOfDay(23, 30) },
        "best-sellers.txt",
        storeContent = true
    ),
    MOST_WISHED_FOR("most-wished-for", Priority13.NORMAL,
        Duration.ofDays(1),
        Duration.ofDays(1),
        deadTime = { DateTimes.endOfDay() },
        startTime = { DateTimes.startOfDay() },
        endTime = { DateTimes.timePointOfDay(23, 30) },
        "most-wished-for.txt",
        storeContent = true
    ),
    NEW_RELEASES("new-releases", Priority13.NORMAL,
        Duration.ofDays(1),
        Duration.ofDays(1),
        deadTime = { DateTimes.endOfDay() },
        startTime = { DateTimes.startOfDay() },
        endTime = { DateTimes.timePointOfDay(23, 30) },
        "new-releases.txt",
        storeContent = true
    ),

    ASIN(
        "asin", Priority13.LOWER2,
        Duration.ofDays(1),
        Duration.ofDays(30),
        deadTime = { DateTimes.endOfDay() },
        startTime = { DateTimes.timePointOfDay(1) },
        endTime = { DateTimes.timePointOfDay(23, 50) },
    ),

    REVIEW(
        "review", Priority13.LOWER3,
        Duration.ofDays(1),
        Duration.ofDays(300),
        deadTime = { DateTimes.endOfDay().plusSeconds(DateTimes.SECONDS_PER_DAY) },
        startTime = { DateTimes.timePointOfDay(23, 30) },
        endTime = { DateTimes.endOfDay().plusSeconds(DateTimes.SECONDS_PER_DAY) },
    ),

    BEST_SELLERS7D("zgbs", Priority13.NORMAL,
        Duration.ofDays(7),
        Duration.ofDays(7),
        deadTime = { DateTimes.endOfDay() },
        startTime = { DateTimes.startOfDay() },
        endTime = { Instant.now().plusSeconds(7 * 24 * 3600) },
        "best-sellers.txt",
        storeContent = true
    ),
}

fun PredefinedTask.toResidentTask() = ResidentTask(ordinal, name, label, priority,
    taskPeriod, expires, deadTime, startTime, endTime, fileName,
    ignoreTTL = ignoreTTL, refresh = refresh, storeContent = storeContent)
