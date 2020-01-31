# github-api-client

Groovy code to download issues from the github api.

Code assumes a config.yaml file exists with credentials needed to access the repository.
Note: Two-factor authentication should be off to avoid a 401 Unauthorized HTTP error.

Results written as an HTML report.

## Example:
 - username: "myname"
 - password: "mypassword"
 - repo: "myrepo"
 - dateSince: "YYYY-MM-DD"

  
