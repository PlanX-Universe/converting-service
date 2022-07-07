#!/bin/bash
baseDir=${pwd}

# build without tests
./gradlew -Pbuildprofile=COMPOSE -x test clean build --info

# build docker image from resulting jar
docker build -t planx.toolbox.transforming.converting .
