# ujs-devops

## Branch 1 - Build
For this section we're going to build our code in a more consistent manner while being a bit more proactive about testing
the things we build.

### Process

#### Repeatable Build Process

Let's automate our previous build process using a CI tool [GitHub Actions](https://docs.github.com/en/actions). This should
make things more repeatable, consistent, and reduce the amount of manual intervention that is required.

1. Use `.github/workflows/java-ci.yml` as a start
    - OR  Go to the actions tab within GitHub and make your own
        - Select the Java with Maven starter workflow (or set one up yourself)
        - Take note of the editor (we won't go into much detail here but you can bring in new actions and refer to GitHub's
        [documentation](https://github.blog/2019-10-01-new-workflow-editor-for-github-actions/))
1. Make some slight changes to make sure it's going to publish something
    - For the build workflow. Need to add:
    ```
    - name: Build with Maven
      run: |
        ./mvnw -B package --file pom.xml
        mkdir artifacts && cp target/*.jar artifacts

    - name: Persist Artifacts
      uses: actions/upload-artifact@v2
      with:
        name: JARtifact
        path: artifacts

    ```
    - **You may want to also consider specific versioning standards as well to keep a greater history of your artifacts similar
to what is described [here](https://medium.com/@wakingrufus/semantic-versioning-96cff0830736)**

1. Commit and run your workflow
1. ...Uh oh

---

#### Test Driven Development

Tests are important, so I wrote some. How else will we know if our thing works? However, seems like they break the code?
This is something that happens from time to time. This is why it's encouraged to do something known as
[Test Driven Development](https://en.wikipedia.org/wiki/Test-driven_development). For now though:

1. Let's find the issue and fix it.
1. Commit and run the pipeline again
1. Going back to our original goal, look at your packaged artifact
    - Why is this valuable? Because it gives you the ability to store previous builds and rollback whenever necessary
    while also giving you better ability to test certain revisions if necessary
    
Now that we know when things start to get out of hand just by having our build run, let's look at deploying this thing

---

#### <ins>**BONUS**</ins> Static Code Analysis

Now how do we know our code is good? We know it builds, but is it right? Probably good to check (and have those checks
exist as part of the pipeline)

1. Import your project into [Sonar Cloud](https://sonarcloud.io/github?gads_campaign=North-America-SonarCloud&gads_ad_group=SC-GitHub&gads_keyword=sonarcloud%20github&gclid=CjwKCAjwqIiFBhAHEiwANg9szvr0JVWzwaxeu1lbtrLEDAFvvZLF8WabyTrzSvdddV4Whq81Hvaz6BoCcj8QAvD_BwE)
(give it access to look at your repositories)
1. Create the org within Sonar Cloud
1. Configure it to run with GHA
    - Create a GitHub Secret for your SONAR_TOKEN (documentation [here](https://docs.github.com/en/actions/reference/encrypted-secrets#creating-encrypted-secrets-for-a-repository))
    - Update your pom.xml with the following **being sure to replace with your actual project key and sonar org**
    ```
    <properties>
      <sonar.projectKey>YOUR_PROJECT_KEY</sonar.projectKey>
      <sonar.organization>YOUR_SONAR_ORG</sonar.organization>
      <sonar.host.url>https://sonarcloud.io</sonar.host.url>
    </properties>
    ```

    - Update your workflow with the following: 
    ```
      - name: Cache SonarCloud packages
        uses: actions/cache@v1
        with:
          path: ~/.sonar/cache
          key: ${{ runner.os }}-sonar
          restore-keys: ${{ runner.os }}-sonar
      - name: Cache Maven packages
        uses: actions/cache@v1
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-m2
      - name: Build and analyze
        run: ./mvnw -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar package --file pom.xml
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}  # Needed to get PR information, if any
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
     ```
    This will verify our build via sonar while building the artifact, while also introducing caching to improve performance

1. Commit your changes and run the workflow again

---

### Gotchas
- If you create a workflow via GitHub be sure to update your local branch with `git pull --rebase origin <current_branch`
before pushing
- Be sure that you're pointing to your own GitHub user/repo (especially if you name it something different)
- Make sure that your workflow triggers are specific to the 1-build branch. Otherwise, you likely won't see them kickoff
when you expect. Example trigger shown below:
```
on:
  push:
    branch: [ 1-build ]
```

### References

- [GHA Docs - Publish Maven Package to GitHub Packages](https://docs.github.com/en/actions/guides/publishing-java-packages-with-maven)
- [GHA Community - Unauthorized error](https://github.community/t/deploying-to-github-packages-from-github-actions-returns-unauthorized-error/18156)
- [5 Ways Static code Analysis Can Help you](https://sdtimes.com/test/5-ways-static-code-analysis-can-save-you/)
- [SonarSource - Code Quality](https://www.sonarsource.com/why-us/code-quality/)
- [SonarSource - Code Security](https://www.sonarsource.com/why-us/code-security/)
