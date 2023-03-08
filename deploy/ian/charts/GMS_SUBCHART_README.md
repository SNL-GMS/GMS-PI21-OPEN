# IAN Sub-charts
[Common](common/GMS_SUBCHART_README.md) and [Kafka](kafka/GMS_SUBCHART_README.md) are symbolically 
linked sub-charts. See their readme files for more details. 

All configuration for these sub-charts should occur in the main `values.yaml` file in IAN.
No customizations should be made here unless absolutely necessary. The goal is that these charts can
be upgraded directly from the internet with no modifications.

However, there are some cases where modifications are necessary. These changes should be detailed below
so they can be reproduced when upgrading the charts:
* reactive-interaction-gateway
  * templates/deployment.yaml (line 23) - change image line to support tags from values `image: "{{ .Values.image.repository }}:{{ .Chart.AppVersion }}"` to `image: "{{ .Values.global.imageRegistry }}/{{ .Values.imageName }}:{{ .Values.global.imageTag }}"`
  * templates/service-headless.yaml (line 14) - change name to `http-proxy` for istio
  * templates/service-headless.yaml (line 18) - change name to `https-proxy` for istio
  * templates/service-headless.yaml (line 22) - change name to `http-internal` for istio
  * templates/service-headless.yaml (line 26) - change name to `https-internal` for istio