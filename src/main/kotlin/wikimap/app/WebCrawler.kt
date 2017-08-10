package wikimap.app

import java.net.URL
import javax.xml.ws.WebEndpoint

class WebCrawler(val seedUrl:String, val maxDepth:Int){
    val crawledUrls:MutableList<String> = mutableListOf()

    fun crawl(currentUrl:String, depth:Int):List<String>{
        if(depth > maxDepth || crawledUrls.contains(currentUrl)) {
            return listOf()
        }else{
            crawledUrls.add(currentUrl)
            println(currentUrl + " " + depth)
            val rawPage = downloadPage(currentUrl)
            val intro = extractIntro(getArticleNameFromUrl(currentUrl), rawPage)

            val introLinks = extractLinks(intro)

            for (link in introLinks){
                if (validateLink(link)){
                    if (linkIsComplete(link)){
                        crawl(link, depth + 1)
                    }
                    else{
                        crawl(seedUrl + link, depth + 1)
                    }
                }
            }

            return crawledUrls
        }
    }

    fun getSiteSearchResultLinks(searchTerm:String):List<String>{
        val searchUrl = seedUrl + "/w/index.php?search=" + searchTerm.replace(" ", "+").replace("_", "+")

        val searchResultsPage = downloadPage(searchUrl)
        val resultLinks = extractSearchResultsLinks(searchResultsPage)
        return resultLinks
    }

    fun getArticleNameFromUrl(url:String):String{
        val startIndex = url.indexOf("/wiki/") + "/wiki/".length
        return url.substring(startIndex).replace("_", " ")
    }

    fun extractSearchResultsLinks(fullPage:String):List<String>{
        val resultsStart = fullPage.indexOf("<ul class='mw-search-results'>")
        val resultsEnd = fullPage.indexOf("</ul>", resultsStart + 1)

        val results = fullPage.slice(IntRange(resultsStart, resultsEnd - 1))

        return extractLinks(results)
    }

    fun downloadPage(urlString:String):String{
        val url = URL(urlString)
        return url.readText()
    }

    fun extractIntro(articleName:String, rawPage:String, searchStartIndex:Int = 0):String{
        val introStartIndex = rawPage.indexOf("<p>")
        var introStopIndex = rawPage.indexOf("<div id=\"toctitle\"", introStartIndex + 1)

        if (introStopIndex == -1){
            introStopIndex = rawPage.indexOf("</p>", introStartIndex + 1)
        }

        var rawIntro = rawPage.slice(IntRange(introStartIndex, introStopIndex - 1))

        if (rawIntro.indexOf(articleName) == -1){
            rawIntro =  extractIntro(articleName, rawPage, introStopIndex)
        }

        return rawIntro
    }

    fun extractLinks(str:String):MutableList<String>{
        var searchIndex = 0

        val linkList = mutableListOf<String>()

        while (true){
            val startLink = str.indexOf("<a href", searchIndex)

            if (startLink == -1)
                break
            else{
                val startQuote = str.indexOf("\"", startLink + 1)
                val endQuote = str.indexOf("\"", startQuote + 1)

                linkList.add(str.substring(startQuote + 1, endQuote))
                searchIndex = endQuote
            }
        }

        return linkList
    }

    fun linkIsComplete(link:String):Boolean{
        return link.contains(seedUrl)
    }

    fun validateLink(link:String):Boolean{
        if (link.contains("#")){
            return false
        }else{
            return (!link.contains("https://") || link.contains(seedUrl))
        }
    }
}

fun main(args:Array<String>){
    var crawler = WebCrawler("https://en.wikipedia.org", 1)
    print(crawler.crawl("https://en.wikipedia.org/iki/Egypt", 0))
}