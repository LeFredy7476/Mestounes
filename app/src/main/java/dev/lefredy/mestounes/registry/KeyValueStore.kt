import java.io.File
    import java.util.concurrent.ConcurrentHashMap
    import java.util.concurrent.locks.ReentrantLock
    import kotlin.concurrent.withLock

    class NoHistoryKvStore(private val file: File) {
        // ConcurrentHashmap laisse la possibilite d acceder a plusieur cle valeur en meme temps
        private val memory = ConcurrentHashMap<String, String>()
        // RentrantLock() verouille les cles en  court de lecture ou changement
        private val lock = ReentrantLock()
        //
        init {
            loadFromDisk()
        }

        fun put(key: String, value: String) {
            memory[key] = value
            saveToDisk()
        }

        fun get(key: String): String? = memory[key]

        fun delete(key: String): Boolean {
            val removed = memory.remove(key) != null
            if (removed) saveToDisk()
            return removed
        }

        private fun loadFromDisk() {
            if (!file.exists()) return
            file.forEachLine { line ->
                val idx = line.indexOf('=')
                if (idx != -1) {
                    memory[line.substring(0, idx)] = line.substring(idx + 1)
                }
            }
        }

        private fun saveToDisk() = lock.withLock {
            file.bufferedWriter().use { writer ->
                memory.forEach { (key, value) ->
                    writer.write("$key=$value\n")
                }
            }
        }
    }