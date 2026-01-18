# Aw Crud...

### Requirements

- Ubuntu/Debian or similar
- Docker >= 24.x
- Docker Compose >= 2.x
- Java 17 (for local execution)

## What this does fundamentally

    Installs Docker
    Builds an image of the Aw Crud app
    Starts a docker compose that runs two containers
    - The app itself
    - A Postgres database which is used by said app

## Setup docker and run the server + database

    sudo apt update

    Install Docker

    docker compose build
    docker compose up -d

## To attach run this command

    docker attach aw-crud-app

## To run the app without docker

    You can run the app itself without docker.
    But, Docker is still required for the database.

    sudo apt update

    Install Java 17
    Install Docker

    export JAVA_HOME="/path/to/openjdk-17-jdk" (same here)
    export PATH="$JAVA_HOME/bin:$PATH"
    
    cd ./crud
    ./run.sh

## To rebuild the .jar on linux

    cd ./crud
    sudo apt update

    Install Java 17

    export JAVA_HOME="/path/to/openjdk-17-jdk"
    export PATH="$JAVA_HOME/bin:$PATH"
    ./compile.sh
