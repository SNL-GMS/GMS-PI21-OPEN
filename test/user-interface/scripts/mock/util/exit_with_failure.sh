#!/bin/bash
set -e

function exit_with_failure {
  echo -e "** Something went wrong. See errors above. Exiting with failure. **"
  exit 1
}

exit_with_failure
