#
#  Dockerfile to make a configured ironbank postgres image for GMS.
#
ARG SOURCE_IMAGE
ARG SOURCE_TAG
FROM ${SOURCE_IMAGE}:${SOURCE_TAG}

ENV CONFIGDIR=/etc/postgresql
ENV POSTGRES_HOST_AUTH_METHOD="scram-sha-256"
ENV POSTGRES_INITDB_ARGS="--data-checksums -A --auth=scram-sha-256 --auth-host=scram-sha-256 --auth-local=scram-sha-256"
# set PGDATA to a subdir per the documentation regarding chmod issues
ENV PGDATA=/var/lib/postgresql/data/pgdata

USER 0

#Copy in our init scripts
COPY src/db-scripts/* /docker-entrypoint-initdb.d/
COPY src/gms-docker-entrypoint.sh /usr/local/bin/gms-docker-entrypoint.sh

# copy the configuration files
COPY src/postgresql.conf $CONFIGDIR/
COPY src/pg_hba.conf $CONFIGDIR/

#Add write permission for the custom entrypoint to perform sed
RUN set -ex && \
    chmod -R a+w /docker-entrypoint-initdb.d

# postgres uid
USER 26

ENTRYPOINT ["gms-docker-entrypoint.sh"]

# note: the config_file argument is set in gms-docker-entrypoint.sh
CMD ["postgres"]