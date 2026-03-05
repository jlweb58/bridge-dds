A Spring Boot backend project wrapping the open source DDS bridge library (https://github.com/dds-bridge/dds/)

The application is deployed as a Docker container. First the jar needs to be buil with ./gradlew build.
Then build and tag the docker image with `docker build -t jlweb58/bridge-dds:latest .`  

Deploy it to the repository with `docker push jlweb58/bridge-dds:latest`

On the production server the image needs to be pulled with `docker pull jlweb58/bridge-dds:latest`

Then run it with `docker run -d  --restart unless-stopped --name bridge-dds -p9015:9015 jlweb58/bridge-dds:latest ` 

The docker  image includes required native libraries.

TBD: automated build and deployment via GitHub

