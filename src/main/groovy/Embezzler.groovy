@Grab(group='org.codehaus.sonar', module='sonar-ws-client', version='4.0')
@Grab(group='commons-httpclient', module='commons-httpclient', version='3.1')

import org.sonar.wsclient.Sonar
import org.sonar.wsclient.SonarClient
import org.sonar.wsclient.issue.IssueClient
import org.sonar.wsclient.issue.IssueQuery
import org.sonar.wsclient.issue.Issues
import org.sonar.wsclient.issue.Issue
import org.sonar.wsclient.services.Resource
import org.sonar.wsclient.services.ResourceQuery
import org.sonar.wsclient.services.Measure
import org.sonar.wsclient.user.User
import org.sonar.wsclient.user.UserClient
import org.sonar.wsclient.user.UserQuery

String helpMessage = 'groovy Embezzler.groovy -s http://localhost:9000 -u user -p password'
def cli = new CliBuilder(usage: helpMessage)
cli.with {
    h longOpt: 'help', 'print this message', required: false
    s longOpt: 'host', 'specify SonarQube URL', type: String, args: 1, required: true
    u longOpt: 'user', 'specify user login', type: String, args: 1, required: true
    p longOpt: 'password', 'specify user password', type: String, args: 1, required: true
    c longOpt: 'components', 'specify list of components (separated by comma)', type: String, args: 1, required: true
    d longOpt: 'dry-run', 'specify to not to perform any action, by default is dry-run', args: 1, type: String, required: false
    n longOpt: 'number', 'specify number of issues to retreive, default is 5', type: int, args: 1, required: false
}

def options = cli.parse args

if (!options) {
    return
}

if (options.h) {
    cli.usage()
    return
}

String URL = options.s
String[] COMPONENT_ROOTS = options.c.split(',')
int number = options.n ? Integer.valueOf(options.n) : 5
boolean dryRun = !options.d || options.d == 'true'
String DEFAULT_USER = "admin"

SonarClient sonarClient = SonarClient.builder().url(URL).login(options.u).password(options.p).build()
Sonar sonar = Sonar.create URL

IssueClient issueClient = sonarClient.issueClient()
UserClient userClient = sonarClient.userClient()

Issues issues = issueClient.find IssueQuery.create()
  .componentRoots(COMPONENT_ROOTS)
  .resolved(false)
  .severities('BLOCKER', 'CRITICAL', 'MAJOR', 'MINOR')
  .assigned(false)
  .statuses('OPEN')
  .pageSize(number)

List issueList = issues.list()

Map sourceMap = [:]
Map userMap = [:]

issueList.each { issue ->
    String issueDesc = "Issue key: ${issue.key()}, issue type: ${issue.ruleKey()}, line: ${issue.line()}, in file '${issue.componentKey().split(':')[2]}' "
    String key = issue.componentKey()
    
    Measure measure = sourceMap.get key
    // retrieve measure for given resource
    if (!measure) {
        ResourceQuery resourceQuery = ResourceQuery.createForMetrics key, 'authors_by_line'
        Resource resource = sonar.find(resourceQuery)
        sourceMap[key] = measure = resource.getMeasure 'authors_by_line'
    }
    
    Map measureData = measure.getDataAsMap ';'
    String assignee = measureData["${issue.line()}"]
    
    User user = userMap[assignee]
    if(!user){
        // strange code, but user in SCM and in Sonar are different
        def matcher = assignee =~ /^(\w+\_)?(\w+)@.*$/
        String searchTxt = matcher[0][2]
        searchTxt = searchTxt == null ? DEFAULT_USER : searchTxt
        List users = userClient.find UserQuery.create().searchText(searchTxt)
        if(!users){
            users = userClient.find UserQuery.create().searchText(DEFAULT_USER)
        }
        userMap[assignee] = user = users[0]
    }

    if(!dryRun){
        Issue assignedIssue = issueClient.assign issue.key(), user.login()
        issueDesc += ", assignee was changed from '${issue.assignee()}' to '${user.login()}'"
    
        issueDesc += ", status was changed from '${issue.status()}' to "
        assignedIssue = issueClient.doTransition issue.key(), 'confirm'    
        issueDesc += "'${assignedIssue.status()}'"
    }
    println issueDesc
}