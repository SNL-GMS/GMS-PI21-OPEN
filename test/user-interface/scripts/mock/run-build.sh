#!/bin/bash

set -e

function run_help {
  should_print_help=1
}

function explain_basic_usage {
  ./util/validate_env_vars.sh
  echo "#############"
  echo "Mock Build Help"
  echo "Runs docker-compose up and gradle docker to compile and build the necessary docker images for the frameworks data injector."
  echo "Also checks to ensure that the mock data server has been built (yarn build from /typescript/user-interface)."
  echo
  echo "Options:"
  echo "  -h | --help                            You're looking at it."
  echo "  -v | --verbose                         Turns on verbose mode for more info."
}

function compose_docker_container {
  ./util/validate_env_vars.sh
  if [[ $GMS_UI_MODE =~ ^(ian|IAN|Ian)$ ]] ; then
    docker-compose -f ian-mock-compose.yaml up -d && sleep 10s
  else 
    docker-compose -f soh-mock-compose.yaml up -d && sleep 10s
  fi
}

function verify_mock_server_is_built {
  [ ! -d "$GMS_COMMON_DIR/typescript/user-interface/packages/mock-data-server/lib" ] &&  echo "It looks like the mock data server has not been built. Have you run '$GMS_COMMON_DIR/typescript/user-interface/yarn build'?" && ./util/exit_with_failure.sh;
  echo "Mock Data Server found"
}

function build_producer {
  ./util/validate_env_vars.sh
  source $GMS_COMMON_DIR/.bash_env
  pushd $GMS_COMMON_DIR/java/gms/shared/frameworks/data-injector
    DOCKER_IMAGE_TAG=develop gradle docker
  popd
}

function info {
  ./run-info.sh $options
}

function fail_helpfully {
  ./util/offer_help.sh
  ./util/exit_with_failure.sh
}

while [[ $# -gt 0 ]]
do
key="$1"

case $key in
  -h|--help)
    run_help
    shift
    ;;
  -v|--verbose)
    verbose="I would have written a shorter placeholder, but I did not have the time."
    shift
    options="$options -v"
    ;;
  *)    # unknown option
    echo "Unknown option $1"
    ./util/offer_help.sh
    ./util/exit_with_failure.sh
    shift # past argument
    ;;
esac
done

if ! [ -z "$should_print_help" ] ; then
  explain_basic_usage
  exit 0
fi

# Now run the producer with the variables set above.
compose_docker_container

if [[ $GMS_UI_MODE =~ ^(soh|SOH|Soh)$ ]] ; then
  echo "BUILDING PRODUCER"
  build_producer
fi

verify_mock_server_is_built

if ! [ -z "$verbose" ] ; then
  info $options
fi

exit 0;
