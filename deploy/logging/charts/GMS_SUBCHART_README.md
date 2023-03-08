# Logging Sub-charts
[Common](common/GMS_SUBCHART_README.md) is a symbolically 
linked sub-chart. See the readme file for more details. 

All configuration for this sub-chart should occur in the main `values.yaml` file in Logging.
No customizations should be made here unless absolutely necessary. The goal is that these charts can
be upgraded directly from the internet with no modifications.

However, there are some cases where modifications are necessary. These changes should be detailed below
so they can be reproduced when upgrading the chart:
* elasticsearch
  * none
* fluentd
  * none
* kibana
  * none
