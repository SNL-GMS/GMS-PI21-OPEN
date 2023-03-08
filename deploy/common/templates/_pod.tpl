{{/*
Return pod Affinity definion. Currently this only supports nodeAffinity, but could be exapnded later to support podAffinity.
Tpl expansion is available on key and values. 
Usage:
      affinity:
        {{- include "gms.common.pod.affinity" $appContext | trim | nindent 8 }}

Values file example for a gms application:
  myvar: "test"
  nodeAffinity:
    type: "soft"
    key: "kubernetes.io/hostname"
    operator: "In"
    values:
      - "{{ .appValues.myvar }}"
*/}}
{{- define "gms.common.pod.affinity" }}
  {{- if .appValues.nodeAffinity }}
    {{- include "gms.common.pod.nodeAffinity" . }}
  {{- end }}
{{- end }}


{{/*
Return a soft or hard nodeAffinity definition
Usage:
  {{ include "gms.common.pod.nodeAffinity" . }}
*/}}
{{- define "gms.common.pod.nodeAffinity" }}
  {{- if or (not .appValues.nodeAffinity.key) (not .appValues.nodeAffinity.operator) (not .appValues.nodeAffinity.type) (not .appValues.nodeAffinity.values) }}
    {{- printf "ERROR: %s.nodeAffinity must contain .key, .operator, .type, and .values: %s" $.appName .appValues.nodeAffinity | fail }}
  {{- end }}
  {{- if eq .appValues.nodeAffinity.type "soft" }}
nodeAffinity:
  preferredDuringSchedulingIgnoredDuringExecution:
    - weight: 1
      preference:
        matchExpressions:
          - key: {{ tpl .appValues.nodeAffinity.key $ | quote }}
            operator: {{ .appValues.nodeAffinity.operator | quote }}
            values:
              {{- range .appValues.nodeAffinity.values }}
              - {{ tpl . $ | quote }}
              {{- end }}

  {{- else if eq .appValues.nodeAffinity.type "hard" }}
nodeAffinity:
  requiredDuringSchedulingIgnoredDuringExecution:
    nodeSelectorTerms:
      - matchExpressions:
          - key: {{ tpl .appValues.nodeAffinity.key $ | quote }}
            operator: {{ .appValues.nodeAffinity.operator | quote }}
            values:
              {{- range .appValues.nodeAffinity.values }}
              - {{ tpl . $ | quote }}
              {{- end }}
  {{- end }}
{{- end }}


{{/*
Render the app's PodSecurityContext. Note that this is at the pod level, not the container level.
Usage:
    securityContext:
      {{- include "gms.common.pod.securityContext" $appContext | trim | nindent 8 }}

Values example:
  myapp:
    podSecurityContext:
      runAsNonRoot: true
      runAsUser: 65534
      runAsGroup: 65534
      fsGroup: 65534
*/}}
{{- define "gms.common.pod.securityContext" }}
  {{- $podSecurityContext := .appValues.podSecurityContext | default dict }}
runAsNonRoot: {{ $podSecurityContext.runAsNonRoot | default true }}
  {{- if $podSecurityContext.runAsUser }}
runAsUser: {{ $podSecurityContext.runAsUser }}
  {{- end }}
  {{- if $podSecurityContext.runAsGroup }}
runAsGroup: {{ $podSecurityContext.runAsGroup }}
  {{- end }}
  {{- if $podSecurityContext.fsGroup }}
fsGroup: {{ $podSecurityContext.fsGroup }}
  {{- end }}
{{- end }}
