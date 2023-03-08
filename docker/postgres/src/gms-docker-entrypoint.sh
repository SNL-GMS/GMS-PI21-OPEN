#!/bin/bash

for pw in \
    'GMS_POSTGRES_ADMIN_PASSWORD' \
    'GMS_POSTGRES_CONFIG_APPLICATION_PASSWORD' \
    'GMS_POSTGRES_READ_ONLY_PASSWORD' \
    'GMS_POSTGRES_SESSION_APPLICATION_PASSWORD' \
    'GMS_POSTGRES_SOH_APPLICATION_PASSWORD' \
    'GMS_POSTGRES_SOH_APPLICATION_ELEVATED_PASSWORD' \
    'GMS_POSTGRES_SOH_TEST_APPLICATION_PASSWORD' \
    'GMS_POSTGRES_SOH_TTL_APPLICATION_PASSWORD'; do

    if [ -z ${!pw} ]; then
        echo "gms-docker-entrypoint: '${pw}' is NOT SET but is REQUIRED. Skipping..."
    else
        echo "gms-docker-entrypoint: updating ${pw}"
        for f in ./docker-entrypoint-initdb.d/*.sql; do
            sed -i "s/${pw}/${!pw}/g" $f
        done
    fi
done

if [[ "$*" != *"-c config_file="* ]]; then
  # add config_file argument if it does not exist
  set -- "$@" '-c' 'config_file=/etc/postgresql/postgresql.conf'
fi

# exec the docker CMD
echo "gms-docker-entrypoint: handing off to docker-entrypoint.sh $@"
exec docker-entrypoint.sh "$@"
