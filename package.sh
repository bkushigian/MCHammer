#!/usr/bin/env bash

SCRIPT_DIR="$( cd -P "$( dirname "$(realpath "${BASH_SOURCE[0]}")" )" && pwd )"

mvn package shade:shade

UBER_JAR="${SCRIPT_DIR}/target/abstract-state-mutator-1.0-SNAPSHOT-jar-with-dependencies.jar"
zip msav.zip msav.sh "${UBER_JAR}"