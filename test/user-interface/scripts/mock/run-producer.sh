#!/bin/bash

# set vars
batch_size=1
interval=PT20s

soh_producer=true
system_message_producer=true

set -e

function explain_basic_usage {
  ./util/validate_env_vars.sh
  [ $? == 1 ] && exit 1
  echo "#############"
  echo "Mock Run Help"
  echo "Runs the mocker to put data on the kafka topics for the UI to consume."
  echo "By default, it produces station-soh, capability-rollup, acknowledging, quieting, and system-message"
  echo ""
  echo "Also runs the json server using the version from the current branch."
  echo ""
  echo "Note, expects the docker images to have been built."
  echo
  echo " Default batch size: 100"
  echo " Default interval: PT20s"
  echo
  print_options
}

function print_options {
  echo "Options:"
  echo "  -b <value> | --batch <value>               Sets the batch size."  
  echo "  -c <value> | --count <value>               Sets how many batches you want to send. Leave empty to run indefinitely."  
  echo "  -d | --debug                               Runs the frameworks-data-injector in debug mode. To use, configure your debugger to connect, and initiate a remote connection with the docker container running the injector."  
  echo "  --data-dir                                 Directory path to location of mock data i.e.: 'repos/<gitlab-url>/gms/gms-common/typescript/user-interface/scripts/mock/mockdata/'."
  echo "  -h | --help                                You're looking at it."
  echo "  -i <duration> | --interval <duration>      Sets the producer's interval. "
  ./util/explain_interval_usage.sh
  echo "  -m | --mount                               The name of the docker volume you would like to use to mount mock data into the frameworks data injector. i.e. 'gms-data'"
  echo "  -v | --verbose  "
  echo "  --soh                                      Run only the soh producer."
  echo "  --system | --system-message                Run only the system message producer."
  echo "  --interval | --interval-message            Run only the system event wrapped workflow's interval message producer."
}

function run_help {
  should_print_help=1
}

function run_options {
  should_print_options=1
}

# return 0 (true) if we are running only one producer
# otherwise return 1 (false)
function is_single_producer {
  if [ "$soh_producer" = true ] && [ "$system_message_producer" = false ] ; then
    echo true
  elif [ "$system_message_producer" = true ] && [ "$soh_producer" = false ]; then
    echo "found system message producer"
    echo true
  else 
    echo false
  fi
}

function print_producer_vars {
  echo "Running producer with the following arguments:"
  echo "  base_directory=$base_directory"
  echo "  batch_size=$batch_size"
  echo "  batch_count=$batch_count"
  echo "  injectable_type=$injectable_type"
  echo "  interval=$interval"
  echo "  kafka_bootstrap_server=$kafka_bootstrap_server"
  echo "  kafka_retries=$kafka_retries"
  echo "  kafka_retry_delay=$kafka_retry_delay"
  echo "  mock_file=$mock_file"
  echo "  network=$network"
  echo "  detached_flag=$detached_flag"
}

function init_producer {
  base_directory=$GMS_COMMON_DIR/typescript/user-interface/scripts/mock/mockdata
  base_directory=$(echo $base_directory | tr -s /) # Remove duplicate slashes
  injectable_type=UI_STATION_AND_STATION_GROUPS
  topic=system-event
  kafka_bootstrap_server=kafka:9092
  kafka_retries=10
  kafka_retry_delay=1000
  detached_flag=""
  etcdUser="ETCD_GMS_USER=gms"
  etcdPassword="ETCD_GMS_PASSWORD=SOME-PASSWORD"

  isSingle=$(is_single_producer)
  if [[ $(is_single_producer) == false ]] ; then
    detached_flag="-d"
  fi

  network=kafka-net
  if ! [ -z "$batch_count" ] ; then
    batch_count_option="--batchCount $batch_count"
  fi

  
  default_dir="/mockdata"
  pathToData="$base_directory:$default_dir"
  if [ -n "$mount" ]; then 
    default_dir="/data"
    pathToData="$mount:$default_dir"
  fi

  if [ -n "$data_dir" ]; then 
    base_directory=$default_dir/$data_dir
  else 
    base_directory=$default_dir/
  fi 

  base_directory=$(echo $base_directory | tr -s /) # Remove duplicate slashes
  base_directory=${base_directory%/} # Remove trailing slash
}

# Starts a producer with the given parameters
# $1 is injectable_type
# $2 is mock_file
# $3 is topic
# $4 is mount
function start_producer {
  local injectable_type=$1
  local mock_file=$2
  local topic=$3
  local mount=$4
  local cmd=""

  echo "In start producer: base_directory = $base_directory"
  pushd $GMS_COMMON_DIR/java/gms/shared/frameworks/data-injector
    cmd="docker run --rm ${debug_options} $detached_flag -it -v $pathToData \
      -e $etcdUser \
      -e $etcdPassword \
      --network $network \
      $CI_DOCKER_REGISTRY/gms-common/frameworks-data-injector:${VERSION:-develop} \
      --type $injectable_type \
      --interval $interval \
      --base $base_directory/$mock_file \
      --topic $topic \
      --batchSize $batch_size $batch_count_option \
      --bootstrapServer $kafka_bootstrap_server \
      --retries $kafka_retries \
      --retryBackoff $kafka_retry_delay"

    echo "running: $cmd"
    eval $cmd
  popd
}

function start_system_message_producer {
  start_producer SYSTEM_MESSAGE systemEvent.json system-event
}

function fail_helpfully {
  set -e
  ./util/offer_help.sh
  ./util/exit_with_failure.sh
  set +e
}

function validate_is_number {
  local re='^[0-9]+$'
  if [[ $1 =~ $re ]] ; then
    echo "valid"
  else 
    echo "invalid"
  fi
}

function validate_batch_size {
  if [ -z "$batch_size" ] ; then
    echo "batch (-b) requires a number as an argument. $batch_size"
    echo "  for example: kafkactl --batch 10"
    fail_helpfully
  fi
  isValid=$(validate_is_number $batch_size)
  if [[ "$isValid" == "invalid" ]] ; then
    echo "batch (-b) requires a number as an argument. '$batch_size' is not a number."
    fail_helpfully
  fi
}

function validate_batch_count {
  if [ -z "$batch_count" ] ; then
    echo "count (-c) requires a number as an argument. $batch_count"
    echo "  for example: kafkactl --count 12"
    fail_helpfully
  fi
  isValid=$(validate_is_number $batch_count)
  if [[ "$isValid" == "invalid" ]] ; then
    echo "count (-c) requires a number as an argument. '$batch_count' is not a number."
    fail_helpfully
  fi
}

function validate_interval {
  if [ -z "$interval" ] ; then
    echo "interval (-i) requires an argument."
    echo "  usage$ kafkactl --interval PT10s"
    fail_helpfully
  fi
  local re='^PT[0-9]+[smh]$'
  if ! [[ $interval =~ $re ]] ; then
    echo "Invalid interval: $interval"
    ./util/explain_interval_usage.sh
    ./util/exit_with_failure.sh
  fi
}

function validate_is_built {
  validate_container_exists kafka
  validate_container_exists zookeeper
  validate_container_exists etcd
}

function validate_container_exists {
  local num_found_containers="$(docker ps | grep $1 | wc -l)";
  if [ $num_found_containers -eq 0 ] ; then
    echo "ERROR"
    echo " * docker container $1 not found"
    echo " * You may need to compose the container. Try running build."
    echo
    fail_helpfully
  fi
}

function validate_image_exists {
  local num_found_images="$(docker image ls | grep $1 | wc -l)"
  if [ $num_found_images -eq 0 ] ; then
    echo "ERROR"
    echo " * docker image $1 not found"
    echo " * You may need to build the docker image. Try running build."
    echo
    fail_helpfully
  fi
}

function validate_mount {
  local matching_vol="$(docker volume ls | cut -d " " -f2- | tr -d "[:blank:]" | grep "^${mount}$")"
  if ! [ -n "$matching_vol" ] ; then
    echo "ERROR"
    echo " * docker volume \"$mount\" not found"
    echo " * You may need to create a docker volume and put test data into it to use this option. See https://docs.docker.com/storage/volumes/"
    echo
    fail_helpfully
  fi
}

while [[ $# -gt 0 ]]
do
key="$1"

case $key in
  -b|--batch)
    shift;
    batch_size=$1
    validate_batch_size
    shift
    ;;
  -c|--count|--batch-count)
    shift;
    batch_count=$1
    validate_batch_count
    shift
    ;;
  -d|--debug)
    debug_options="-p 5005:5005 -e JAVA_OPTS=\"-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005\""
    shift
    ;;
  --data-dir)
    shift; 
    data_dir=$1
    shift
    ;;
  -h|--help)
    run_help
    shift
    ;;
  -i|--interval)
    shift;
    interval=$1
    validate_interval
    shift
    ;;
  -m|--mount)
    shift;
    mount=$1
    validate_mount
    shift
    ;;
    --interval|--interval-message)
    # any string with length greater than 0 will cause the system message producer to run
    system_message_producer=false
    soh_producer=false
    shift
    ;;
  --soh)
    # any string with length greater than 0 will cause the soh producer to run
    soh_producer=true
    system_message_producer=false
    shift
    ;;
  --system|--system-message)
    # any string with length greater than 0 will cause the system message producer to run
    system_message_producer=true
    soh_producer=false
    shift
    ;;
  -v|--verbose)
    # any string with length greater than 0 will cause the system message producer to run
    verbose="I would have written a shorter placeholder, but I did not have the time."
    shift
    ;;

  options)
    run_options;
    shift
    ;;
  *)    # unknown option
    echo "Unknown option $1"
    ./util/offer_help.sh
    ./util/exit_with_failure.sh
    shift # past argument
    ;;
esac
done

# Make sure we have the environment variables set correctly.
./util/validate_env_vars.sh

if ! [ -z "$should_print_help" ] ; then
  explain_basic_usage
  exit 0
fi

if ! [ -z "$should_print_options" ] ; then
  print_options
  exit 0
fi

# Now run the producer with the variables set above.
init_producer $@
if ! [ -z "$verbose" ] ; then
  print_producer_vars
fi
validate_is_built

if [ "$system_message_producer" = true ] ; then
  start_system_message_producer 
fi

if [ "$soh_producer" = true ] ; then
  start_producer $injectable_type "systemEvent.json" $topic $mount
fi

exit 0;
