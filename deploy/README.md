# GMS Helm Charts Overview

This directory contains Helm charts for different configurations of the GMS
system:

* **ian** - Interactive Analysis (IAN) data bridge and analyst tools
* **soh** - Station State-of-Health (SOH) Monitoring
* **sb** - Standalone Bridge (SB)
* **logging** - Logging Stack
* **[common](./common/GMS_SUBCHART_README.md)** - Common Helm library chart with common GMS templates - not a deployable chart.
* **[kafka](./kafka/GMS_SUBCHART_README.md)** - Common Kafka helm chart used by IAN, SOH, SB - not a deployable chart
* **[augmentation](./augmentation/GMS_SUBCHART_README.md)** - Common items that can be
  added to a running GMS instance for testing and debug - not a deployable chart

The `gmskube` script must be used to start instances of these
charts. The `gmskube` script will provide the myriad of key/value
settings required for these charts. It will also coordinate the
configuration loading required to bootstrap the system at startup.

Run `gmskube --help` for information on commands.

## Requirements
* Docker - At least docker 1.13 be installed and running
* Kubeconfig - Configure `kubectl` to talk to a Kubernetes cluster.
  1. Login to the Rancher interface for the cluster you will be using.
  2. Click on the cluster name in the list
  3. In the upper right, click on the blue **Kubeconfig File** button.
  4. Copy/paste the provided file contents into `~/.kube/<cluster_name>.config` on your development machine.
  5. Run `kubectl get no` to confirm that things are working.
* Gms-common source - A clone of gms-common on your development machine, with `gms-common/.bash_env` sourced.
