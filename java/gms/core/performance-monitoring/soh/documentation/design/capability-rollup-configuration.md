# Capability rollup configuration mechanism

## Requirements of configuration

The configuration mechanism for capability rollups is designed to meet these requirements:

1. Configuration for station groups can be specified. Each station group must be configured with
a rollup operator that combines the state of health of the stations in the station group.

2. Configuration for stations can be specified. Each station must be configured with
a rollup operator that combines the state of health of the channels in the station.

3. Configuration for channels can be specified. Each channel must be configured with a 
rollup operator that combines the state of health of all the Soh monitors in the channel.

4. Rollup operators for station groups must have stationOperands specified. Rollup operators
for stations must have channelOperands specified. Rollup operators for channels must have sohMonitorTypeOperands
specified.

5. Rollup operators are allowed to have ALL of their operand fields missing. For rollup operators for stations,
the field will be filled in with all of the stations of the station group. Similarly for channel operators and
SohMonitorType operators.

Additionally, a parameter `rollupStationSohTimeTolerance` is part of the overall station SOH configuration. it
specifies how far back in time to reach when selecting `StationSoh` objects to include in the capability rollup
for a given station group.

## Definition objects

The config mechanism for capability rollups will produce a set of `CapabilitySohRollupDefinition` objects
that get passed to the overall SOH configuration mechanism.

`StationSohMonitoringDefinition` contains a `Set` of `CapabilitySohRollupDefinition`s that `SohControl` 
will load at startup.

Each `CapabilitySohRollupDefinition` contains the name of the station group that the capability rollup
is for, the `RollupOperator` used to combine the rolled-up station soh statuses, and a map of
station name to a `StationRollupDefinition` that gets applied to the respective station.

Each `StationRollupDefinition` contains a map of channel name to a `ChannelRollupDefinition` that gets
applied to the respective channel, as well as another `RollupOperator` thats gets applied
to all of the rolled-up channel soh statuses.

Finally, each `ChannelRollupDefinition` contains a single `RollupOperator` that gets applied to the
set of Soh statuses that are found in the map of `SohMonitorType` to `SohStatus` inside `ChannelSoh`.

# Option objects

The configuration mechanism contains  intermediate "Option" classes that facilitate the needs and
requirements of how the system casn be configured by the user. Each one only contains a `RollupOperator`
The configuration files get deserialized into these Option files before they are transformed into Definition
files.

# CapabilityRollupConfigurationUtility

The `CapabilityRollupConfigurationUtility` class is an instatiable utility that houses the functionality of
producing `CapabilitySohRollupDefinition` objects. It is instantiated with a `ConfigurationConsumerUtility`
object, a `Collection` of `StationGroup`, and the configuration keys needed to find specific configuration
files for station group, station, and channel.

To construct a set of `CapabilitySohRollupDefinition`, the following work flow occurs:

#### For each station: 

1. For each channel in the station, resolve the set of `ChannelRollupConfigurationOption`. This is done by using the `ConfigurationConsumerUtility` 
to resolve a raw `Map<String, Object>>`, and convert it to a `ChannelRollupConfigurationOption`. This is needed because the `RollupOperator`
is allowed to have missing fields, thus cannot be directly deserialized.

2. Compile the set of `ChannelRollupConfigurationOption`; resolve the rollup operator for the channels, and construct a `StationRollupConfigurationOption`


### For each station group:

1.  Compile the set of `StationRollupConfigurationOption`; resolve the rollup operator for the stations, and construct a 
`CapabilitySohRollupConfigurationOption`

All of these Option classes are then transformed into Definition classes.

# RollupOperator resolution

`RollupOperators` are resolved from the raw `Map<String, Object>>` objects that are returned from `ConfigurationConsumerUtility` when no `Class` is specified
for the `resolve` method. This is to allow the user to avoid having to specify operand lists when the intent is to include all possible operands,
and to avoid having to specify empty operand lists for operands that are not of the right type for the operator (operators that operate on stations,
for example, should not have operands for channels or SohMonitorTypes.)

`RollupOperatorConfigurationUtility` encapsulates the complexities of resolving `RollupOperators`.

To resolve `RollupOperator`s, which can have more `RollupOperator`s nested inside them, `RollupOperatorConfigurationUtility` splits the problem of resolution
into to cases:

1. Terminal operators. These are `RollupOperator`s that have no nested `RollupOperator`s. These get resolved immediately.
2. Non-Terminal operators. These are `RollupOperator`s thqt have nested `RollupOperator`s. The algorithm does a "depth first search" until it finds
terminal operators, resolves them, then uses them to construct the non-terminal operators.


# Examples of `Option` objects and corresponding configuration 

## CapabilitySohRollupConfigurationOption

Located under `gms-common/config/processing/soh-control.station-group-capability-rollup`

**Example: "Large" station groups**

This file is an example configuration that applies to station groups,
which have been deemed "large" because they contain aproximately 20 stations:

```json
{
  "name": "large-capability-rollup",
  "constraints": [
    {
      "constraintType": "STRING",
      "criterion": "StationGroupName",
      "operator": {
        "type": "IN",
        "negated": false
      },
      "value": [
        "All_1",
        "All_2",
        "CD1.1"
      ]
    }
  ],
  "parameters": {
    "stationsToGroupRollupOperator": {
      "operatorType": "MIN_GOOD_OF",
      "goodThreshold": 17,
      "marginalThreshold": 14
    }
  }
}
```
The operator type "IN" says apply this setting to all of the station groups in the "value"
field. Thus the station groups All_1, All_2, and CD1.1 will have a `MIN_GOOD_OF` operator,
where the `goodThreshold` is 17, and the `marginalThreshold` is 14. No operands are
specified, so ALL stations in station group will be operated on.

The `stationOperands` field is missing from `stationsToGroupRollupOperator` to signify we want all
stations in the group.

## StationRollupConfigurationOption

Located under `gms-common/config/processing/soh-control.station-capability-rollup`

**Example: Array stations**
This file is the configuration for what has been deemed "Array" stations, stations with a large
number of channels.

```json
{
  "name": "large-array-capability-rollup",
  "constraints": [
    {
      "constraintType": "STRING",
      "criterion": "StationName",
      "operator": {
        "type": "IN",
        "negated": false
      },
      "value": [
        "AKASG",
        "ASAR",
        "CMAR",
        "FINES",
        "ILAR",
        "KSRS",
        "NOA",
        "SPITS",
        "TORD",
        "WRA"
      ]
    }
  ],
  "parameters": {
    "channelsToStationRollupOperator": {
      "operatorType": "MIN_GOOD_OF",
      "marginalThreshold": 8,
      "goodThreshold": 16
    }
  }
}
```
The operator type "IN" says apply this setting to all of the stations in the "value"
field.

The `channelOperands` field is missing from `channelsToStationRollupOperator` to signify we want all
channels in the station.

Another example of configuration for station, this time also determined by the station 
group that the statio belongs to:

```json
{
  "name": "zalv-i_to_z-capability-rollup",
  "constraints": [
    {
      "constraintType": "STRING",
      "criterion": "StationName",
      "operator": {
        "type": "EQ",
        "negated": false
      },
      "value": [
        "ZALV"
      ]
    },
    {
      "constraintType": "STRING",
      "criterion": "StationGroupName",
      "operator": {
        "type": "EQ",
        "negated": false
      },
      "value": [
        "I_To_Z"
      ]
    }
  ],
  "parameters": {
    "channelsToStationRollupOperator": {
      "operatorType": "WORST_OF"
    }
  }
}
```

This says that for station group "I_To_Z", we want station ZALV to have a WORST_OF operator
that has all of its stations as operands.

## ChannelRollupConfigurationOption

Located under `gms-common/config/processing/soh-control.channel-capability-rollup`

**Example: Default configuration for channels**
```json
{
  "name": "default-channel-rollup",
  "constraints": [
    {
      "constraintType": "DEFAULT"
    }
  ],
  "parameters": {
    "sohMonitorsToChannelRollupOperator": {
      "operatorType": "BEST_OF",
      "sohMonitorTypeOperands": [
        "MISSING",
        "LAG"
      ]
    }
  }
}
```

This configuration gets applied to all channels that are not applicable to an other configuration.
Currently, this is the only configuration file for channels, meaning this configuration gets applied
to ALL channels.

In this case we are specifying the operands to the operator via `sohMonitorTypeOperands` under `sohMonitorsToChannelRollupOperator`.
Thus the calculation will consider only LAG and MISSING SohMonitorValueAndStatus types inside ChannelSoh.



                