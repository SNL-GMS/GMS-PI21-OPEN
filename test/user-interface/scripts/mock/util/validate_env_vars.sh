#!/bin/bash
set -e

function validate_env_vars {
  [ -z "$GMS_UI_MODE" ] &&  echo "Required environment variable GMS_UI_MODE not set." && ./util/exit_with_failure.sh;
  [ -z "$GMS_COMMON_DIR" ] &&  echo "Required environment variable GMS_COMMON_DIR not set." && ./util/exit_with_failure.sh;
  [ ! -d "$GMS_COMMON_DIR" ] &&  echo "Required environment variable GMS_COMMON_DIR does not resolve to a directory." && ./util/exit_with_failure.sh;
  [ -z "$CI_DOCKER_REGISTRY" ] && echo "Required environment variable CI_DOCKER_REGISTRY not set." && ./util/exit_with_failure.sh;
  [ -z "$CI_REMOTE_REPOSITORY_URL" ] && echo "Required environment variable CI_REMOTE_REPOSITORY_URL not set." && ./util/exit_with_failure.sh;
  echo "Environment variables appear valid"
}

validate_env_vars
