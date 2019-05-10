
@Grab(group='org.yaml', module='snakeyaml', version='1.8')


import groovy.json.JsonSlurper
import java.nio.charset.StandardCharsets
import org.yaml.snakeyaml.Yaml

Yaml parser = new Yaml()
def config = parser.load(("config.yaml" as File).text)

println config.username
println config.password
println config.repo
println config.dateSince


def page = 1

def queryParams = ""

def nextPage = true

File file = new File("${config.repo}Issues.html")
def writer = file.newWriter()
def totCnt = 0
def storyList = []
def epicList = []

while ( nextPage ) {
    def storyCnt = 0
    def epicCnt = 0

    def baseURL = "https://api.github.com/repos/cedardevs/${config.repo}/issues?state=closed&since=${config.dateSince}&sort=created&direction=asc&page=${page}&per_page=100"
    def connection = new URL( baseURL ).openConnection() as HttpURLConnection
    // set some headers
    connection.setRequestProperty( 'User-Agent', 'groovy-2.4.x' )
    connection.setRequestProperty( 'Accept', 'application/json' )
    //BasicAuth
    String encoded = Base64.getEncoder().encodeToString((config.username+":"+config.password).getBytes(StandardCharsets.UTF_8))  //Java 8
    connection.setRequestProperty("Authorization", "Basic "+encoded)

    writer << "<!DOCTYPE HTML>\n"
    writer << "<html>\n"
    writer << "<head><title>Github Report</title></head>\n"
    writer << "<body>\n"
    writer << "<p><b>Issue report for the ${config.repo} repo</b></p>\n"
    writer << "<p><b><i>Work completed since ${config.dateSince}</i></b></p>\n\n"


    if ( connection.responseCode == 200 ) {
        
        // get the JSON response
        def json = connection.inputStream.withCloseable { inStream ->
        
            new JsonSlurper().parse( inStream as InputStream )
        
        }

        // extract some data from the JSON array, printing a report
        def issueText

        json.each { issue ->

            issueText = new StringBuffer("")

            issueText << "<li>${issue.title.capitalize()} (${config.repo} <a href='${issue.html_url}' target='_blank'>#${issue.number}</a>)</li>\n"
            //issueText << "  Status: ${issue.state}<br>\n"
            //issueText << "  Status: ${issue.url}<br>\n"
            //issueText << "  Description: ${issue.body}<br>\n"
            //issueText << "  Create: ${issue.created_at}<br>\n"
            //issueText << "  Labels: ${issue.labels.name}<br>\n"
            //issueText << "  Closed: ${issue.closed_at}<br><br>\n\n"


            // write out just epics
            if ("${issue.title}".contains("EPIC")) {
                epicList.add( issueText )

            } else {
                storyList.add( issueText )
            }

            totCnt++
        }
        writer << "<u>Epics:</u><br>\n"
        writer << "<ul>"
        epicList.each {
            writer << "${it}"
        }
        writer << "</ul>"

        writer << "<br>\n"

        writer << "<u>Stories:</u><br>\n"
        writer << "<ul>\n"
        storyList.each {
            writer << "${it}"
        }
        writer << "</ul>\n"
        writer.flush()


        writer << "<br>\n"
        
    } else {

        println "Request failed:" + connection.responseCode
        return true 

    }
    
    json = null
    connection = null
    
    page++
    if ( storyCnt < 100 || page > 20) nextPage = false
    
}
writer << "<b>Total Issues: ${totCnt}</b><br>\n"

writer << "</body>\n"
writer << "</html>\n"
writer.close()

println "Total Issues: ${totCnt}\n"