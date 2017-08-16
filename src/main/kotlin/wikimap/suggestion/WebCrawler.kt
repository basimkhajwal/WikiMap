package wikimap.suggestion

import java.net.URL

class WebCrawler(val seedUrl: String) {

    fun crawl(currentUrl: String, depth: Int): List<String>{
        if (depth < 0) return listOf(currentUrl)

        val rawPage = downloadPage(currentUrl)
        val intro = extractIntro(rawPage)

        return extractLinks(intro)
            .filter { validateLink(it) }
            .flatMap { crawl(completeLink(it), depth - 1) }
            .toSet().toList()
    }

    fun getSiteSearchResultLinks(searchTerm: String): List<String> {
        val searchUrl = seedUrl + "/w/index.php?search=" + searchTerm.replace(" ", "+").replace("_", "+")
        val searchResultsPage = downloadPage(searchUrl)

        return extractSearchResultsLinks(searchResultsPage)
    }

    fun extractSearchResultsLinks(fullPage: String): List<String> {
        val resultsStart = fullPage.indexOf("<ul class='mw-search-results'>")
        val resultsEnd = fullPage.indexOf("</ul>", resultsStart + 1)

        val results = fullPage.substring(resultsStart, resultsEnd)
        return extractLinks(results)
    }

    fun downloadPage(urlString: String): String = URL(urlString).readText()

    fun extractIntro(rawPage: String): String{
        val startString = "<div class=\"mv-parser-output\""
        val endString = "<div id=\"toc\""

        return rawPage.substring(rawPage.indexOf(startString), rawPage.indexOf(endString))
    }

    fun extractLinks(str: String): List<String> {
        val regex = Regex("<a href=[\"'](.*?)[\"']")
        return regex.findAll(str).map { it.groups[1]!!.value }.toList()
    }

    fun completeLink(link: String): String = if (link.contains(seedUrl)) link else seedUrl + link

    fun validateLink(link: String): Boolean{
        return !link.contains("#") && (!link.contains("https://") || link.contains(seedUrl))
    }
}
