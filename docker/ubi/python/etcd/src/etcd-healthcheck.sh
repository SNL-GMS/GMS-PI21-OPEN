#!/bin/sh

etcdctl endpoint health --user "${ETCD_GMS_USER}:${ETCD_GMS_PASSWORD}"

if [ ?$ -ne 0 ]; then
    echo "GMS system configuration etcd server not running..."
    exit 1
fi

echo "GMS system configuration is available..."
exit 0
