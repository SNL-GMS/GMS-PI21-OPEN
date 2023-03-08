# Kafka Sub-chart
This sub-chart is common to SOH, IAN, and SB. It is symbolically linked into the `charts` directory
in each chart type to prevent duplication of the chart.

All configuration for this sub-chart should occur in the main `values.yaml` file in SOH, IAN, or SB.
No customizations should be made here unless absolutely necessary. The goal is that this chart can
be upgraded directly from the internet with no modifications.

However, there are some cases where modifications are necessary. These changes should be detailed below
so they can be reproduced when upgrading the chart:
* kafka
  * charts/zookeeper/templates/svc-headless.yaml (line 35) - change name to `tcp-follower` for istio
  * charts/zookeeper/templates/svc.yaml (line 46) - change name to `tcp-follower` for istio
  * README.md - content replaced with a link due to fortify finding
  * templates/NOTES.txt - delete due to fortify finding
  * templates/kafka-provisioning.yaml (line 45) - added full path to /opt/bitnami/common/bin/wait-for-port
    due to ironbank image not having the path set correctly.
  * templates/statefulset.yaml (line 270-273) - comment out JMX_PORT env var to prevent duplicate port
    when running kafka scripts. CMD and ARGS are changed in the values file to set JMX_PORT. See
    [this PR discussion](https://github.com/bitnami/charts/pull/10284) for more info.