# ujs-devops

## Branch 0 - Code
For this section we're going to focus on simply writing something that runs (and then we want to run it)

### Process

#### Just get something running

1. We have a really basic Spring Boot project (created via the [intializr](https://start.spring.io/)) so let's run it
locally
    ```
    ./mvnw spring-boot:run
    ```
1. Now let's generate an artifact, since we'll need the artifact to host it in an environment where people can use it
    ```
    ./mvnw clean package
    ```
1. Since we don't have a ton of time or Azure money lets just run the executable using Java on our own hosted server
(just pretend your customer will have to join a Microsoft Teams call to use the application)
    ```
    java -jar target/devops-0.0.1-SNAPSHOT.jar com.ujs.devops.DevopsApplication
    ```

So we can deploy/execute it locally, but that isn't ideal or really representative of the real world so lets do some
automation in the next section.

### Gotchas
- You will need to have Java 11 installed in order to properly execute the steps above. If on mac OS X I would suggest 
a combination of [jenv](https://github.com/jenv/jenv) and [brew](https://brew.sh/) (as highlighted in this [StackOverflow Post](https://stackoverflow.com/a/52524114/14159380)).
However, you can [download the JDK manually](http://jdk.java.net/java-se-ri/11) if you'd prefer that.
- You should have the maven wrapper configured as part of the repo, but if not, you can configure if use the instructions
listed in this [baeldung article](https://www.baeldung.com/maven-wrapper)

### References

- [Spring Initializr](https://start.spring.io/)
- [Maven Lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html#build-lifecycle-basics)
