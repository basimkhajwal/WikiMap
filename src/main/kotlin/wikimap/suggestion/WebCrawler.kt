package wikimap.suggestion

import java.net.URL

class WebCrawler(private val seedUrl: String) {

    fun crawl(currentUrl: String, depth: Int): List<String>{
        if (depth <= 0) return listOf(currentUrl)

        val rawPage = downloadPage(currentUrl)
        val intro = extractIntro(rawPage)

        val allLinks = listOf(currentUrl) +
            extractLinks(intro)
                .filter { validateLink(it) }
                .flatMap { crawl(completeLink(it), depth - 1) }

        return allLinks.distinct()
    }

    fun getSiteSearchResultLinks(searchTerm: String): List<String> {
        val searchUrl = seedUrl + "/w/index.php?search=" + searchTerm.replace(" ", "+").replace("_", "+")
        val searchResults = downloadPage(searchUrl)

        val resultsStart = searchResults.indexOf("<ul class=\"mw-search-results\">")
        val resultsEnd = searchResults.indexOf("</ul>", resultsStart + 1)

        return extractLinks(searchResults.substring(resultsStart, resultsEnd))
    }

    private fun downloadPage(urlString: String): String = URL(urlString).readText()

    private fun extractIntro(rawPage: String): String{
        val startString = "<div class=\"mw-parser-output\""
        val endString =
            when {
                rawPage.contains("<div id=\"toc\"") -> "<div id=\"toc\""
                rawPage.contains("<h2") -> "<h2"
                else -> "</p>"
            }

        return rawPage.substring(rawPage.indexOf(startString), rawPage.indexOf(endString))
    }

    private fun extractLinks(str: String): List<String> {
        val regex = Regex("<a href=[\"'](.*?)[\"']")
        return regex.findAll(str).map { it.groups[1]!!.value }.toList()
    }

    private fun completeLink(link: String): String = if (link.contains(seedUrl)) link else seedUrl + link

    private fun validateLink(link: String): Boolean =
        !link.contains("#") &&
        !link.contains("File:") &&
        (!link.contains("https://") || link.contains(seedUrl))
}
