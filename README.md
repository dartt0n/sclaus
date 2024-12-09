# sclaus

<div align = center><img src="assets/banner.png"><br><br>

&ensp;[<kbd> <br> Overview <br> </kbd>](#overview)&ensp;
&ensp;[<kbd> <br> Development <br> </kbd>](#development)&ensp;
&ensp;[<kbd> <br> Deployment <br> </kbd>](#deployment)&ensp;
&ensp;[<kbd> <br> History <br> </kbd>](#histoty)&ensp;
<br><br></div>

## Overview
`sclaus` is a [telegram](https://web.telegram.org) bot for a tradition called "Secret Santa" (read more about Secret Santa on [wikipedia](https://en.wikipedia.org/wiki/Secret_Santa) and learn how to play Secret Santa on [elfster](https://www.elfster.com/content/secret-santa-rules)) hold at Innopolis University every year.

Name `sclaus` comes from [Santa Claus](https://en.wikipedia.org/wiki/Santa_Claus) shorted to SClaus.
Also it is sounds phonetically close to Scala language, in which this project is written.

## Development
### Prerequisites
Before you start, make sure you have the following tools installed on your machine:
1. **Java Development Kit (JDK)**: Ensure you have the JDK installed. You can download it from [Oracle's official website](https://www.oracle.com/java/technologies/javase-downloads.html) or use an open-source version like [OpenJDK](https://openjdk.java.net/install/).
2. **Scala Build Tool (SBT)**: Install SBT, which is the build tool for Scala. You can download it from [SBT's official website](https://www.scala-sbt.org/download.html) or using [coursier](https://get-coursier.io) tool (recommended).

### Clone the Repository
To clone the repository and change directory into it, run the following commands:
```shell
git clone https://github.com/dartt0n/sclaus.git && cd sclaus
```

### Launch the sbt shell
To launch the sbt shell and start the project, run the following command:
```shell
sbt
```
From the sbt shell, you can run the following commands:
```
compile     # Compile the project
test        # Run the tests
scalafmtAll # Format the code
```

> [!NOTE]
> You can also use the `sbt` command to run these commands in non-interactive mode.
> ```shell
> sbt compile
> sbt test
> sbt scalafmtAll
> ```

### Create Config File
Create file `application.conf`, which describes application configuration in HOCON format.
Take look at [example.application.conf](./src/main/resources/example.application.conf) to
understand what to put into config.file

### Launch application
To launch the application, run the following command:
```shell
sbt run
```

### Build the JAR File
To build the project, run the following command:
```shell
sbt assembly
```
This command will compile the project and generate an executable JAR file named `sclaus.jar` in the `target/assembly` directory.

### Launch the Application
This project depends on the several services, which are required to run the application. Please, refer to the [Deployment](#deployment) section for more details.

> [!NOTE]
> tldr: You can also use the `docker compose up --build` command to start all the services and the project using docker-compose.

## Deployment

### Prerequisites

Before you start, make sure you have the following tools installed on your machine:
1. **Docker**: Ensure Docker is installed and running. You can download it from [Docker's official website](https://www.docker.com/get-started)
2. **Docker Compose**: This is usually installed with Docker, but you can check the installation guide [here](https://docs.docker.com/compose/install/)

### Setup Instructions
#### Step 1: Create a `.env` File
Create a file named `.env` in the root of your project directory. Add the following content to the file, replacing the placeholder values with your actual credentials:
```shell
PG_USERNAME="someuser"
PG_PASSWORD="<secure-password>"
PGADMIN_USERNAME="someuser"
PGADMIN_PASSWORD="<secure-password>"
```
#### Step 2: Start the Project
Open your terminal and navigate to the project directory. Run the following command to build and start the project:
```shell
docker compose up --build --detach
```
This command will build the Docker images and start the services in detached mode.

#### Step 3: Access the Services
After starting the project, you can access the following services at the specified ports:

| service          | exposed port    |
| ---------------- | --------------- |
| sclaus-postgres  | 127.0.0.1:32004 |
| sclaus-pgadmin   | 127.0.0.1:32005 |
| sclaus-server    | 127.0.0.1:32006 |

#### Futher Steps
> [!IMPORTANT]
> Use reverse proxy like [nginx](https://www.nginx.com), [caddy](https://caddyserver.com) or [traefik](https://traefik.io) to expose the services to the internet.

## History
For the past three years (since 2022), [@dartt0n](https://t.me/dartt0n) (yes, it's me) create Secret Santa Bot for Innopolis University each time in a new language and with new functionalle. The very first bot was written in Python, the second one in Golang and this year I've decided to do it in Scala.

<div align = center>
<img src="assets/tg1.png" width="200">
<img src="assets/tg2.png" width="200">
<img src="assets/tg3.png" width="200">
</div>

## Future Plans
- [ ] Build native images and minimal docker images using GraalVM

## License
`sclaus` is free and open-source under [MIT License](https://choosealicense.com/licenses/mit/).

Please, refer to [LICENSE]("./LICENSE") for details.
