# CheckIt server

Server side of application for reviewing changes in RDF datasets designed for integration
into [Assembly line](https://github.com/opendata-mvcr/sgov-assembly-line). Frontend for this application is created by
Bc. Filip Kopecký [here](https://github.com/filip-kopecky/checkit-ui).

## Application information

CheckIt server is a Java 17 application build with Gradle 8. It uses Keycloak as OAuth2 authorization service and
connects to RDF database using [Rdf4jDataSource](https://github.com/kbss-cvut/jopa/tree/master/ontodriver-rdf4j) driver.

|              | Tested with versions   |
|--------------|------------------------|
| **Java**     | Amazon Corretto 17.0.6 |
| **Gradle**   | 8.1                    |
| **Keycloak** | JBOSS 12.0.4           |
| **GraphDB**  | GraphDB 9.6.0, 9.8.0   |

## Requirements

- Java 17
- Gradle 8

## Startup guide

Startup guide to compile and run the application.

### 1. Set required variables

You need to set required variables either by editing `src/main/resources/application.yml`, thus baking the information
to the JAR file build in the next step or setup environment variable in the environment you want to run this application
in. For the list of required variables, refer to the [Environment variables](#environment-variables) section of this
readme file.

### 2. Build JAR

To build JAR file, execute:

```
gradle clean bootJar
```

The built JAR can be found in `build/libs/` as `checkit-server-{version}.jar`.

### 3. Run application

For running you have two options:

- Run with Java 17 JRE
- Run in Docker

#### Run with Java 17 JRE

You can run it with JRE by executing in `build/libs/`:

```shell
java -jar checkit-server-{version}.jar
```

#### Run in Docker

Project contains a Dockerfile to build a docker image using the built JAR file. You need to have Docker installed and
run:

```shell
docker build . --tag checkit-server-custom
```

Now you have docker image called `checkit-server-custom`. You can run this image in Docker by executing:

```shell
docker run -p 8080:8080 --name checkit-server-app checkit-server-custom 
```

If you have not baked variables by editing yml file you will also have to specify all the environment variables. For
this refer to [docker run](https://docs.docker.com/engine/reference/commandline/run/#env) documentation.

## Setup development environment

Before you start developing this application, you need to set required environmental variables that can be found in
section [Environment variables](#environment-variables). These variables are needed to connect to Keycloak authorization
service and database.

## Environment variables

Description of environment variables consumed by CheckIt server. *You can see some variables set with
examples/placeholders in [`src/main/resources/application.yml`](src/main/resources/application.yml) file.*

You can choose to put your variables in YAML file located
in [`src/main/resources/application.yml`](src/main/resources/application.yml) or run the application in environment with
set environment variables. All variable bellow are written in environment variables format, but can be easily converted
to YAML format, like so:

```yaml
# YAML format
here:
  is:
    anExample: "of YAML format"

  # Environment variables format
  HERE_IS_AN-EXAMPLE="of environment variable format"
```

### Required

Variables required to be set to start the application.

| Name                        | Description                                                                            |
|-----------------------------|----------------------------------------------------------------------------------------|
| KEYCLOAK_URL                | URL of Keycloak endpoint.                                                              |
| KEYCLOAK_REALM              | Keycloak Realm name you want to connect to.                                            |
| KEYCLOAK_REALM-KEY          | Public key of the Keycloak Realm specified in previous variable.                       |
| KEYCLOAK_CLIENT-ID          | Client ID (not UUID) from Keycloak assigned for this application.                      |
| KEYCLOAK_SECRET             | Secret for Client specified in previous variable.                                      |
| KEYCLOAK_API-ADMIN_USERNAME | Username of a user created in Keycloak with the rights to manage users a view clients. |
| KEYCLOAK_API-ADMIN_PASSWORD | Password of user mentioned in previous variable.                                       |
| REPOSITORY_URL              | URL of a Database (like GraphDB) with specified repository.                            |

### Optional

Optional variables allowing more configuration.

| Name                                      | Description                                                                               |
|-------------------------------------------|-------------------------------------------------------------------------------------------|
| SERVER_PORT                               | Port the application is running on. <br/>*Default: 8080*                                  |
| SERVER_SERVLET_CONTEXT-PATH               | Sets base URL path. <br/>*Default: /checkit-server*                                       |
| GITHUB_PUBLISH-TO-SSP                     | Indicates if approved publication contexts should be pushed to SSP. <br/>*Default: false* |
| GITHUB_ORGANIZATION                       | GitHub organization where SSP is located.                                                 |
| GITHUB_REPOSITORY                         | GitHub repository name of SSP.                                                            |
| GITHUB_TOKEN                              | Login token to merge PRs in GitHub.                                                       |
| SGOV-SERVER_URL                           | URL of SGoV server (to create Github PR with Publication Context).                        |
| KEYCLOAK_ISSUER-URL                       | URL of Keycloak's issuer endpoint.                                                        |
| KEYCLOAK_AUTHORIZATION-URL                | URL of Keycloak's authorization endpoint.                                                 |
| KEYCLOAK_TOKEN-URL                        | URL of Keycloak's token endpoint.                                                         |
| KEYCLOAK_USER-INFO-URL                    | URL of Keycloak's user info endpoint.                                                     |
| KEYCLOAK_JWKS-URL                         | Keycloak's JWK Set URL.                                                                   |
| KEYCLOAK_END_SESSION_URL                  | URL of Keycloak's end session (logout) endpoint.                                          |
| REPOSITORY_USER_ID-PREFIX                 | Prefix for Keycloak user IDs used in Database.                                            |
| REPOSITORY_USER_CONTEXT                   | Identifier of context (graph) users are stored in.                                        |
| REPOSITORY_USER_GESTORING-REQUEST_CONTEXT | Identifier of context (graph) where Gestoring requests should be stored.                  |
| REPOSITORY_USER_COMMENT_CONTEXT           | Identifier of context (graph) where comments should be stored.                            |
| REPOSITORY_DRIVER                         | Database connection driver.                                                               |
| REPOSITORY_LANGUAGE                       | Default language tag for literals in Database.                                            |

You can find more optional variables
in [Spring documentation](https://docs.spring.io/spring-boot/docs/3.0.4/reference/htmlsingle/).
