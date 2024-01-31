#!/usr/bin/env bash

SCRIPT_DIR="$( cd -P "$( dirname "$(realpath "${BASH_SOURCE[0]}")" )" && pwd )"
UBER_JAR="${SCRIPT_DIR}/target/abstract-state-mutator-1.0-SNAPSHOT-jar-with-dependencies.jar"

# POM_XML="${SCRIPT_DIR}/pom.xml"
# mvn -f "${POM_XML}" exec:java -Dexec.mainClass="org.mutation_testing.App" -Dexec.args="$*"

echo "$*"
java -cp "${UBER_JAR}" org.mutation_testing.App "$@"