#!/bin/bash
set -e
cd "${REPO_NAME:-/workspace/AppDependencyModelerMaven_FULL}"
mvn dependency:go-offline -B
