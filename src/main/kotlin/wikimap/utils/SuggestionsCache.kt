package wikimap.utils

import wikimap.app.SuggestionProvider

/**
 * Created by Basim on 07/08/2017.
 */
class SuggestionsCache(val provider: SuggestionProvider) : SuggestionProvider {

    private val cache = mutableMapOf<String, List<String>>()

    override fun getSuggestions(key: String): List<String> {
        return cache.getOrPut(key, { provider.getSuggestions(key) })
    }

}
