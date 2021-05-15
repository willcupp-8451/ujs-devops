# ujs-devops

## Branch 1 - Build
For this section we're going to build our code in a more consistent manner while being a bit more proactive about testing
the things we build.

TODO:
- Introduce templatization (templatize our workflow with some parameters so that anyone could use it)
    - Either use a workflow template or a custom action

### Process

#### Repeatable Build Process

Let's automate that build process using a CI tool [GitHub Actions](https://docs.github.com/en/actions).

1. Go to the actions tab within GitHub
1. Select the Java with Maven starter workflow (or set one up yourself)
    - Take note of the editor (we won't go into much detail here but you can bring in new actions and refer to GitHub's
    documentation)
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
        name: github-actions-artifact
        path: artifacts

    ```
    - For the build/publish workflow. Need to change the creds in the settings.xml and add to your workflow:
    settings.xml

    ```
	<activeProfiles>
		<activeProfile>github</activeProfile>
	</activeProfiles>

	<profiles>
		<profile>
			<id>github</id>
			<repositories>
				<repository>
					<id>github</id>
					<name>GitHub caseyokane-8451 Apache Maven Packages</name>
					<url>https://maven.pkg.github.com/caseyokane-8451/ujs-devops</url>
				</repository>
			</repositories>
		</profile>
	</profiles>

	<servers>
		<server>
			<id>github</id>
			<username>caseyokane-8451</username>
			<password>${env.GITHUB_TOKEN}</password>
		</server>
	</servers>
    ```
    - Also change your workflow settings to correspond to it
    ```
    - name: Publish to GitHub Packages
      run: mvn deploy -s settings.xml
      env:
        GITHUB_TOKEN: ${{ github.token }}

    ```
1. Commit your workflow
1. Run the workflow
1. Uh oh

---

#### Test Driven Development

Tests are important, so I wrote some (how else will we know if our thing works) but they break the code? This is something
that happens from time to time. This is why it's encouraged to do something known as
[Test Driven Development](https://en.wikipedia.org/wiki/Test-driven_development). For now though:

1. Let's find the issue and fix it.
1. Commit and run the pipeline again
1. Going back to our original goal, look at your packaged artifact
    - Why is this valuable? Because it gives you the ability to store previous builds and rollback whenever necessary
    while also giving you better ability to test certain revisions if necessary

---

#### Static Code Analysis

Now how do we know our code is good? We know it builds, but is it right? Probably good to check (and have those checks
exist as part of the pipeline)

. Import your project into [Sonar Cloud](https://sonarcloud.io/github?gads_campaign=North-America-SonarCloud&gads_ad_group=SC-GitHub&gads_keyword=sonarcloud%20github&gclid=CjwKCAjwqIiFBhAHEiwANg9szvr0JVWzwaxeu1lbtrLEDAFvvZLF8WabyTrzSvdddV4Whq81Hvaz6BoCcj8QAvD_BwE)
(give it access to look at your repositories)
1. Create the org within Sonar Cloud
1. Configure it to run with GHA
    - Create a GitHub Secret for your SONAR_TOKEN
    - Update your pom.xml with the following
    ```
    <properties>
      <sonar.projectKey>caseyokane-8451_ujs-devops</sonar.projectKey>
      <sonar.organization>caseyokane-8451</sonar.organization>
      <sonar.host.url>https://sonarcloud.io</sonar.host.url>
    </properties>
    ```

    - Update your workflow with the following
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
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}  # Needed to get PR information, if any
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: mvn -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar
     ```

1. Commit your changes and run the workflow again
1. Now we'll know when things are starting to get out of hand

---

#### BONUS: Templatization and Usability

TODO

### Gotchas
- Be sure that you're pointing to your own GitHub user/repo (especially if you name it something different)
- Make sure that your workflow triggers are specific to the 1-build branch. Otherwise you likely won't see them kickoff
when you expect. Example trigger shown below:
```
on:
  push:
    branch: [ 1-build ]
```

### References
[GHA Docs - Publish Maven Package to GitHub Packages](https://docs.github.com/en/actions/guides/publishing-java-packages-with-maven)
[GHA Community - Unauthorized error](https://github.community/t/deploying-to-github-packages-from-github-actions-returns-unauthorized-error/18156)
