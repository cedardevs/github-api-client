import org.apache.tools.bzip2.CBZip2OutputStream

@Grab(group='org.yaml', module='snakeyaml', version='1.8')
@Grab(group = 'com.atlassian.commonmark', module = 'commonmark', version = '0.14.0')

import java.time.LocalDate
import groovy.json.JsonSlurper
import java.nio.charset.StandardCharsets
import org.yaml.snakeyaml.Yaml
import org.commonmark.node.*
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

Yaml parser = new Yaml()
def config = parser.load(("config.yaml" as File).text)

println "username: ${config.username}"
//println "password: ${config.password}"
println "repo: ${config.repo}"
println "dateSince: ${config.dateSince}"
println "includeBody: ${config.includeBody}"
println "useHeaders: ${config.useHeaders}"
println '-' * 10

Parser mdParser // Markdown parser
Node docuemnt
HtmlRenderer renderer
if (config.includeBody) {
    mdParser = Parser.builder().build()
    renderer = HtmlRenderer.builder().build()
}

// Section text
String startOfEpics = "<u>Epics:</u><br>\n"
String startOfClosedStories = "<u>Closed Stories:</u><br>\n"
String startOfOpenStories = "<u>Open Stories:</u><br>\n"
Boolean boldIssueTitles = false
if (config.useHeaders) {
    startOfEpics = "<h2>Epics:</h2>\n"
    startOfClosedStories = "<h2>Closed Stories:</h2>\n"
    startOfOpenStories = "<h2>Open Stories:</h2>\n"
    boldIssueTitles = true
}

def page = 1

def queryParams = ""

def nextPage = true

File file = new File("${config.repo}Issues.html")
def writer = file.newWriter()
def totCnt = 0
def today = LocalDate.now().toString()

writer << "<!DOCTYPE HTML>\n"
writer << "<html>\n"
writer << "<head><title>Github Report</title></head>\n"
writer << "<body>\n"
writer << "<p><h1>Sprint Recap for ${config.repo}</h1></p>\n"
writer << "<p><b>Dates: ${config.dateSince} to ${today}</b></p>\n\n"

def openList = []
def closedList = []
def epicList = []
while ( nextPage ) {

    def storyCnt = 0
    def epicCnt = 0

    def baseURL = "https://api.github.com/repos/cedardevs/${config.repo}/issues?state=all&since=${config.dateSince}&sort=created&direction=asc&page=${page}&per_page=100"
    def connection = new URL( baseURL ).openConnection() as HttpURLConnection
    // set some headers
    connection.setRequestProperty( 'User-Agent', 'groovy-2.4.x' )
    connection.setRequestProperty( 'Accept', 'application/json' )
    //BasicAuth
    String encoded = Base64.getEncoder().encodeToString((config.username+":"+config.password).getBytes(StandardCharsets.UTF_8))  //Java 8
    connection.setRequestProperty("Authorization", "Basic "+encoded)

    // Personal Access Token, acts as OAuth for development usage
//    String tokenParam = "${config.personalAccessToken}:x-oauth-basic"
    //String authString = "Basic" + Base64.getEncoder().encodeToString(tokenParam.getBytes(StandardCharsets.UTF_8))  //Java 8
//    String authString = "Basic" + Base64.getEncoder().encodeToString(tokenParam.getBytes())  //Java 8
//    connection.setRequestProperty("Authorization", authString)


    if ( connection.responseCode == 200 ) {
        
        // get the JSON response
        def json = connection.inputStream.withCloseable { inStream ->
        
            new JsonSlurper().parse( inStream as InputStream )
        
        }

        // extract some data from the JSON array, printing a report
        def issueText

        json.each { issue ->
            storyCnt++
            issueText = new StringBuffer("")
            issueText << "<li>"
            if (boldIssueTitles) {
                issueText << "<b>${issue.title.capitalize()}</b>"
            }
            else {
                issueText << "${issue.title.capitalize()}"
            }
            issueText << " (${config.repo} <a href='${issue.html_url}' target='_blank'>#${issue.number}</a>)</li>\n"
            if (config.includeBody && issue.body) {
                document = mdParser.parse(issue.body)
                String issueHtml = renderer.render(document)
                issueText << "<dd><table style='width: 100%; border: 1px solid black; background-color: #eee;'><tr><td>${issueHtml}</td></tr></table></dd>\n"
            }

            // write out just epics
            if ("${issue.title}".contains("EPIC")) {
                epicList.add( issueText )

            } else {
                if ("${issue.state}"=="open") {
                    openList.add( issueText )
                } else {
                    closedList.add( issueText )
                }
            }

            totCnt++
        }


        writer << "<br>\n"
        writer.flush()
        
    } else {

        println "Request failed:" + connection.responseCode
        return true 

    }
    
    json = null
    connection = null
    println "storyCnt=${storyCnt}, page=${page}"
    page++
    if ( storyCnt.intValue()==0 || page > 5) nextPage = false

    
}
writer << startOfEpics
writer << "<ul>"
epicList.each {
    writer << "${it}"
}
writer << "</ul>"

writer << "<br>\n"

writer << startOfClosedStories
writer << "<ul>\n"
closedList.each {
    writer << "${it}"
}
writer << "</ul>\n"

writer << "<br>\n"

writer << startOfOpenStories
writer << "<ul>\n"
openList.each {
    writer << "${it}"
}
writer << "</ul>\n"

writer << "<b>Total Issues: ${totCnt}</b><br>\n"

writer << "</body>\n"
writer << "</html>\n"
writer.close()

println "Total Issues: ${totCnt}\n"