{{/*
Render a standard GMS app of kind job or deployment. Default is deployment if 'kind' not specified.
Usage:
{{- include "gms.common.app.standard" $appContext }}
*/}}
{{- define "gms.common.app.standard" }}
  {{- if not .appValues }}
    {{- printf "ERROR: standard app '%s' has no appValues defined in the chart's values.yaml file" $.appName | fail }}
  {{- end }}
  {{- $kind := .appValues.kind | default "deployment" }}
  {{- if eq (lower $kind) "job" }}
    {{- include "gms.common.job" . }}
  {{- else if eq (lower $kind) "deployment" }}
    {{- include "gms.common.deployment" . }}
  {{- else }}
    {{- printf "ERROR: unrecognized 'kind' in '%s', must be one of 'job' or 'deployment'" $.appName | fail }}
  {{- end }}
  {{- include "gms.common.storage.pvc" . }}
  {{- include "gms.common.network.service" . }}
  {{- include "gms.common.network.virtualService" . }}
  {{- include "gms.common.network.ingress" . }}
  {{- include "gms.common.secret" . }}
  {{- include "gms.common.configMap" . }}
{{- end }}

{{/*
Render a standard GMS augmentation app. Augmentations are just standard apps, but wrapped in an enabled conditional.
Usage:
{{- include "gms.common.app.augmentation" $appContext }}
*/}}
{{- define "gms.common.app.augmentation" }}
  {{- if .appValues.enabled }}
    {{- include "gms.common.app.standard" . }}
  {{- end }}
{{- end }}