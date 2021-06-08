# ujs-devops

## Branch 4 - monitor & maintain
For this section we're going to figure out what to do when things aren't operating as we may have intended and how we can
find out that we have a problem in the first place.

So we have a local, staging, and production environment where our code can run but how do we make sure that everything is
operating correctly and efficiently? The answer is multiple concepts: historical log aggregation, service level monitoring and
alerting.

For our purposes we'll focus on historical logs and alerting, but expect to learn more about all of these concepts in
another jumpstart session from the SRE team down the road.

### Process

#### Historical Logs

A key component to triage and error determination is having a way to aggregate your application's logs in a readable
and queryable manner. This allows you to trace where an issue occurred and find relevant logs to point you in the
direction of finding a fix.

To represent this, let's look at some of Heroku's functionality (and potentially some other tools as well)

1. First let's see what Heroku logging looks like for our staging application
   - We can retrieve Heroku logs from the [CLI](https://devcenter.heroku.com/articles/logging#log-retrieval-via-cli-view-logs)
   or the [browser](https://devcenter.heroku.com/articles/logging#build-logs). For our purposes, let's just try to tail
   the logs via the CLI
   ```
   heroku logs -a <app-name> --tail
   ```
   - Then hit the message endpoint again so we can verify that the logs are tailed
   ```
   curl https://<stg-app-name>.herokuapp.com/message
   ```
1. Now, since we're on this new branch, lets say that I made a change and added a new endpoint that a user asked for
without telling you and they complained to you about it not working properly. Let's check it out and see what's up:
    - Navigate to the endpoint via the browser or a cURL request:
    ```
    https://<stg-app-name>.herokuapp.com/divide?numerator=4&denominator=2
    curl https://<stg-app-name>.herokuapp.com/divide\?numerator\=4\&denominator\=2
    ```
    - Uh, oh what's wrong? Let's take a look at the logs to see.
    - Seems like when I implemented the feature, I didn't really do anything... Looks like you're going to have to fix my
    mistake 😬
 
 ---

#### Maintain (Test Driven Development/Code Reviews)

Our code is broken so we should probably bug the person who broke things (but really let's just fix it). A key
component to maintaining a codebase is testing while we develop, so we don't encounter issues like this in the wild. So
lets do the right thing and add a test for the thing we're trying to fix before we fix it.


1. Add a new test file for the divide controller and add the following
    ```
      @Test
      public void getDivisionResult() throws Exception {
        final String uri = "/divide?numerator=4&denominator=2";

        mvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(equalTo("2.0")));
      }
    ```

1. This gives us coverage over at least the issue that we were seeing earlier (for this demo we'll just focus on this
case but we'll probably want some boundary cases later). Anyway let's fix the thing, should be pretty easy (hopefully).
    ```
      @GetMapping("/divide")
      public String getDivide(@RequestParam("numerator") int numerator,
                              @RequestParam("denominator") int denominator) {
        double result = (double) numerator / denominator;
        return String.valueOf(result);
      }
    ```

1. Now lets push this to our staging environment (by committing and pushing to GitHub) and test it using the same curl
request as earlier
    ```
    curl https://<stg-app-name>.herokuapp.com/divide\?numerator\=4\&denominator\=2
    ```

1. After we confirmed that it worked for staging, let's open a Pull Request so that members of our team can review the
changes and confirm things before we promote to production. Thus completing the cycle all the way from our initial
development to the continued maintenance.

---

#### *BONUS* - Alerting

Another component of monitoring/maintaining is making sure that we're alerted to these issues before a user experiences
them. So it's important that we're proactive about fixing these issues after we initially find them.

While [GitHub](https://github.com/settings/notifications) and
[Heroku](https://devcenter.heroku.com/articles/metrics#threshold-alerting) have some alerting functionality,
we can use something else to give us the granular control that we really need. That's where [Sentry](https://docs.sentry.io/)
(another OSS solution) comes in.


For our project, we can configure sentry for our project by creating an organization and creating a project for our
repository.

1. First, lets add some things to our codebase so that sentry can can connect to our app
    - Add a few dependencies to your pom.xml
    ```
    <dependency>
        <groupId>io.sentry</groupId>
        <artifactId>sentry-spring-boot-starter</artifactId>
        <version>5.0.0</version>
    </dependency>
    <dependency>
        <groupId>io.sentry</groupId>
        <artifactId>sentry-logback</artifactId>
        <version>5.0.0</version>
    </dependency>
    ```
    - Then add a line to the application.properties
    ```
    sentry.dsn=https://d8998ab4ced849878242f4ab9b316388@o789068.ingest.sentry.io/5799950
    ```

1. Since we already fixed our issue, lets add another endpoint so we can verify things work
    - Add an exception endpoint that we can call to test things
    ```
      @GetMapping("/error-endpoint")
      public void blowThingsUp() {
        try {
          throw new Exception("This is a test.");
        } catch (Exception e) {
          Sentry.captureException(e);
        }
      }
    ```
    - Commit and push all the changes to the staging environment

1. Lets head to Sentry and see our issue
    - Go to your specific organization that you configuration is set up for
    ```
    https://sentry.io/organizations/<sentry-org-name>
    ```
    - If you aren't seeing anything, try to hit your endpoint so that Sentry detects your issue
    ```
    curl https://<stg-app-name>.herokuapp.com/error-endpoint
    ```

1. From there we can look at the issue and use Sentry's functionality to detect how the issue occurred and even resolve it
    - While we aren't going to a crazy amount of detail about Sentry and it's functionality; it's important to know that
    tools like it exist and can help improve your ability to detect issues and set up relevant alerts to make monitoring
    and maintaining even easier
    - You can read more about [issues here](https://docs.sentry.io/product/issues/) and
    [alerting here](https://docs.sentry.io/product/alerts-notifications/)



### Gotchas

- {Insert here}

### References

- [O'Reilly - SRE Overview](https://www.oreilly.com/content/site-reliability-engineering-sre-a-simple-overview/)
- [Martin Fowler - TDD](https://martinfowler.com/bliki/TestDrivenDevelopment.html)
- [Sentry Spring Boot Documentation](https://docs.sentry.io/platforms/java/guides/spring-boot/)

