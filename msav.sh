#!/usr/bin/env bash

POM_XML="${SCRIPT_DIR}/pom.xml"
echo "SCRIPT_DIR: ${SCRIPT_DIR}"

mvn -f "${POM_XML}" exec:java -Dexec.mainClass="org.mutation_testing.App" -Dexec.args="$*"