#!/usr/bin/env bash

SCRIPT_DIR="$( cd -P "$( dirname "$(realpath "${BASH_SOURCE[0]}")" )" && pwd )"
POM_XML="${SCRIPT_DIR}/pom.xml"

mvn -f "${POM_XML}" exec:java -Dexec.mainClass="org.mutation_testing.App" -Dexec.args="$*"