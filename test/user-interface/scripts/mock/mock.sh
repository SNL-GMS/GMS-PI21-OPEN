#!/bin/bash
set -e

function explain_basic_usage {
  ./util/validate_env_vars.sh
  echo "#############"
  echo "Mock Help"
  echo "Command line tool to build, run, and manage the mocker."
  echo
  echo "Commands:"
  echo "  boot                                   Does it all. When GMS_UI_MODE=ian, simply run the mock servers."
  echo "                                         When GMS_UI_MODE=soh, also stops old docker containers. Builds. Runs with any run options provided."
  echo "  build                                  Compile and build docker images."
  echo "  clean                                  Cleans the docker images, containers, volumes."
  echo "  info                                   Prints out info about the docker containers, images and volumes of interest"
  echo "  run | start                            Runs the mock server, and the producer if GMS_UI_MODE=soh"  
  echo "  stop | down                            Brings down these docker containers: Kafka, zookeeper, etcd."
  echo "                                         Also kills the mock data server."
  echo
  echo "Options:"
  echo "  -v | --verbose                         Turns on verbose mode for more info."
}

function fail_helpfully {
  ./util/offer_help.sh
  ./util/exit_with_failure.sh
}

function boot {
  ./run-boot.sh $options
}

function build {
  ./run-build.sh $options
}

function clean {
  ./run-clean.sh $options
}

function info {
  ./run-info.sh $options
}

function start_producer {
  ./run-producer.sh $@
}

function stop_producer {
  ./run-stop.sh $@
}

function schedule_run_help {
  should_print_help=1
}

function schedule_run_producer {
  should_produce=1
}

function schedule_run_boot {
  should_boot=1
}

function schedule_run_build {
  should_build=1
}

function schedule_run_clean {
  should_clean=1
}

function schedule_run_info {
  should_print_info=1
}

function schedule_run_stop_producer {
  should_stop_producer=1
}

if [[ $# -eq 0 ]] ; then
  schedule_run_help
fi

while [[ $# -gt 0 ]]
do
key="$1"

case $key in
  boot)
    schedule_run_boot
    shift
    options="$options $@"
    break;
    ;;
  build)
    schedule_run_build
    shift
    options="$options $@"
    break;
    ;;
  clean)
    schedule_run_clean
    shift
    options="$options $@"
    break;
    ;;
  help|-h|--help)
    schedule_run_help
    shift
    ;;
  info)
    schedule_run_info
    shift
    options="$options $@"
    break;
    ;;
  run|start)
    schedule_run_producer
    shift
    options="$options $@"
    break;
    ;;
  stop|down)
    schedule_run_stop_producer
    shift
    options="$options $@"
    break;
    ;;
  -v|--verbose)
    verbose="I would have written a shorter placeholder, but I did not have the time."
    shift
    options="$options -v"
    ;;
  *)    # unknown option
    echo "Unknown option $key"
    fail_helpfully
    shift # past argument
    ;;
esac
done

if ! [ -z "$should_print_help" ] ; then
  explain_basic_usage
  exit 0
fi

if ! [ -z "$should_boot" ] ; then
  boot $options
  exit 0
fi

if ! [ -z "$should_build" ] ; then
  build $options
  exit 0
fi

if ! [ -z "$should_clean" ] ; then
  clean $options
  exit 0
fi

if ! [ -z "$should_print_info" ] ; then
  info $options
  exit 0
fi

if ! [ -z "$should_stop_producer" ] ; then
  stop_producer $options
  exit 0
fi

if ! [ -z "$should_produce" ] ; then 
  start_producer $options
fi
