# ujs-devops

## Branch 2 - Deploy
For this section we're going to actually deploy our artifact so that our users (us in this case) can access use it.


### Process

#### Continuous Delivery (and Continuous Deployment)

Let's actually deploy our app somewhere. We have our artifact but it's useless unless we have a runtime environment to
host it. Heroku is an open-source alternative that we can utilize for this.

Let's get our app running somewhere. We'll use Heroku to get started fast. We have a couple options to get there

1. Use the CLI
    - [Install the Heroku CLI](https://devcenter.heroku.com/articles/heroku-cli#download-and-install)
    - Create the app (will require you to authenticate)
    ```
    heroku login
    heroku create <your-app-name>
    ```
    - Deploy your app
    ```
    git push heroku 2-deploy:main //Deploy from branch 2
    git push heroku main //Deploy from your main if you'd rather do that
    ```
   
1. **OPTIONAL** OR Go through the browser (would suggest the CLI though for this exercise though)
    - [Create a new-app](https://dashboard.heroku.com/new-app) with your account
    - Integrate your Heroku account with GitHub under "Deployment method"
        - Find your repo name and connect to it
    - If you created your app through the UI, add the remote to it before pushing
    ```
    heroku git:remote --app <your-app-name>
    ```
    - Preform a manual deploy of the 2-deploy branch

1. Navigate to our hello-world endpoint `https://<your-app-name>.herokuapp.com/hello-world`  and verify that it works

---

#### Build Once, Deploy Many

Now let's integrate this Heroku deployment as part of our GHA workflow.

Assuming we're using an artifact repository we want the build (CI) to happen as part of our workflow (from the last
branch). So after we build our artifact, lets try to deploy that.

Let's set up a new job for our deploys. In an ideal CI/CD workflow we'd have an artifact repository exist between our
deployments for [a variety of reasons](https://jfrog.com/knowledge-base/what-is-an-artifact-repository/). However, for
this quick POC we'll just use GitHub's ability to store our artifact

1. First, lets create a new Deploy Job that is dependent on the build job that we completed as part of the
previous branch. 
    - This is required since we need the artifact from the previous job (similar to what we'd do with an
    artifact repository).
    ```
      deploy:

        runs-on: ubuntu-latest
        needs: build
    ```
1. Let's pull down our JAR that we published as part of the build job. 
    - This just involves to using the download-artifact (using the name of the artifact that we want to retrieve)
    action.
    ```
      - name: Retrieve Artifact
        uses: actions/download-artifact@v2
        with:
          name: JARtifact
    ```

1. Now set up Heroku configuration as part of the workflow
    - Let's start by installing the CLI and the corresponding Java plugin
    [as described here](https://devcenter.heroku.com/articles/deploying-executable-jar-files). This will require adding
    some more steps to our `deploy` job.
    ```
    - name: Install Heroku and java plugin
      run: |
        curl https://cli-assets.heroku.com/install-ubuntu.sh | sh
        heroku plugins:install java
    ```
    - Before we can use the heroku CLI as part of our workflow we need a way to authenticate. To do this, lets generate
    the API key using the local CLI [using their documentation](https://devcenter.heroku.com/articles/authentication#retrieving-the-api-token).
    Pull the token value from running the following command
    ```
    heroku authorizations:create
    ```
    - Create [a GitHub Secret](https://docs.github.com/en/actions/reference/encrypted-secrets#creating-encrypted-secrets-for-a-repository)
    for your workflow and include it in your workflow step to deploy your JAR
    ```
    - name: Heroku deploy jar
      env:
        HEROKU_API_KEY: ${{ secrets.HEROKU_API_KEY }}
    ```

1. Let's deploy the jar with a GitHub action using this syntax from [the jar deploy](https://devcenter.heroku.com/articles/deploying-executable-jar-files#using-the-heroku-java-cli-plugin)
    ```
    - name: Heroku deploy jar
      env:
        HEROKU_API_KEY: ${{ secrets.HEROKU_API_KEY }}
      run: heroku deploy:jar <jar-name>.jar --jdk 11 --app <app-name>
    ```
   
### Gotchas
- Heroku only deploys code that you push to main/master. Pushing code to another branch of the heroku remote has no affect.
    - This shouldn't affect our case though, since we're simply using the JAR 
- Make sure you have a system.properties file present in your repo so that heroku is able to compile Java 11
[source](https://devcenter.heroku.com/changelog-items/1489)
- [Server port was required to deploy the jar](https://stackoverflow.com/questions/36751071/heroku-web-process-failed-to-bind-to-port-within-90-seconds-of-launch-tootall)


### References

- [Atlassian - Continuous Deployment vs. Continuous Delivery](https://www.atlassian.com/continuous-delivery/principles/continuous-integration-vs-delivery-vs-deployment)
- [Some comment on a Reddit thread - Build Once Deploy Many](https://www.reddit.com/r/devops/comments/d9ln04/build_once_deploy_many/f1iu60i?utm_source=share&utm_medium=web2x&context=3)
- [Deploying executable jars in Heroku](https://devcenter.heroku.com/articles/deploying-executable-jar-files)
- [Deploy to Heroku from GHA](https://dev.to/heroku/deploying-to-heroku-from-github-actions-29ej)
- [GitLab Example](https://lab.github.com/githubtraining/github-actions:-continuous-integration?overlay=register-box-overlay)
