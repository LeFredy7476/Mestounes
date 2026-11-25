package dev.lefredy.mestounes.utils

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun currentTimestamp(): Long {
    return Clock.System.now().toEpochMilliseconds()
}