package wikimap.app

import java.io.FileNotFoundException

class BasicSuggestionProvider : SuggestionProvider{
    val seedUrl = "https://en.wikipedia.org"
    val crawler = WebCrawler(seedUrl, 1)

    override fun getSuggestions(key:String):List<String>{
        val link = expandLink(key)

        var suggestedLinks:List<String>

        try {
            suggestedLinks = crawler.crawl(link, 0)
        }catch (ex:FileNotFoundException){
            val searchTerm = extractArticleNames(listOf(link)).first()
            suggestedLinks = crawler.getSiteSearchResultLinks(searchTerm)
        }

        return extractArticleNames(suggestedLinks)
    }

    fun expandLink(articleName:String):String{
        return seedUrl + "/wiki/" + articleName.replace(" ", "_")
    }

    fun extractArticleNames(links:List<String>):List<String>{
        val names = mutableListOf<String>()

        for (link in links){

            var name:String

            if (link.contains(seedUrl)){
                name = link
                        .substring(seedUrl.length  + "/wiki/".length)
                        .replace("_", " ")

            }else{
                name = link
                        .substring("/wiki/".length)
                        .replace("_", " ")
            }
            names.add(name)
        }

        return names
    }
}

fun main(args:Array<String>){
    val p = BasicSuggestionProvider()
    print(p.getSuggestions("13th century rebels"))

}