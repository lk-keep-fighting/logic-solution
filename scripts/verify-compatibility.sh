#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MVNW_BIN="${PROJECT_ROOT}/mvnw"

if [[ ! -x "${MVNW_BIN}" ]]; then
  echo "Maven Wrapper not found or not executable at: ${MVNW_BIN}" >&2
  exit 1
fi

if [[ -z "${JAVA_HOME:-}" ]]; then
  echo "JAVA_HOME is not set. Please export JAVA_HOME to a JDK 17 installation before running this script." >&2
  exit 1
fi

profiles=("spring-boot-2" "spring-boot-3")

for profile in "${profiles[@]}"; do
  echo "========================================"
  echo "Running Maven verify for profile: ${profile}"
  echo "========================================"
  "${MVNW_BIN}" -pl logic-ide -am clean verify -P"${profile}" "$@"
done
