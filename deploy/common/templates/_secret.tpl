{{/*
Render static secrets for app. The secrets are defined with the app, but
they are available to the entire namespace. Data is expected to be
b64 encoded already, stringData will automatically b64 encode.

Secret data is static and the existing values will be reused during
an upgrade. This allows for using helm uuidv4 function to create
random generated data, but prevent it from changing during upgrade.

Usage:
  {{- include "gms.common.secret" .appContext }}
*/}}
{{- define "gms.common.secret" }}
  {{- range $name, $secretDef := .appValues.secret }}
    {{- $existing_secret := (lookup "v1" "Secret" $.Release.Namespace $name) }}
---
apiVersion: v1
kind: Secret
metadata:
  name: "{{ $name }}"
  labels:
    {{- include "gms.common.labels.standard" $ | trim | nindent 4 }}
type: {{ $secretDef.type | default "Opaque" | quote }}
    {{- if $existing_secret }}
data:
      {{- range $key, $value := $existing_secret.data }}
  {{ $key }}: {{ $value }}
      {{- end }}
    {{- else }}
      {{- if $secretDef.data }}
data:
        {{- range $key, $value := $secretDef.data }}
  {{ $key }}: {{ tpl $value $ }}
        {{- end }}
      {{- end }}
      {{- if $secretDef.stringData }}
stringData:
        {{- range $key, $value := $secretDef.stringData }}
  {{ $key }}: {{- tpl $value $ | toYaml | indent 2 }}
        {{- end }}
      {{- end }}
    {{- end }}
  {{- end }}
{{- end }}


{{/*
Copy multiple secrets from another namespace as defined in the .Values.copySecrets.
The source and destination names default to the key name. Destination name can be overidden
by specifying destName. The source namespace must always be specified.

Usage:
  {{- include "gms.common.secrets.copy" $ }
*/}}
{{- define "gms.common.secrets.copy" }}
  {{- if .Values.copySecrets }}
    {{- range $key, $secretDef := .Values.copySecrets }}
      {{- if not $secretDef }}
        {{- printf "ERROR: copySecrets.%s cannot be empty" $key | fail }}
      {{- end }}
      {{- if not $secretDef.namespace }}
        {{- printf "ERROR: copySecrets.%s.namespace cannot be empty" $key | fail }}
      {{- end }}
      {{- if $secretDef.destName }}
        {{- include "gms.common.secret.copy" (dict "context" $ "namespace" $secretDef.namespace "source_name" $key "dest_name" $secretDef.destName) }}
      {{- else }}
        {{- include "gms.common.secret.copy" (dict "context" $ "namespace" $secretDef.namespace "source_name" $key) }}
      {{- end }}
    {{- end }}
  {{- end }}
{{- end }}


{{/*
Copy a secret from another namespace.
namespace: source namespace
source_name: source secret name
dest_name: destination secret name, optional. Will use source_name if not defined.

Usage:
  {{- include "gms.common.secret.copy" (dict "context" $ "namespace" "gms" "source_name" "ingress-cert" "dest_name" "my-cert") }}
*/}}
{{- define "gms.common.secret.copy" }}
  {{/* Check if running dryrun or template by getting the namespace, it will return empty in that case */}}
  {{- if (lookup "v1" "Namespace" "" .context.Release.Namespace) }}
    {{- $existing_secret := (lookup "v1" "Secret" .namespace .source_name) }}
    {{- if not $existing_secret }}
      {{- printf "ERROR: Secret %s does not exist in %s namespace" .source_name .namespace | fail }}
    {{- end }}
---
apiVersion: v1
kind: Secret
metadata:
  name: {{ .dest_name | default .source_name | quote }}
  labels:
    {{- include "gms.common.labels.standard" .context | trim | nindent 4 }}
type: {{ $existing_secret.type }}
data:
      {{- range $key, $value := $existing_secret.data }}
  {{ $key }}: {{ $value | default "\"\"" }}
      {{- end }}
  {{- end }}
{{- end }}


{{/*
Create the oracle-wallet secret either from the embedded container-wallet
or copy it from the gms namespace. If global.oracleWalletOverride is set
then the override wallet is always used.

Usage:
  {{- include "gms.common.secret.oracleWallet" $ }}
*/}}
{{- define "gms.common.secret.oracleWallet" }}
  {{- if or $.Values.augmentation.oracle.enabled $.Values.global.oracleWalletOverride }}
---
apiVersion: v1
kind: Secret
metadata:
  name: oracle-wallet
  labels:
    {{- include "gms.common.labels.standard" $ | trim | nindent 4 }}
type: Opaque
data:
    {{- if $.Values.global.oracleWalletOverride }}
      {{- ($.Files.Glob "oracle-wallet-override/*").AsSecrets | nindent 2 }}
    {{- else }}
      {{- ($.Files.Glob "container-wallet/*").AsSecrets | nindent 2 }}
    {{- end }}
  {{- else }}
    {{- include "gms.common.secret.copy" (dict "context" $ "namespace" "gms" "source_name" "oracle-wallet-default" "dest_name" "oracle-wallet") }}
  {{- end }}
{{- end }}