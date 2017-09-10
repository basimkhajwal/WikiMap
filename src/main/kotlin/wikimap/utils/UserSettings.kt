package wikimap.utils

import java.util.prefs.Preferences

/**
 * Created by Basim on 09/09/2017.
 */
object UserSettings {

    private val prefs = Preferences.userRoot().node(javaClass.name)

    private val FILE_LEN = "FileLength"
    private fun fileNum(n: Int) = "file#$n"

    fun getRecentFiles(): List<String> {
        val len = prefs.getInt(FILE_LEN, 0)

        return (0..(len-1))
            .map { prefs.get(fileNum(it), "") }
            .filter { it.isNotEmpty() }
    }
}