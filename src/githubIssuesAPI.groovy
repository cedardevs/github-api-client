
@Grab(group='org.yaml', module='snakeyaml', version='1.8')


import groovy.json.JsonSlurper
import java.nio.charset.StandardCharsets
import org.yaml.snakeyaml.Yaml

Yaml parser = new Yaml()
def credential = parser.load(("user.yaml" as File).text)

println credential.username
println credential.password

//System.exit(-1)
def page = 1

def queryParams = ""

def nextPage = true

def repo = "onestop"
def epicFilter = false
def dateSince = "2018-11-20"

File file = new File("${repo}Issues.txt")
def writer = file.newWriter()
def totCnt = 0


while ( nextPage ) {
    def storyCnt = 0
    def epicCnt = 0

    def baseURL = "https://api.github.com/repos/cedardevs/${repo}/issues?state=closed&since=${dateSince}&sort=created&direction=asc&page=${page}&per_page=100"
    def connection = new URL( baseURL ).openConnection() as HttpURLConnection
    // set some headers
    connection.setRequestProperty( 'User-Agent', 'groovy-2.4.x' )
    connection.setRequestProperty( 'Accept', 'application/json' )
    //BasicAuth
    String encoded = Base64.getEncoder().encodeToString((credential.username+":"+credential.password).getBytes(StandardCharsets.UTF_8));  //Java 8
    connection.setRequestProperty("Authorization", "Basic "+encoded);

    writer << "Github issue report for ${repo}\n"
    writer << "Closed issues since ${dateSince}\n\n"


    if ( connection.responseCode == 200 ) {
        
        // get the JSON response
        def json = connection.inputStream.withCloseable { inStream ->
        
            new JsonSlurper().parse( inStream as InputStream )
        
        }

        // extract some data from the JSON array, printing a report
        def issueText

        json.each { issue ->

            issueText = new StringBuffer("")

            issueText << "Title: ${issue.title}\n"
            issueText << "  Status: ${issue.state}\n"
            issueText << "  Status: ${issue.url}\n"
            issueText << "  Description: ${issue.body}\n"
            issueText << "  Create: ${issue.created_at}\n"
            issueText << "  Labels: ${issue.labels.name}\n"
            issueText << "  Closed: ${issue.closed_at}\n\n"

            if ( !epicFilter ) {

                // write out all issues
                writer << issueText

            } else {

                // write out just epics
                if ("${issue.title}".contains("EPIC")) {
                    writer << issueText
                    epicCnt++
                }

            }
            storyCnt++
            totCnt++
            writer.flush()
        }
        
        writer << "\n\n"
        if ( epicFilter ) writer << "Epic Count: ${epicCnt}\n"
        //writer << "Story Count: ${storyCnt}\n"
        //writer << "Page Count: ${page}\n"
        
    } else {

        println "Request failed:" + connection.responseCode
        return true 

    }
    
    json = null
    connection = null
    
    page++
    if ( storyCnt < 100 || page > 20) nextPage = false
    
}
writer << "Total Issues: ${totCnt}\n"
writer.close()

println "Total Issues: ${totCnt}\n"