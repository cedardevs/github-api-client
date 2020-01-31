
@Grab(group='org.yaml', module='snakeyaml', version='1.8')

import java.time.LocalDate
import groovy.json.JsonSlurper
import java.nio.charset.StandardCharsets
import org.yaml.snakeyaml.Yaml

Yaml parser = new Yaml()
def config = parser.load(("config.yaml" as File).text)

println config.username
//println config.password
println config.repo
println config.dateSince


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
writer << "<p><h3>Dates: ${config.dateSince} to ${today}</h3></p>\n\n"

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
            issueText << "<li>${issue.title.capitalize()} (${config.repo} <a href='${issue.html_url}' target='_blank'>#${issue.number}</a>)</li>\n"

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
writer << "<u>Epics:</u><br>\n"
writer << "<ul>"
epicList.each {
    writer << "${it}"
}
writer << "</ul>"

writer << "<br>\n"

writer << "<u>Closed Stories:</u><br>\n"
writer << "<ul>\n"
closedList.each {
    writer << "${it}"
}
writer << "</ul>\n"

writer << "<br>\n"

writer << "<u>Open Stories:</u><br>\n"
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