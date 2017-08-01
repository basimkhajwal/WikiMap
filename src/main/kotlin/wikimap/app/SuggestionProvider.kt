package wikimap.app

interface SuggestionProvider{
    fun getSuggestions(key: String): List<String>
}