{{/*
Render the app's container ports.
Usage:
          ports:
            {{- include "gms.common.network.ports" $appContext | trim | nindent 12 }}
*/}}
{{- define "gms.common.network.ports" }}
  {{- if .appValues.network }}
    {{- /* The list of ports comes from the .appValues.network.service keys, plus ports 10800, 47100, 47500 if the app uses Ignite. */ -}}
    {{- $ports := list }}
    {{- $ports := concat $ports (ternary list (keys .appValues.network.service) (empty .appValues.network.service)) }}
    {{- $ports := concat $ports (ternary list (list 10800 47100 47500) (empty .appValues.network.ignite)) }}
    {{- range $port := $ports | uniq | sortAlpha }}
      {{- if atoi $port }}
- containerPort: {{ $port }}
      {{- end }}
    {{- end }}
  {{- end }}
{{- end }}



{{/*
Render the app's Service object.
Usage:
{{- include "gms.common.network.service" $appContext }}
*/}}
{{- define "gms.common.network.service" }}
  {{- if .appValues.network }}
    {{- if .appValues.network.service }}
---
apiVersion: v1
kind: Service
metadata:
  labels:
    {{- include "gms.common.labels.standard" . | trim | nindent 4 }}
  annotations:
      {{- if .appValues.network.metrics }}
        {{- if or (not .appValues.network.metrics.path) (not .appValues.network.metrics.port) }}
          {{- printf "ERROR: Map %s.network.metrics must contain both .path and .port: %s" $.appName .appValues.network.metrics | fail }}
        {{- end }}
    prometheus.io/path: {{ .appValues.network.metrics.path | quote }}
    prometheus.io/port: {{ .appValues.network.metrics.port | quote }}
    prometheus.io/scheme: "http"
    prometheus.io/scrape: "true"
      {{- else }}
    prometheus.io/scrape: "false"
      {{- end }}
    networking.istio.io/exportTo: ".,istio-system"
  name: {{ $.appName | quote }}
spec:
  ports:
      {{- range $port, $serviceDef := .appValues.network.service }}
        {{- if atoi $port }}
          {{- if not $serviceDef }}
            {{- printf "ERROR: %s.network.service.%s cannot be empty" $.appName $port | fail }}
          {{- end }}
          {{- if not $serviceDef.name }}
            {{- printf "ERROR: %s.network.service.%s must contain .name: %s" $.appName $port $serviceDef | fail }}
          {{- end }}
    - name: {{ $serviceDef.name | quote }}
      port: {{ $port }}
      protocol: {{ $serviceDef.protocol | default "TCP" | quote }}
      targetPort: {{ $serviceDef.targetPort | default $port }}
        {{- end }}
      {{- end }}
      {{- if .Values.global.injector }}
        {{- if eq .appName "da-connman" }}
    - name: tcp-cd11connections
      port: {{ .appValues.connPort }}
      protocol: TCP
      targetPort: {{ .appValues.connPort }}
        {{- else if eq .appName "da-dataman" }}
          {{- range (untilStep (int .appValues.dataPortStart) (int (add .appValues.dataPortEnd 1)) 1) }}
    - name: tcp-cd11{{ . }}
      port: {{ . }}
      protocol: TCP
      targetPort: {{ . }}
          {{- end }}
        {{- end }}
      {{- end }}
  selector:
    app: {{ $.appName | quote }}
  type: ClusterIP
    {{- end }}
  {{- end }}
{{- end }}



{{/*
Render the app's VirtualService object (if Istio is enabled).
Each .appValues.network.ingress entry optionally supports a 'weight' value that, if present, will be used to sort the entries.
If omited, the default weight value of 100 will be used.  Since Helm doesn't seem to have a numeric sort function, all weights
must be between 100 and 999 so that alphaSorting them will work as expected.  Entries with equal weights will show up in
alphaSort order on the port number.
Usage:
{{- include "gms.common.network.virtualService" $appContext }}
*/}}
{{- define "gms.common.network.virtualService" }}
  {{- if .Values.global.istio }}
    {{- if .appValues.network }}
      {{- if .appValues.network.ingress }}
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: {{ $.appName | quote }}
  labels:
    {{- include "gms.common.labels.standard" . | trim | nindent 4 }}
spec:
  exportTo:
    - "."
    - "istio-system"
  gateways:
    - "istio-system/ingress-default-gateway"
  hosts:
        {{- $host := .appValues.network.ingress.host | default "{{ .Release.Name }}.{{ .Values.global.baseDomain }}" }}
    - {{ tpl $host $ | quote }}
  http:
        {{- /* This first loop builds up a new $byWeight dictionary that maps weight values to a list of ports using that weight. */ -}}
        {{ $byWeight := dict }}
        {{- range $port, $ingressDef := .appValues.network.ingress }}
          {{- if atoi $port }}
            {{- if not $ingressDef }}
              {{- printf "ERROR: %s.network.ingress.%s cannot be empty" $.appName $port | fail }}
            {{- end }}
            {{- $weight := $ingressDef.weight | default "100" | toString }}
            {{- if or (lt ($weight | int) 100) (gt ($weight | int) 999) }}
              {{- printf "ERROR: %s.network.ingress.%s has weight %s, which must be between 100 and 999: %s" $.appName $port $weight $ingressDef | fail }}
            {{- end }}
            {{- $existingList := get $byWeight $weight | default list }}
            {{- $newList := append $existingList $port }}
            {{- $_ := set $byWeight $weight $newList }}
          {{- end }}
        {{- end }}
        {{- /* This loop then iterates in order of increasing weights, generating output for each port at that weight. */ -}}
        {{- range $weight, $ports := $byWeight }}
          {{- range $port := $ports }}
            {{- $ingressDef := get $.appValues.network.ingress $port }}
            {{- if not $ingressDef.path }}
              {{- printf "ERROR: %s.network.ingress.%s must contain .path: %s" $.appName $port $ingressDef | fail }}
            {{- end }}
    - match:
        - uri:
            prefix: {{ tpl $ingressDef.path $ | quote }}
      route:
        - destination:
            host: "{{ $.appName }}.{{ $.Release.Name }}.svc.cluster.local"
            port:
              number: {{ $port }}
          {{- end }}
        {{- end }}
        {{- if eq .appName "da-connman" }}
  tcp:
    - match:
        - port: {{ .appValues.connPort }}
      route:
        - destination:
            host: "{{ .appName }}.{{ .Release.Name }}.svc.cluster.local"
            port:
              number: {{ .appValues.connPort }}
        {{- else if and .Values.global.injector (eq .appName "da-dataman") }}
  tcp:
          {{- range (untilStep (int .appValues.dataPortStart) (int (add .appValues.dataPortEnd 1)) 1) }}
    - match:
      - port: {{ . }}
      route:
      - destination:
          host: "{{ $.appName }}.{{ $.Release.Name }}.svc.cluster.local"
          port:
            number: {{ . }}
         {{- end }}
        {{- end }}
      {{- end }}
    {{- end }}
  {{- end }}
{{- end }}



{{/*
Render the app's Ingress object. If istio is enabled, a 302 redirect is configured to redirect to the istio url.
Each .appValues.network.ingress entry optionally supports a 'weight' value that, if present, will be used to sort the entries.
If omited, the default weight value of 100 will be used.  Since Helm doesn't seem to have a numeric sort function, all weights
must be between 100 and 999 so that alphaSorting them will work as expected.  Entries with equal weights will show up in
alphaSort order on the port number.
Usage:
{{- include "gms.common.network.ingress" $appContext }}
*/}}
{{- define "gms.common.network.ingress" }}
    {{- if .appValues.network }}
      {{- if .appValues.network.ingress }}
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  labels:
    {{- include "gms.common.labels.standard" . | trim | nindent 4 }}
        {{- if or .appValues.network.ingress.annotations .Values.global.istio }}
  annotations:
          {{- if .appValues.network.ingress.annotations }}
            {{- .appValues.network.ingress.annotations | toYaml | trim | nindent 4 }}
          {{- end }}
          {{- if .Values.global.istio }}
    nginx.ingress.kubernetes.io/configuration-snippet: 'return 302 https://$host:{{ .Values.global.basePort }}$request_uri;'
          {{- end }}
        {{- end }}
  name: {{ $.appName | quote }}
spec:
  rules:
        {{- $host := .appValues.network.ingress.host | default "{{ .Release.Name }}.{{ .Values.global.baseDomain }}" }}
    - host: {{ tpl $host $ | quote }}
      http:
        paths:
        {{- /* This first loop builds up a new $byWeight dictionary that maps weight values to a list of ports using that weight. */ -}}
        {{ $byWeight := dict }}
        {{- range $port, $ingressDef := .appValues.network.ingress }}
          {{- if atoi $port }}
            {{- if not $ingressDef }}
              {{- printf "ERROR: %s.network.ingress.%s cannot be empty" $.appName $port | fail }}
            {{- end }}
            {{- $weight := $ingressDef.weight | default "100" | toString }}
            {{- if or (lt ($weight | int) 100) (gt ($weight | int) 999) }}
              {{- printf "ERROR: %s.network.ingress.%s has weight '%s', which must be between 100 and 999: %s" $.appName $port $weight $ingressDef | fail }}
            {{- end }}
            {{- $existingList := get $byWeight $weight | default list }}
            {{- $newList := append $existingList $port }}
            {{- $_ := set $byWeight $weight $newList }}
          {{- end }}
        {{- end }}
        {{- /* This loop then iterates in order of increasing weights, generating output for each port at that weight. */ -}}
        {{- range $weight, $ports := $byWeight }}
          {{- range $port := $ports }}
            {{- $ingressDef := get $.appValues.network.ingress $port }}
            {{- if not $ingressDef.path }}
              {{- printf "ERROR: %s.network.ingress.%s must contain .path: %s" $.appName $port $ingressDef $ingressDef | fail }}
            {{- end }}
          - backend:
              service:
                name: {{ $.appName | quote }}
                port:
                  number: {{ $port }}
            path: {{ tpl $ingressDef.path $ | quote }}
            pathType: {{ $ingressDef.pathType | default "Prefix" | quote }}
          {{- end }}
        {{- end }}
  tls:
    - hosts:
        - {{ tpl $host $ | quote }}
      secretName: ingress-default-cert
      {{- end }}
    {{- end }}
{{- end }}
