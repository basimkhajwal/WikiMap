package wikimap.app

class BasicSuggestionProvider : SuggestionProvider{
    val seedUrl = "https://en.wikipedia.org"

    override fun getSuggestions(key:String):List<String>{
        val link = expandLink(key)

        val crawler = WebCrawler(seedUrl)
        val suggestedLinks = crawler.crawl(link, 1)

        return extractArticleNames(suggestedLinks)
    }

    fun expandLink(articleName:String):String{
        return seedUrl + "/wiki/" + articleName.replace(" ", "_")
    }

    fun extractArticleNames(links:List<String>):List<String>{
        val names = mutableListOf<String>()

        for (link in links){
            val name = link
                    .substring(seedUrl.length  + "/wiki/".length)
                    .replace("_", " ")
            names.add(name)
        }

        return names
    }
}