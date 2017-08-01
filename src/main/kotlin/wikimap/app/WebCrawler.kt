package wikimap.app

import java.net.URL

class WebCrawler(seedUrl:String){
    var seedUrl:String? = null

    val maxDepth = 2
    val crawledUrls:MutableList<String> = mutableListOf()

    init{
        this.seedUrl = seedUrl
        println(downloadPage(seedUrl))
    }

    fun crawl(currentUrl:String, depth:Int){
        if(depth > maxDepth || crawledUrls.contains(currentUrl)) {
            return
        }else{
            crawledUrls.add(currentUrl)
            println(currentUrl + " " + depth)
            val rawPage = downloadPage(currentUrl)
        }
    }

    fun downloadPage(urlString:String):String{
        //val url = URL(urlString)
        //return url.readText()
        return ""
    }

}