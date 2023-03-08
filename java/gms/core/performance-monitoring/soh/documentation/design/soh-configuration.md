# CR 18851 - Restructure StationSohControl Configuration

`SohConfiguration` will resolve the following sets of configuration and combine them into a singular
`StationSohMonitoringDefinition` that `SohControl` will load at startup.

## SohControlConfigurationOption

Located under `gms-common/config/processing/soh-control`

**Default**
```json
{
  "name": "soh-control-default",
  "constraints": [
    {
      "constraintType": "DEFAULT"
    }
  ],
  "parameters": {
    "reprocessingPeriod": "PT20S"
  }
}
```

## SohMonitorStatusThresholdDefinition

Located under `gms-common/config/processing/soh-control.soh-monitor-thresholds`

It is not possible to have an overall default that applies to any monitor type,
because the threshold types are different depending on the monitor type. For
example, MISSING has floating point thresholds (representing percentages), but
LAG has DURATION thresholds.

**Default per `MonitorType`**
```json
{
  "name": "LAG",
  "constraints": [
    {
      "constraintType": "STRING",
      "criterion": "MonitorType",
      "operator": {
        "type": "EQ",
        "negated": false
      },
      "value": [
        "LAG"
      ]
    },
    {
      "constraintType": "DEFAULT"
    }
  ],
  "parameters": {
    "goodThreshold": "PT2M",
    "marginalThreshold": "PT5M"
  }
}
```

**Overrides per Station, `MonitorType`**
```json
{
  "name": "missing-AAK",
  "constraints": [
    {
      "constraintType": "STRING",
      "criterion": "MonitorType",
      "operator": {
        "type": "EQ",
        "negated": false
      },
      "value": [
        "MISSING"
      ]
    },
    {
      "constraintType": "STRING",
      "criterion": "StationName",
      "operator": {
        "type": "EQ",
        "negated": false
      },
      "value": [
        "AAK"
      ]
    }
  ],
  "parameters": {
    "goodThreshold": 80,
    "marginalThreshold": 90
  }
}
```

**Overrides per Station, Channel, `MonitorType`**
```json
{
  "name": "lag-AFI-AFI-BHZ",
  "constraints": [
    {
      "constraintType": "STRING",
      "criterion": "MonitorType",
      "operator": {
        "type": "EQ",
        "negated": false
      },
      "value": [
        "LAG"
      ]
    },
    {
      "constraintType": "STRING",
      "criterion": "StationName",
      "operator": {
        "type": "EQ",
        "negated": false
      },
      "value": [
        "AFI"
      ]
    },
    {
      "constraintType": "STRING",
      "criterion": "ChannelName",
      "operator": {
        "type": "EQ",
        "negated": false
      },
      "value": [
        "AFI.AFI.BHZ"
      ]
    }
  ],
  "parameters": {
    "goodThreshold": "PT110S",
    "marginalThreshold": "PT275S"
  }
}
```

## TimeWindowDefinition

Located under `gms-common/config/processing/soh-control.soh-monitor-timewindows`

**Default**
```json
{
  "name": "default-timewindows",
  "constraints": [
    {
      "constraintType": "DEFAULT"
    }
  ],
  "parameters": {
    "calculationInterval": "PT10M",
    "backOffDuration": "PT5M"
  }
}
```

**Overrides per station, with two overrides in one file**

Notice that the two overrides are in a JSON array.


```json
[
  {
    "name": "SEISMIC_AUX",
    "constraints": [
      {
        "constraintType": "STRING",
        "criterion": "StationName",
        "operator": {
          "type": "IN",
          "negated": false
        },
        "value": [
          "AAK",
          "AFI",
          "AKTO",
          "ANMO"
        ]
      }
    ],
    "parameters": {
      "backOffDuration": "PT30M",
      "calculationInterval": "PT30M"
    }
  },
  {
    "name": "SEISMIC_AUX_DELAY",
    "constraints": [
      {
        "constraintType": "STRING",
        "criterion": "StationName",
        "operator": {
          "type": "IN",
          "negated": false
        },
        "value": [
          "TIXI",
          "TLY",
          "URZ",
          "VRAC",
          "YAK"
        ]
      }
    ],
    "parameters": {
      "calculationInterval": "PT60M",
      "backOffDuration": "PT90M"
    }
  }
]
```

## StationGroupNamesConfigurationOption

Located under `gms-common/config/processing/soh-control.station-group-names`

**Default**
```json
{
  "name": "station-group-names-default",
  "constraints": [
    {
      "constraintType": "DEFAULT"
    }
  ],
  "parameters": {
    "stationGroupNames": [
      "All_1",
      "All_2",
      "EurAsia",
      "OthCont",
      "CD1.1",
      "Wrapped",
      "A_To_H",
      "I_To_Z"
    ]
  }
}
```

## SohMonitorTypesForRollupConfigurationOption

Located under  `gms-common/config/processing/soh-control.soh-monitor-types-for-rollup-station` and `gms-common/config/processing/soh-control.soh-monitor-types-for-rollup-channel` for station and channel rollups respectively

**Overrides per Station**
```json
{
  "name": "soh-monitor-types-for-rollup-station-AAK",
  "constraints": [
    {
      "constraintType": "STRING",
      "criterion": "StationName",
      "operator": {
        "type": "EQ",
        "negated": false
      },
      "value": [
        "AAK"
      ]
    }
  ],
  "parameters": {
    "sohMonitorTypesForRollup": [
      "MISSING",
      "LAG"
    ]
  }
}
```

**Overrides per Station and Channel**
```json
{
  "name": "soh-monitor-types-for-rollup-channel-AKTO-AKTO-BHZ",
  "constraints": [
    {
      "constraintType": "STRING",
      "criterion": "StationName",
      "operator": {
        "type": "EQ",
        "negated": false
      },
      "value": [
        "AKTO"
      ]
    },
    {
      "constraintType": "STRING",
      "criterion": "ChannelName",
      "operator": {
        "type": "EQ",
        "negated": false
      },
      "value": [
        "AKTO-AKTO-BHZ"
      ]
    }
  ],
  "parameters": {
    "sohMonitorTypesForRollup": [
      "MISSING",
      "LAG",
      "ENV_CLIPPED"
    ]
  }
}
```

## ChannelsByMonitorTypeConfigurationOption

Located at `gms-common/config/processing/soh-control.channels-by-monitor-type`.

**Overrides per Station**
```json
{
  "name": "channels-by-monitor-type-AKASG",
  "constraints": [
    {
      "constraintType": "STRING",
      "criterion": "StationName",
      "operator": {
        "type": "EQ",
        "negated": false
      },
      "value": [
        "AKASG"
      ]
    }
  ],
  "parameters": {
    "LAG": [
      "AKASG.AK01.BHZ",
      "AKASG.AK02.BHZ"
    ],
    "MISSING": [
      "AKASG.AK01.BHZ"
    ]
  }
}
```


## Workflow for Constructing StationSohMonitoringDefinition
```
I. Resolve StationSohConfigurationOption using ConfigurationConsumerUtility.

II. Query OSD for Stations in the StationGroups specified in StationSohConfigurationOption.

III. Loop through Stations to create StationSohDefinitions for each Station

   A. Get entry for the current Station from StationSohConfigurationOption

   B. Populate the stationName

   C. Populate the sohMonitorTypesForRollup list in StationSohDefinition

       i. Resolve a MonitorTypesInRollupConfigurationOption, providing the Station as a selector.  If
          a configuration option exists for that Station, use it to populate sohMonitorTypesForRollup.

      ii. If no MonitorTypesInRollupConfigurationOption exists for the current Station, set
          sohMonitorTypesForRollup to contain all MonitorTypes.

   D. Populate the channelsByMonitorType map in StationSohDefinition

      i. Resolve a ChannelsByMonitorTypeConfigurationOption, providing the Station as a selector.
         If a configuration option exists for that station, use it. For every MonitorType key not 
         specified in that configuration option, the value for that MonitorType is a list of all 
         Channels for that Station.

      ii. If there is no ChannelsByMonitorTypeConfigurationOption for that Station, create a map 
          containing entries that map every MonitorType to all Channels in the current station.
          
   E.  Populate the timeWindowBySohMonitorType map in StationSohDefinition
      
      i. Resolve a TimeWindowDefinition object, providing station and monitor type.
      
      ii. Associate the TimeWindowDefinition object with the monitor type.


   F. Loop through Channels in the current Station

       Populate ChannelSohDefinitions in StationSohDefinition

          i. Populate channelName with the current Channel's name

          ii. Populate the sohMonitorTypesForRollup list in ChannelSohDefinition

             i. Resolve a SohMonitorTypesForRollupConfigurationOption, providing the Channel as a 
                selector.  If a configuration option exists for that Station and Channel, use it
               a. Resolve a SohMonitorTypesForRollupConfigurationOption, providing the Station and Channel
                  as selectors.  If a configuration option exists for that Station and Channel, use it
                  to populate sohMonitorTypesForRollup.

               b. If no MonitorTypesInRollupConfigurationOption exists for the specified Station and
                  Channel, set sohMonitorTypesForRollup to contain all MonitorTypes.

          iii. Populate sohMonitorStatusThresholdDefinitionsBySohMonitorType by resolving
               SohMonitorStatusThresholdDefinition, providing the current MonitorType, 
               Station, and Channel as configuration Selectors.
```
