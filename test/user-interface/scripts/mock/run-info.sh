#!/bin/bash


search_str="kafka|zookeeper|etcd|soh|frameworks-data-injector"

function explain_basic_usage {
  ./util/validate_env_vars.sh
  echo "#############"
  echo "Mock Info Help"
  echo "Searches for containers, volumes, images and networks that contain one of the following: "
  echo "  $search_str"
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

echo "docker ps | grep -E "$search_str""
docker ps | grep -E "$search_str"
echo
echo "docker volume ls | grep -E "$search_str""
docker volume ls | grep -E "$search_str"
echo 
echo "docker network ls | grep -E "$search_str""
docker network ls | grep -E "$search_str"
echo
echo "docker image ls | grep -E "$search_str""
docker image ls | grep -E "$search_str"
