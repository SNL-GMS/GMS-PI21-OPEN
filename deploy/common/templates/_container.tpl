{{/*
Render the container's resources requests and limits.
      containers:
        - name: "appname"
          image: "docker-image-name:tag"
          resources:
            {{- include "gms.common.container.resources" $appContext | trim | nindent 12 }}
*/}}
{{- define "gms.common.container.resources" }}
  {{- if or .appValues.cpuRequest .appValues.memoryRequest }}
requests:
    {{- if (.appValues.cpuRequest) }}
  cpu: {{ .appValues.cpuRequest }}
    {{- end }}
    {{- if (.appValues.memoryRequest) }}
  memory: {{ .appValues.memoryRequest }}
    {{- end }}
  {{- end }}
  {{- if or .appValues.cpuLimit .appValues.memoryLimit }}
limits:
    {{- if (.appValues.cpuLimit) }}
  cpu: {{ .appValues.cpuLimit }}
    {{- end }}
    {{- if (.appValues.memoryLimit) }}
  memory: {{ .appValues.memoryLimit }}
    {{- end }}
  {{- end }}
{{- end }}



{{/*
Render the container's securityContext with the ability to elevate for debugging. This can be set
with `--set <appName>.elevate` during install or upgrade. Note the container securityContext will override
any overlapping settings in the podSecurityContext. Currently this does not support setting individual
items in the values file, but could be expanded in the future.
      containers:
        - name: "appname"
          image: "docker-image-name:tag"
          securityContext:
            {{- include "gms.common.container.securityContext" $appContext | trim | nindent 12 }}
*/}}
{{- define "gms.common.container.securityContext" }}
  {{- if .appValues.elevate }}
runAsNonRoot: false
runAsUser: 0
  {{- else }}
allowPrivilegeEscalation: false
  {{- end }}
{{- end }}


{{/*
Render the docker image tag, truncated to 62 characters to match Gitlab's $CI_COMMIT_REF_SLUG.
Usage:
    {{ include "gms.common.container.imageTag" $appContext }}
*/}}
{{- define "gms.common.container.imageTag" }}
{{- .Values.global.imageTag | trunc 63 }}
{{- end }}



{{/*
Render the full Container image path and name, including tag.
Usage:
    image: {{ include "gms.common.container.image" $appContext }}
*/}}
{{- define "gms.common.container.image" }}
  {{- if contains ":" .appValues.imageName }}
    {{- printf "%s/%s" .Values.global.imageRegistry .appValues.imageName | quote }}
  {{- else }}
    {{- printf "%s/%s:%s" .Values.global.imageRegistry .appValues.imageName (include "gms.common.container.imageTag" .) | quote }}
  {{- end }}
{{- end }}


{{/*
Render the app environment variables for a app's container by combining the
global `global.env` and the app's `env` from values.yaml (per-app envs take
precedence), and expanding templates in the resulting values. Global env
inherritance is enabled by default and can be disabled by setting
useGlobalEnv: false in the appValues.
Usage:
          env:
            {{- include "gms.common.container.env" $appContext | trim | nindent 12 }}
*/}}
{{- define "gms.common.container.env" }}
  {{- $appEnvMap := .appValues.env | default dict }}
  {{- $globalEnvMap := dict }}
  {{- if ternary .appValues.useGlobalEnv true (hasKey .appValues "useGlobalEnv") }}
    {{- $globalEnvMap = .Values.global.env | default dict }}
  {{- end }}
  {{- range $key := keys $appEnvMap $globalEnvMap | uniq | sortAlpha }}
    {{- $val := ternary (get $appEnvMap $key) (get $globalEnvMap $key) (hasKey $appEnvMap $key) }}
    {{- if kindIs "map" $val }}
      {{- if empty $val }}
        {{- /* Do not output anything in this case */ -}}
      {{- else }}
        {{- if not $val.type }}
          {{- printf "ERROR: %s.env.%s is a map, so it must contain .type: %s" $.appName $key $val | fail }}
        {{- end }}
        {{- if eq $val.type "fromSecret" }}
          {{- if or (not $val.key) (not $val.name) }}
            {{- printf "ERROR: %s.env.%s is of type fromSecret, so it must contain both .key and .name: %s" $.appName $key $val | fail }}
          {{- end }}
- name: {{ $key | quote }}
  valueFrom:
    secretKeyRef:
      name: {{ $val.name | quote }}
      key: {{ $val.key | quote }}
        {{- else if eq $val.type "fromConfigMap" }}
          {{- if or (not $val.key) (not $val.name) }}
            {{- printf "ERROR: %s.env.%s is of type fromConfigMap, so it must contain both .key and .name: %s" $.appName $key $val | fail }}
          {{- end }}
- name: {{ $key | quote }}
  valueFrom:
    configMapKeyRef:
      name: {{ $val.name | quote }}
      key: {{ $val.key | quote }}
        {{- else }}
          {{- printf "ERROR: %s.env.%s is invalid: %s" $.appName $key $val | fail }}
        {{- end }}
      {{- end }}
    {{- else if kindIs "slice" $val }}
      {{- printf "ERROR: %s.env.%s cannot be an array: %s" $.appName $key $val | fail }}
    {{- else }}
- name: {{ $key | quote }}
  value: {{ tpl ($val | toString) $ | quote }}
    {{- end }}
  {{- end }}
{{- end }}
