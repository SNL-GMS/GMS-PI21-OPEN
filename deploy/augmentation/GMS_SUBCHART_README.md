# GMS Augmentations
This chart provides common GMS augmentations that can be applied to other GMS types. The `augmentation` chart 
is linked as a sub-chart into the other GMS charts. Augmentations are disabled by default and are only included
when the `augmentation.<name>.enabled=true` value is set. Gmskube handles applying the augmentations via the 
`gmskube augment` commands (`apply`, `delete` and `catalog`) and with the `gmskube install --augment` argument
during install.

## Defining augmentations
An augmentation is a standard application within helm. Most augmentations fall within the common
`deployment` or `job` templates defined in the `common` helper functions. These standard augmentations
are listed in the `values.yaml` file in the `standardAugmentationApps`.

Non-standard augmentations have their own file in `templates/app-*.yaml`. These augmentations need additional
items not included in the standard templates (secrets, configmaps, etc.)

Each augmentation must have its properties/values defined in the `values.yaml` file. The minimum requirements
for an app to be considered an augmentation are the `imageName` and `metadata` keys:
```yaml
example-augmentation:
  imageName: "gms-common/example"
  metadata:
    description: "A short description of the augmentation"
    labels: #optional labels
      - "type (soh, ian, sb, etc)"
    type: "harness"
    wait: "deployment/some-other-deployment" #optional wait
```

## Adding a new augmentation
As long as the augmentation is "standard", it only takes a few lines in the `values.yaml` file to define it.
1. Add the new augmentation name in the `values.yaml` file, `standardAugmentationApps` list.
2. Create a new key for the appValues further down in the `values.yaml` file. The key must match what was 
   added to `standardAugmentationApps`.
3. See above for the `example-augmentation` above for an example of the minimum required. Add additional
   fields depending on what the augmentation requires (volumes, network, env, etc). Other existing
   augmentations can be used as a reference.