# github-api-client

Groovy code to download issues from the github api.

Code assumes a config.yaml file exists with credentials needed to access the repository.

Results written as an HTML report.

## Example config.yaml:
```yaml
- username: "myname"
- password: "mypassword"
- repo: "myrepo"
- dateSince: "YYYY-MM-DD"
- includeBody: false
- includeComments: false
- useHeaders: false
```

Note: If two-factor authentication is on, you will encounter a 401 Unauthorized HTTP error with your normal login password.

Use a Personal Access Token in place of your normal password, so you can leave your interactive login protected by two-factor 
authentication. To generate the personalAccessToken view the instructions at

https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line

**includeBody** enables printing the body of the issue (markdown converted to HTML). This can be handy for presenting
information to people who do not have permission to view the linked issue numbers. 

**includeComments** enables printing the issue comments (markdown converted to HTML).

**useHeaders** turns on an alternate formatting option using headers for the section text instead of underlined text. 
This helps the Epic, Closed and Open Stories section text stand out better when issue bodies are included.
