<h2>Fraud Detection Application</h2>
This is a demo project for testing Neo4j's functionality for detecting financial frauds using the PaySim mobile money simulator.
<h4>Usage</h4>
<ul>
  <li>
    With bootstrap.sh:
    <ol>
      <li>Go to docker/</li>
      <li>Run `./bootstrap.sh --rebuild`</li>
    </ol>
  </li>
  <li>
    Manually:
    <ol>
      <li>Go to docker/</li>
      <li>
        Remove previously created docker container and images if any:<br/>
        ```shell
        docker-compose kill
        docker-compose rm -f
        docker rmi docker_nginx docker_tomcat docker_neo4j
        ```
      </li>
      <li>
        Go back to the project folder and build the project with Maven:<br/>
        ```shell
        mvn clean install
        ```
      </li>
      <li>Copy the generated fraud-detection-api.war package from target/ to docker/tomcat/</li>
      <li>
        Go to docker/ and build the docker-compose:<br/>
        ```shell
        docker-compose up -d --build
        ```
      </li>
    </ol>
  </li>
  <li>The server API will be available at `http://localhost:8080/fraud-detection-api/`</li>
 </ul>
 <h4>Prerequisites</h4>
 <ol>
   <li>Running Docker daemon</li>
   <li>Docker Compose</li>
   <li>Maven</li>
   <li>Bash shell in case of using the bootstrap.sh script</li>
 </ol>