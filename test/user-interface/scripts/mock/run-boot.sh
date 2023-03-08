#!/bin/bash

function explain_basic_usage {
  ./util/validate_env_vars.sh
  echo "#############"
  echo "Mock Boot Help"
  echo "In IAN mode, simply runs the mock data server. In SOH mode, also:"
  echo "Stops Kafka, Zookeeper, and etcd. Then rebuilds the mocker. Then runs the frameworks data injector using the options provided."
  echo
  ./run-producer.sh options
}

function run_help {
  should_print_help=1
}

while [[ $# -gt 0 ]]
do
key="$1"

case $key in
  -h|--help)
    run_help
    shift
    ;;
  *)    # add unknown options to $options
    options="$options $key"
    shift # past argument
    ;;
esac
done

if ! [ -z "$should_print_help" ] ; then
  explain_basic_usage
  exit 0
fi

./util/validate_env_vars.sh
./run-stop.sh
./run-build.sh
if [[ $GMS_UI_MODE =~ ^(soh|SOH|Soh)$ ]] ; then
  ./run-producer.sh $options
fi
