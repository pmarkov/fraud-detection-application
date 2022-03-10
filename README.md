<h1>Fraud Detection Application</h1>
This is a demo project for testing Neo4j's functionality for detecting financial frauds using the PaySim mobile money simulator.
<h2>Usage</h2>
<ul>
  <li>
  With bootstrap.sh:
  <ol>
    <li>Go to docker/</li>
    <li>Run:</li>

```
./bootstrap.sh --rebuild
```

  </ol>
  </li>
  <li>Manually:
    <ol>
    <li>Go to docker/</li>
    <li>Remove previously created docker container and images if any:</li>

```
docker-compose kill
docker-compose rm -f
docker rmi docker_nginx docker_tomcat docker_neo4j
```

   <li>Go back to the project folder and build the project with Maven:</li>

```
mvn clean install
```

  <li>Copy the generated fraud-detection-api.war package from target/ to docker/tomcat/</li>
  <li>Go to docker/ and build the docker-compose:</li>
      
```
docker-compose up -d --build
```

  </ol>
  </li>
  <li>The server API will be available at: <a href="http://localhost:8080/fraud-detection-api/">http://localhost:8080/fraud-detection-api/</a></li>
</ul>
<h2>Prerequisites</h2>
<ol>
  <li>Running Docker daemon</li>
  <li>Docker Compose</li>
  <li>Maven</li>
  <li>Bash shell in case of using the bootstrap.sh script</li>
</ol>
