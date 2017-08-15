package wikimap.suggestion

interface SuggestionProvider{
    fun getSuggestions(key: String): List<String>
}