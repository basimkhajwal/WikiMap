package wikimap.app

class BasicSuggestionProvider{
    val seedWebsite = "https://en.wikipedia.org"

    var crawler = WebCrawler(seedWebsite)

    init {

    }
}