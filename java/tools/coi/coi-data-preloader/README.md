# SOH Data Generator and PreLoader

#### Wiki

### Notes

1. It currently takes about 24 hours for 30 days of Analog and Boolean ACEI, Capability Rollups, RSDFs, and Station SOH to load. Please keep in mind that this loads over 90 million records into the database.
2. By default, data is generated for the past 30 days. Please change `DAYS_TO_LOAD` to change the total generation duration.
3. This solution is currently a multiple step process: install a deployment, apply the pre-loader job, delete the pre-loader job, optionally scale up `soh-control` and/or any other services originally scaled down, optionally update the time to live on the deployment, optionally start an injector.
4. Environment variables, update in `data-preloader.yaml`:
      * `STATION_GROUP`: the station group the preloader will generate data for
      * `DAYS_TO_LOAD`: the number of days the preloader will generate data for
      * `DAYS_AGO_TO_START`: the number of days ago the preloader will use as the start date for the generated data
      * `LOAD_RSDFS`: the rsdf load will run if this is set to "TRUE"
      * `RECEPTION_DELAY`: the reception delay to be used when generating rsdfs
      * `RSDF_SAMPLE_DURATION`: the sample duration to be used when generating rsdfs
      * `LOAD_STATION_SOHS`: the station soh load will run if this is set to "TRUE"
      * `STATION_SOH_SAMPLE_DURATION`: the sample duration to be used when generating station sohs
      * `LOAD_ANALOG_ACEIS`: the analog acei load will run if this is set to "TRUE"
      * `ACEI_ANALOG_SAMPLE_DURATION`: the sample duration to be used when generating analog aceis
      * `LOAD_BOOLEAN_ACEIS`: the boolean acei load will run if this is set to "TRUE"
      * `ACEI_BOOLEAN_SAMPLE_DURATION`: the sample duration to be used when generating boolean aceis
      * `LOAD_ROLLUPS`: the rollup load will run if this is set to "TRUE"
      * `ROLLUP_SAMPLE_DURATION`: the sample duration to be used when generating rollups
      * `DURATION_INCREMENT`: smallest amount for which statuses will be generated
      * `BOOLEAN_INITIAL_STATUS`: initial status e.g. start with `FALSE` or `TRUE`
      * `MEAN_OCCURRENCES_PER_YEAR`: mean occurrences per year
      * `MEAN_HOURS_OF_PERSISTENCE`: mean time the status will continue to be that status
      * `DURATION_ANALOG_STATUS_MIN`: duration analog status minimum
      * `DURATION_ANALOG_STATUS_MAX`: duration analog status maximum
      * `DURATION_BETA0`: constant auto-regression term 
      * `DURATION_BETA1`: first order auto-regression term
      * `DURATION_STDERR`: standard deviation of the Gaussian noise
      * `DURATION_ANALOG_INITIAL_VALUE`: duration analog initial value, initial value of time series 
      * `PERCENT_ANALOG_STATUS_MIN`: percent analog status minimum
      * `PERCENT_ANALOG_STATUS_MAX`: percent analog status maximum
      * `PERCENT_BETA0`: constant auto-regression term 
      * `PERCENT_BETA1`: first order auto-regression term
      * `PERCENT_STDERR`: standard deviation of the Gaussian noise
      * `PERCENT_ANALOG_INITIAL_VALUE`: percent analog initial value, initial value of time series 
      * `USE_CURATED_DATA_GENERATION`: whether or not to use the time series data generation vs the default volume data generation which will generate duplicate data with only shifted time until the total generation time has been reached
      * `JAVA_OPTS: -Dreactor.schedulers.defaultPoolSize=`: places an upper limit on the number of threads that will be used by the preloader 
5. On deploy, the `soh-control` service should be scaled down. Please scale up any time after the data load completes. 
6. On deploy, please update the time to live for all data types to be something greater than `DAYS_AGO_TO_START` plus time to run the pre-loader e.g. 768 hours (32 days). See the `Running` section below for an example.
7. Copy and modify `data-preloader.yaml`, which is co-located with this `README`, as necessary. Please submit any updates with a merge request to Chillas and Platform. 

### Running
1. `kubeconfig` for the desired testbed, e.g. `kubeconfig pilot`. 
2. Start a deployment, for example:
   ```shell
   # soh-control scaled down
   # updated time to live to 768 hours (32 days)
   gmskube install --tag <branch> --type <type> <namespace> --set soh-control.replicas=0 --set frameworks-osd-ttl-worker.env.ACEI_TTL_IN_HOURS=768 --set frameworks-osd-ttl-worker.env.RSDF_TTL_IN_HOURS=768 --set frameworks-osd-ttl-worker.env.SSOH_TTL_IN_HOURS=768
   ``` 
3. Replace `<branch>` and `<namespace>` in `data-preloader.yaml` with the `branch` and `namespace` used in the above deploy command. Make any additional updates to `data-preloader.yaml`. 
4. Start the pre-loader, `kubectl apply -f data-preloader.yaml`.
5. Wait for the pre-load to complete. You'll see `The script completed` at the end of the log.
6. Delete the job. Any manually started jobs, including the pre-loader, must be deleted before the deployment can be removed.
```shell
kubectl delete -f data-preloader.yaml
# OR
kubectl delete -n <namespace> job/data-preloader
```
Hopefully soon we'll be able to set time to live in the job specification. In the below example, the job will be automatically deleted after 60 seconds.
```yaml
spec:
  # Kubernetes v1.12 [alpha]
  ttlSecondsAfterFinished: 60
```
6. Optionally update time to live and scale up any additional services needed, for example:
```shell
# scale up soh-control
# set TTL to 720 hours (30 days)
# start the injector
gmskube upgrade --set soh-control.replicas=1 --set frameworks-osd-ttl-worker.env.ACEI_TTL_IN_HOURS=720 --set frameworks-osd-ttl-worker.env.RSDF_TTL_IN_HOURS=720 --set frameworks-osd-ttl-worker.env.SSOH_TTL_IN_HOURS=720 --tag <branch> --injector <namespace>
```

### Helpful Commands
```shell
# start the pre-loader job
kubectl apply -f data-preloader.yaml
# get information about the pre-loader job
watch kubectl get pod,job -n <namespace>
# more information, logs
kubectl logs -n <namespace> job/data-preloader -f
# stop the pre-loader
kubectl delete -f data-preloader.yaml
# OR
kubectl delete -n <namespace> job/data-preloader
```
