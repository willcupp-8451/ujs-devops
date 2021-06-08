# ujs-devops

## Branch 3 - Configure
For this section we're going to add multiple environments so that we can ensure that the code that makes it to our 
end-users is actually supposed to be there.

### Process

#### Fixing Broken Windows

When our applications directly impact end-users it's important to understand that any issue that a user finds might
affect their experience and ultimately their opinion of the application in general (this is a similar to the
[broken window concept](https://medium.com/@learnstuff.io/broken-window-theory-in-software-development-bef627a1ce99)).

Because of this, and for more reasons that we'll get into shortly, let's introduce the concept of multiple environments
where our code is hosted.

##### **Let's update Heroku so that we have multiple environments**

1. Start by creating a [heroku pipeline](https://devcenter.heroku.com/articles/pipelines). This will allow us to
maintain multiple environments for our app.

    - This can be done via the CLI with the following command which will take the app that we were using and mark it
    as the app in the staging environment (environment checked before production)
        ```
        heroku pipelines:create <pipeline-name> -a <app-name> -s staging
        ```
    - You can also do this manually through the UI if you follow
    [these instructions](https://devcenter.heroku.com/articles/pipelines#creating-pipelines-from-the-heroku-dashboard)

1. Then rename the Heroku application so that it's reflective of the environment that it's getting deployed to
**this will make things less confusing later**
    ```
    heroku apps:rename <app-name>-stg --app <app-name>
    ```
1. While we're here let's also add a production instance, while this will be the same codebase as our staging app
for now, we will update this when we start updating the Actions workflow in the next step.
    ```
    heroku apps:create <app-name>-prd
    heroku pipelines:add <pipeline-name> -a <app-name>-prd -s production
    ```
1. Verify that your pipeline has all the right apps
    ```
    heroku pipelines:info <pipeline-name>
    ```
    
##### Now that we have our heroku pipeline lets update our workflows to account for this.
1. Let's change our build-maven.yml file to point to our staging environment.
    - You can have this as a separate pipeline or just by renaming the current workflow and slightly changing our
    "Heroku deploy jar" step so that it points to the current environment
    ```
    - name: Heroku deploy jar
      env:
        HEROKU_API_KEY: ${{ secrets.HEROKU_API_KEY }}
      run: heroku deploy:jar <jar-name>.jar --jdk 11 --app <stg-app-name>
    ```
1. After the workflow run has completed, try to navigate to a *new secret* endpoint that was included with this branch
    ```
    curl https://<stg-app-name>.herokuapp.com/message
    OR just navigate to the URL in your browser
    ```
1. Then configure a new workflow to promote our application to our production app! This should be simplified
version of our staging workflow that focuses no installing heroku and using the built-in promote command.
    ```
    name: Promote app to production

    on: workflow_dispatch

    jobs:
      promote:

        runs-on: ubuntu-latest

        steps:
        - name: Install Heroku
          run: |
            curl https://cli-assets.heroku.com/install-ubuntu.sh | sh

        - name: Heroku promote app
          env:
            HEROKU_API_KEY: ${{ secrets.HEROKU_API_KEY }}
          run: heroku pipelines:promote -a <stg-app-name>
    ```
    - One thing to note here, is the [workflow_dispatch event](https://docs.github.com/en/actions/reference/events-that-trigger-workflows#workflow_dispatch)
    that the workflow is dependent on. This event basically ensures that the only way that this workflow is run is by
    manually triggering the workflow from the actions tab in your repo in GitHub. As easy as that it is, ideally this is
    where we would want to include any quality gates (like scanning for vulnerabilities) to make sure that the promotion
    is secure and intentional.
1. Now that we have the workflow set up, let's actually trigger it and see if we have our production app. Once it's
complete let's run the same cURL request as earlier
    ```
    curl https://<stg-app-name>.herokuapp.com/message
    ```
1. Well looking at the output, it seems to work as expected, however, it seems like we have a message meant for staging
in our production environment... How do we fix this?

---

#### Configuration Separated from Deployable Code

One essential concept when discussing multiple runtime environments to host our code is the idea of keeping configuration
separated from the codebase itself. For example, if we look at the MessageController that we were using previously we can
see that the value being returned is hardcoded. This means that if we want to change this for production, **we'd have
to change the codebase every time we want to update it**...

That is unless we create an abstraction between the codebase and the given environment using dynamically interpolated
environment variables that can be configured per environment. This functionality is commonly handled by services that
connect to our deployment tools, for this stack Heroku has [config variables](https://devcenter.heroku.com/articles/config-vars).

1. Let's create the config variables per environment
    - Use the Heroku CLI to create a configuration variable for both the staging and production app environments
    ```
    heroku config:set ENV_MESSAGE="this is staging" -a <stg-app-name>
    heroku config:set ENV_MESSAGE="this is production" -a <prd-app-name>
    ```
1. Update our app properties so that it references the environment variable
    - Go to your application.properties file in your codebase and add the following:
    ```
    e451.my-message=${ENV_MESSAGE:"this is a default message"}
    ```
    - This will ensure that we can use this property within our codebase, as we'll see in the next step. This also
    grants us the ability to have multiple properties files that we can refer to ranging from local instances to deployed
    ones (which you can read about more [here](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
    if interested)
1. Change the the message controller to reference our new application.properties value using the @Value annotation
    ```
      @GetMapping("/message")
      public String getMessage(@Value("${e451.my-message}") String myMessage) {
        return myMessage;
      }
    ```
1. Commit and push your changes, this should kick off the build/deploy to staging, and then promote it to production if
all is well. Then use the previous cURL statements to verify things look up to snuff.
    ```
    curl https://<stg-app-name>.herokuapp.com/message
    curl https://<prd-app-name>.herokuapp.com/message
    ```

While this works for this very obvious example, some other use-cases might include database connection strings or service
account credentials that are specific to certain environments. By managing things at the deployment level, we're able to
deploy to environments without having to worry about making code changes for values that will differ by the environment
regardless.


Now that we've configured our code for multiple environments, lets see how we plan to monitor/maintain our app when things
aren't going as we intended.


### Gotchas

- {Insert Here}

### References

- [Wikipedia - Broken Windows Theory](https://en.wikipedia.org/wiki/Broken_windows_theory)
- [12FA - Config](https://12factor.net/config)
- [GHA - Workflow Dispatch Event](https://docs.github.com/en/actions/reference/events-that-trigger-workflows#workflow_dispatch)
- [Config Vars in Heroku](https://devcenter.heroku.com/articles/config-vars)
- [Spring Boot Environments in Heroku](https://devcenter.heroku.com/articles/deploying-spring-boot-apps-to-heroku)
- [Spring Boot Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)

