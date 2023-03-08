{{/*
Render the app overall Kubernetes Job object
Example:
  {{- include "gms.common.job" $appContext }}
*/}}
{{- define "gms.common.job" }}
---
apiVersion: batch/v1
kind: Job
metadata:
  name: {{ .appName }}
  labels:
    {{- include "gms.common.labels.standard" . | trim | nindent 4 }}
  {{- if .appValues.jobAnnotations }}
  annotations:
    {{- tpl (toYaml .appValues.jobAnnotations) $ | trim | nindent 4 }}
  {{- end }}
spec:
  {{- if (hasKey .appValues "backoffLimit") }}
  backoffLimit: {{ .appValues.backoffLimit }}
  {{- end }}
  {{- if (hasKey .appValues "numIdenticalPods") }}
  completions: {{ .appValues.numIdenticalPods }}
  parallelism: {{ .appValues.numIdenticalPods }}
  {{- end }}
  {{- if (hasKey .appValues "ttlSecondsAfterFinished") }}
  ttlSecondsAfterFinished: {{ .appValues.ttlSecondsAfterFinished }}
  {{- end }}
  template:
    metadata:
      labels:
        {{- include "gms.common.labels.standard" . | trim | nindent 8 }}
        {{- include "gms.common.labels.podLabels" . | trim | nindent 8 }}
      annotations:
        sidecar.istio.io/inject: "false"
  {{- if .appValues.podAnnotations }}
        {{- tpl (toYaml .appValues.podAnnotations) $ | trim | nindent 8 }}
  {{- end }}
    spec:
      affinity:
        {{- include "gms.common.pod.affinity" . | trim | nindent 8 }}
      securityContext:
        {{- include "gms.common.pod.securityContext" . | trim | nindent 8 }}
      serviceAccount: gms
  {{- if .appValues.restartPolicy }}
      restartPolicy: {{ .appValues.restartPolicy | quote }}
  {{- end }}
  {{- if (hasKey .appValues "terminationGracePeriodSeconds") }}
      terminationGracePeriodSeconds: {{ .appValues.terminationGracePeriodSeconds }}
  {{- end }}
      volumes:
        {{- include "gms.common.storage.volumes" . | trim | nindent 8 }}
      containers:
        - name: {{ .appName }}
          image: {{ include "gms.common.container.image" . }}
          imagePullPolicy: {{ .Values.global.imagePullPolicy }}
          env:
            {{- include "gms.common.container.env" . | trim | nindent 12 }}
          ports:
            {{- include "gms.common.network.ports" . | trim | nindent 12 }}
          resources:
            {{- include "gms.common.container.resources" . | trim | nindent 12 }}
          securityContext:
            {{- include "gms.common.container.securityContext" . | trim | nindent 12 }}
          volumeMounts:
            {{- include "gms.common.storage.volumeMounts" . | trim | nindent 12 }}
  {{- if .appValues.command }}
          command:
            {{- tpl (toYaml .appValues.command) $ | trim | nindent 12 }}
  {{- end }}
  {{- if .appValues.args }}
          args:
            {{- tpl (toYaml .appValues.args) $ | trim | nindent 12 }}
  {{- end }}
{{- end }}