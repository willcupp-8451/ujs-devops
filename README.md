# ujs-devops

## Branch 0 - Code
For this section we're going to focus on simply writing something that runs (and then we want to run it)


#### Command References

Run the code locally 
```
./mvnw spring-boot:run
```


Generate an artifact (your executable that we want to deploy)
```
./mvnw clean package
```

Run the executable using Java
```
java -jar target/devops-0.0.1-SNAPSHOT.jar com.ujs.devops.DevopsApplication
```

### Why would we do this though? 
*we should't*... There are much better ways 
