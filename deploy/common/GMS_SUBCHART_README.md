# GMS Common Library Chart
This chart provides common GMS template helpers which can be used in other charts. 
It is symbolically linked into the other GMS charts as a sub-chart, so there is only one copy of 
the common source.

All template names should begin with `gms.common` so they do not conflict with other common charts,
such as bitnami common.

## App Context
Most of the templates expect to be passed the `appContext`, which is a dictionary consisting of the following:
- Top level root context ($)
- `appValues` set to the app-specific values
- `appName` set to the app name

This can be created with a series of `merge` and `deepCopy` operations. The `deepCopy` is necessary so the original data structures are not modified by `merge`, since `merge` will actually change the destination object. The following will create the app context:
```go
{{- $appContext := mustMergeOverwrite (mustDeepCopy $) (dict "appValues" (get $.Values $appName)) (dict "appName" $appName) }}
```
That can then be passed to various template functions, for example:
```go
{{- include "gms.common.app.augmentation" $appContext }}
```