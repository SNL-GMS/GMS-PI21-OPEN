{{/*
Kubernetes standard labels
Usage:
  labels:
    {{- include "gms.common.labels.standard" . | trim | nindent 4 }}
*/}}
{{- define "gms.common.labels.standard" }}
app.kubernetes.io/name: {{ default .Chart.Name .appName | trunc 63 | trimSuffix "-" | quote }}
helm.sh/chart: {{ printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" | quote }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
app.kubernetes.io/part-of: {{ .Chart.Name | quote }}
{{- end }}

{{/*
Render the app's restartAfterReconfig value.
Usage:
    labels:
      {{- include "gms.common.labels.restartAfterReconfig" $appContext | trim | nindent 4 }}
*/}}
{{- define "gms.common.labels.restartAfterReconfig" }}
restartAfterReconfig: {{ default "false" .appValues.restartAfterReconfig | quote }}
{{- end }}

{{/*
Labels to use on deploy.spec.selector.matchLabels and svc.spec.selector
*/}}
{{- define "gms.common.labels.matchLabels" }}
app.kubernetes.io/name: {{ .Chart.Name | trunc 63 | trimSuffix "-" }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Render the app's Pod labels.  This always adds an "app=appName" label.  And,
for apps that utilize Ignite, it also adds an "ignite=ignite" label.
Usage:
      labels:
        {{- include "gms.common.labels.podLabels" $appContext | trim | nindent 8 }}
*/}}
{{- define "gms.common.labels.podLabels" }}
app: {{ .appName }}
  {{- if .appValues.network }}
    {{- if .appValues.network.ignite }}
ignite: ignite
    {{- end }}
  {{- end }}
  {{- if .appValues.podLabels }}
{{- tpl (toYaml .appValues.podLabels) . }}
  {{- end }}
{{- end }}
