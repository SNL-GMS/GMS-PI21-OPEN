{{/*
Render the LimitRange object for the namespace, if enabled
Usage:
  {{- include "gms.common.limitrange" $ }}
*/}}
{{- define "gms.common.limitrange" }}
  {{- if .Values.limitRange.enabled }}
--
apiVersion: v1
kind: LimitRange
metadata:
  name: limit-range
  labels:
    {{- include "gms.common.labels.standard" . | trim | nindent 4 }}
spec:
  limits:
    {{- toYaml .Values.limitRange.limits | trim | nindent 4 }}
  {{- end }}
{{- end }}