# Run the pre-loader

ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
FROM $DOCKER_REGISTRY/gms-common/java:$DOCKER_IMAGE_TAG

USER 0

WORKDIR /data-preloader/

COPY bin ./data-preloader/bin
COPY lib ./data-preloader/lib

RUN set -ex && \
    chown -R gms:gms /data-preloader

# gms uid
USER 1001

# Start the pre-loader
###PARAMETER COUNT                  $1               $2              $3                   $4            $5                 $6                      $7                   $8                             $9                   $10                            $11                   $12                             $13             $14                       $15                   $16                       $17                          $18                          $19                           $20                           $21               $22               $23                $24                              $25                          $26                          $27              $28              $29               $30                             $31
