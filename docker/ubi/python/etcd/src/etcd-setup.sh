#!/bin/bash

# This script is run to set up etcd

set -eu

#-- Start etcd temporarily for configuration and loading
etcd &
etcdpid=$!

#-- Wait for etcd to fully initialize
until etcdctl endpoint health; do
    sleep 1
done

#-- Add 'root' user and enable authentication
etcdctl user add "root:4e8ca397f62c45c8939337cc38de974ffd9ccd6a"
etcdctl auth enable

#-- Setup 'read-everything' and 'readwrite-everything' roles
etcdctl role add read-everything --user "root:4e8ca397f62c45c8939337cc38de974ffd9ccd6a"
etcdctl role add readwrite-everything --user "root:4e8ca397f62c45c8939337cc38de974ffd9ccd6a"
etcdctl role grant-permission --prefix read-everything read '' --user "root:4e8ca397f62c45c8939337cc38de974ffd9ccd6a"
etcdctl role grant-permission --prefix readwrite-everything readwrite '' --user "root:4e8ca397f62c45c8939337cc38de974ffd9ccd6a"

#-- Setup 'gmsadmin' user 
etcdctl user add "gmsadmin:36df65feabbcf9ea0e6928928c3c2faeffb8d6e5" --user "root:4e8ca397f62c45c8939337cc38de974ffd9ccd6a"
etcdctl user grant-role gmsadmin readwrite-everything --user "root:4e8ca397f62c45c8939337cc38de974ffd9ccd6a"

#-- Load configuration as 'gmsadmin'
gms-sysconfig --username gmsadmin --password "36df65feabbcf9ea0e6928928c3c2faeffb8d6e5" --endpoints localhost load /setup/config/system/gms-system-configuration.properties

#-- Setup 'gms' user
etcdctl --dial-timeout=6s user add "gms:99f3bc766de7ac915f6e2ed248b67750156d6529" --user "root:4e8ca397f62c45c8939337cc38de974ffd9ccd6a"
etcdctl --dial-timeout=6s user grant-role gms read-everything --user "root:4e8ca397f62c45c8939337cc38de974ffd9ccd6a"
sleep 1

#-- Stop the now-configured etcd
kill ${etcdpid}


