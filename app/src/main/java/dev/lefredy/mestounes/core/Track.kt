package dev.lefredy.mestounes.core

data class Track (
    val trackId: Long, // works inside the on device database
    val sourceId: Long, // refer to the source used
    val distantId: Long, // works within the concerned source
    val path: String, // relative path to the source root
    var title: String, // if missing, default to filename
    var artist: List<String>, // if empty, shows "unknown" in UI
    var album: String, // if missing, default to parent directory name
    var albumartist: String?, // if missing, default to first item in artist list if available.
    var composer: String? = null, // if missing, default to albumartist
    var date: Int? = null, // if null, shows "unknown" in UI
    var genre: List<String> = listOf<String>(), // if empty, shows "unknown" in UI
    var tracknumber: Int = 0, // if missing, default to 0
    var discnumber: Int = 0, // if missing, default to 0
    var comment: String = "", // can be empty. PS: comments are static to the original file and not user-defined
    var updated_at: Long = System.currentTimeMillis(), // or Int, whatever fits a timestamp
)