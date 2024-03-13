#!/bin/bash

mvn clean install
cp ./target/schema-entropy-1.0-SNAPSHOT-jar-with-dependencies.jar ./schema-entropy.jar
