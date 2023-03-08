#!/bin/bash
NAMESPACE=$(cat /var/run/secrets/kubernetes.io/serviceaccount/namespace)
sed "s/PLACEHOLDER/$NAMESPACE/g" ../../config/ian-kube-discovery.tmp > ../../config/ian-kube-discovery.xml
/opt/gms/cache-service/bin/cache-service visor
