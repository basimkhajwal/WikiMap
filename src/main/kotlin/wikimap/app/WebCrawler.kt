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
            val intro = extractIntro(rawPage)

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

    fun downloadPage(urlString:String):String{
        val url = URL(urlString)
        return url.readText()
    }

    fun extractIntro(rawPage:String):String{
        val introStartIndex = rawPage.indexOf("<p>")
        var introStopIndex = rawPage.indexOf("<div id=\"toctitle\"", introStartIndex + 1)

        if (introStopIndex == -1){
            introStopIndex = rawPage.indexOf("</p>", introStartIndex + 1)
        }

        val rawIntro = rawPage.slice(IntRange(introStartIndex, introStopIndex - 1))
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