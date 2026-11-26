package dev.lefredy.mestounes.ui

import dev.lefredy.mestounes.core.Track

// TODO: Give each enum entry a display label (just like MainTab in MainScreen.kt)
//       and optionally a flag like `val supportsDirection: Boolean`
enum class TrackSortOption(
    val label: String,
    val supportsDirection: Boolean,
) {
    Alphabetical("A–Z", supportsDirection = true),
    ReleaseDate("Release Date", supportsDirection = true),
    RecentlyPlayed("Recent", supportsDirection = false),
    MostPlayed("Most Played", supportsDirection = false),
}

enum class SortDirection {
    Ascending,
    Descending,
}

/**
 * One section in the TrackList.
 * - For Alphabetical / ReleaseDate: `header` is the first letter ("A", "#") or year ("2024", "Unknown").
 * - For RecentlyPlayed / MostPlayed: `header` can be null (no inline headers needed).
 */
data class TrackSection(
    val header: String?,
    val tracks: List<Track>,
)

fun sortAndGroupTracks(
    tracks: List<Track>,
    sortOption: TrackSortOption,
    direction: SortDirection,
): List<TrackSection> {
    if (tracks.isEmpty()){
        return emptyList();
    }

    return when (sortOption){
        TrackSortOption.Alphabetical -> {
            val sorted : List<Track> = if (direction == SortDirection.Ascending)
            {
                tracks.sortedBy { it.title.lowercase() }
            } else {
                tracks.sortedByDescending { it.title.lowercase() }
            }

            sorted.groupBy { track ->
                val first = track.title.trim().firstOrNull()
                if (first != null && first.isLetter()) first.uppercase() else "#"
            }.map { (header, groupTracks) -> TrackSection(header, groupTracks) }

        }
        TrackSortOption.ReleaseDate -> {
            val sorted : List<Track> = if (direction == SortDirection.Ascending)
            {
                tracks.sortedBy { it.date }
            } else {
                tracks.sortedByDescending { it.date }
            }

            sorted.groupBy { it.date?.toString() ?: "Unknown"
            }.map { (header, groupTracks) -> TrackSection(header, groupTracks) }

        }
        TrackSortOption.RecentlyPlayed -> listOf(TrackSection(null, tracks))
        TrackSortOption.MostPlayed -> listOf(TrackSection(null, tracks))
    }

}