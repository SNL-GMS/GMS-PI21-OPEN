{/*
Render the Sidecar object for the namespace, if istio enabled
Usage:
  {{- include "gms.common.sidecar" $ }}
*/}}
{{- define "gms.common.sidecar" }}
  {{- if .Values.global.istio }}
---
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: default
  labels:
    {{- include "gms.common.labels.standard" . | trim | nindent 4 }}
spec:
  egress:
  - hosts:
    - "./*"
    - "istio-system/*"
  {{- end }}
{{- end }}
