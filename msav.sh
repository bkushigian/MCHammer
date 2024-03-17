#!/usr/bin/env bash

SCRIPT_DIR="$( cd -P "$( dirname "$(realpath "${BASH_SOURCE[0]}")" )" && pwd )"
UBER_JAR="${SCRIPT_DIR}/target/MCHammer-1.0-SNAPSHOT-jar-with-dependencies.jar"

# POM_XML="${SCRIPT_DIR}/pom.xml"
# mvn -f "${POM_XML}" exec:java -Dexec.mainClass="org.mutation_testing.App" -Dexec.args="$*"

if [ ! -f "${UBER_JAR}" ]; then
    echo "Building the project..."
    mvn -f "${SCRIPT_DIR}/pom.xml" clean package shade:shade
fi

java -cp "${UBER_JAR}" org.mutation_testing.App "$@"