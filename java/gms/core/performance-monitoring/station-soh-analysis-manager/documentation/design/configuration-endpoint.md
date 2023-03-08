# Configuration Endpoint
Configuration endpoint provide configuration for the UI
##Endpoint information

```  
     @Consumes(ContentType.JSON_NAME)
     @Path("retrieve-station-soh-monitoring-ui-client-parameters")
     @POST
     @Operation(description = "Get the display parameters for SOH Monitoring data.")
     @Produces(ContentType.JSON_NAME) 
```

##Endpoint data products

### StationSohMonitoringDisplayParameters

|Field|Type|
|:----|:-----|
|reprocessingPeriod|Duration|
|displayedStationGroups|List\<String>|
|rollupStationSohTimeTolerance|Duration|
|stationSohDefinition|Set\<StationSohDefinition>|
### SsamStationSohControlConfiguration

|Field|Type|
|:----|:-----|
|redisplayPeriod|Duration|
|acknowledgementQuietDuration|Duration|
|availableQuietDurations|Set\<Duration>|
|sohStationStaleDuration|Long|
|sohHistoricalDurations|Set\<Duration>|

