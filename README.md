# github-api-client

Groovy code to download issues from the github api.

Code assumes a config.yaml file exists with credentials needed to access the repository.

Results written as an HTML report.

### Example config.yaml:
```yaml
---
username: "myname"
password: "mypassword"
repo: "myrepo"
dateSince: "YYYY-MM-DD"
includeBody: false
includeComments: false
useHeaders: false
```

#### Config options
**includeBody** enables printing the body of the issue (markdown converted to HTML). This can be handy for presenting
information to people who do not have permission to view the linked issue numbers. 

**includeComments** enables printing the issue comments (markdown converted to HTML).

**useHeaders** turns on an alternate formatting option using headers for the section text instead of underlined text. 
This helps the Epic, Closed and Open Stories section text stand out better when issue bodies are included.

#### MFA Authenticaiton

If two-factor authentication is on, you will encounter a 401 Unauthorized HTTP error with your normal login password.

Use a Personal Access Token in place of your normal password, so you can leave your interactive login protected by two-factor 
authentication. To generate the personalAccessToken view the instructions at

https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line

### Example usage:
`githubIssuesAPI.groovy` expects the `config.yaml` file to exist in the directory where the command is run from.

```bash
cp src/configSample.yaml config.yaml
# Edit config.yaml to set parameters and credentials
groovy src/githubIssuesAPI.groovy
```

You should now have a `onestopIssues.html` (*repo*Issues.html) file.

```bash
# for MacOS
open onestopIssues.html
```
This should open in your default browser, where you can then select-all, copy and paste into a google doc.

#### To install groovy
If you don't have groovy installed locally you can do so using [SDK Man](https://sdkman.io/)
- Install sdkman: `curl -s "https://get.sdkman.io" | bash`
- Install groovy: `sdk install groovy`

