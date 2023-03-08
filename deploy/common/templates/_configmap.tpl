{{/*
Render the standard GMS ConfigMap that is required by gmskube
Usage:
{{- include "gms.common.configMap.standard" $ }}
*/}}
{{- define "gms.common.configMap.standard" }}
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: "gms"
  labels:
    {{- include "gms.common.labels.standard" . | trim | nindent 4 }}
    gms/type: {{ .Chart.Name | quote }}
    gms/user: {{ .Values.global.user | default "UNKNOWN" | quote }}
    gms/name: {{ .Release.Name | quote }}
    gms/namespace: {{ .Release.Namespace | quote }}
    gms/image-tag: {{ .Values.global.imageTag | quote }}
    gms/update-time: {{ dateInZone "2006-01-02T150405Z" (now) "UTC" | quote }}
  {{- if eq .Chart.Name "soh" }}
    gms/cd11-live-data: {{ .Values.global.liveData | quote }}
    gms/cd11-connman-port: {{ get (get .Values "da-connman") "connPort" | quote }}
    gms/cd11-dataman-port-start: {{ get (get .Values "da-dataman") "dataPortStart" | quote }}
    gms/cd11-dataman-port-end: {{ get (get .Values "da-dataman") "dataPortEnd" | quote }}
  {{- end }}
{{- end }}


{{/*
Render configmap for app. The configmap are defined with the app, but
they are available to the entire namespace.

Usage:
  {{- include "gms.common.configMap" .appContext }}
*/}}
{{- define "gms.common.configMap" }}
  {{- range $name, $configDef := .appValues.configMap }}
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: "{{ $name }}"
  labels:
    {{- include "gms.common.labels.standard" $ | trim | nindent 4 }}
data:
    {{- range $key, $value := $configDef.data }}
  {{ $key }}: {{- tpl $value $ | toYaml | indent 2 }}
    {{- end }}
  {{- end }}
{{- end }}


{{/*
Copy multiple configMaps from another namespace as defined in the .Values.copyConfigMaps.
The source and destination names default to the key name. Destination name can be overidden
by specifying destName. The source namespace must always be specified.

Usage:
  {{- include "gms.common.configMaps.copy" $ }
*/}}
{{- define "gms.common.configMaps.copy" }}
  {{- if .Values.copyConfigMaps }}
    {{- range $key, $configDef := .Values.copyConfigMaps }}
      {{- if not $configDef }}
        {{- printf "ERROR: copyConfigMaps.%s cannot be empty" $key | fail }}
      {{- end }}
      {{- if not $configDef.namespace }}
        {{- printf "ERROR: copyConfigMaps.%s.namespace cannot be empty" $key | fail }}
      {{- end }}
      {{- if $configDef.destName }}
        {{- include "gms.common.configMap.copy" (dict "context" $ "namespace" $configDef.namespace "source_name" $key "dest_name" $configDef.destName) }}
      {{- else }}
        {{- include "gms.common.configMap.copy" (dict "context" $ "namespace" $configDef.namespace "source_name" $key) }}
      {{- end }}
    {{- end }}
  {{- end }}
{{- end }}


{{/*
Copy a configmap from another namespace.
namespace: source namespace
source_name: source configmap name
dest_name: destination configmap name, optional. Will use source_name if not defined.

Usage:
  {{- include "gms.common.configMap.copy" (dict "context" $ "namespace" "gms" "source_name" "ingress-cert" "dest_name" "my-cert") }}
*/}}
{{- define "gms.common.configMap.copy" }}
  {{/* Check if running dryrun or template by getting the namespace, it will return empty in that case */}}
  {{- if (lookup "v1" "Namespace" "" .context.Release.Namespace) }}
    {{- $existing_configMap := (lookup "v1" "ConfigMap" .namespace .source_name) }}
    {{- if not $existing_configMap }}
      {{- printf "ERROR: ConfigMap %s does not exist in %s namespace" .source_name .namespace | fail }}
    {{- end }}
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: {{ .dest_name | default .source_name | quote }}
  labels:
    {{- include "gms.common.labels.standard" .context | trim | nindent 4 }}
data:
      {{- range $key, $value := $existing_configMap.data }}
  {{ $key }}: {{ $value | quote }}
      {{- end }}
  {{- end }}
{{- end }}