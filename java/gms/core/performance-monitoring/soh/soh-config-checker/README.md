# Configuration checker

The configuration checker is a command-line tool for running the station-soh-monitoring
configuration mechanism on a given set of configuration files. The utility 
invokes the configuration mechanism with a `FileConfigurationRepository` built
with the supplied configuration directory, and an `StationGroupRepositoryInterface` facade that,
when `receiveStationGroups` is called, connects to the supplied OSD service host
and filters out stations and station groups based on the provided filters.

By default, if the configuration checks out, an output similar to this is displayed:

```
Loading configuration...
Configuration successfully loaded! 

Station SOH definitions: 1 
Channel SOH definitions: 3 
Channels by monitor type entries: 19 
Station monitor types for rollup: 19 
Channel monitor types for rollup: 57 
Entries of monitor type -> soh status: 57 
Capability rollup definitions: 5 
Station rollup definitions: 5 
Channel rollup definitions: 15 
```

Otherwise, the config mechanism will throw some exception that can be analyzed.

## Command line arguments

The command line has the following options:
```
 -d (--configDir) VAL          : Directory containing configuration files
 -g (--stationGroups) STRING[] : Use only the listed station groups
 -j (--json)                   : Print serialized JSON of StationSohMonitoringDe
                                 finition (default: false)
 -jx (-xj, --jsonDump)         : Combine -x and -j (only print json) (default:
                                 false)
 -o (--osdHostName) VAL        : Hostname of the OSD repository
 -s (--stations) STRING[]      : Use only the listed stations. All station
                                 groups will only have these stations. Station
                                 groups without the stations are filtered out.
 -x                            : Suppress config info output (default: false)
 ```
 
 `-o` specifices the osd service host name. The osd service is used to retrieve
 station groups.
 
 `-d` specifies which directory houses the configuration files.
 
 ## Trimming the configuration
 
 The `StationGroupRepositoryInterface` that is created can be tweaked to return only the
 specified station groups, and/or only the specified stations. This is done via the 
 `-g` and `-s` options, respectively.
 
 When only `-g` is specified, the `StationGroupRepositoryInterface` will return only the 
 station groups listed. This list will be passed to the configuration mechanism,
 which will build configuration assuming only the provided station groups exist.
 
 When only `-s` is specified, the `StationGroupRepositoryInterface` will return only those
 station groups that contain at least one of the provided stations. Further, each
 returned station group will have at most the stations specified. None of the
 returned station groups will have any other stations. Thus the config mechanism
 will operate as if only the provided stations, and the station groups that contain
 them, exist.
 
 The `-g` and `-s` options allow the user to trim down the returned configuration.
 Useful if one is only interested in a single station or station group.
 
 ## JSON output
 
 The tool can serialize the `StationSohMonitoringDefinition` object that is 
 generated. The `-j` or `-xj` option will print the json to stdout. (`-xj` excludes
 the information output). This is another place where trimming the configuration
 can be useful - to avoid printing out a gigantic JSON string that represents
 the full configuration.
 
 ## Running via Gradle
 
 The quickest way to use the tool after it has been built is via `gradle run`:
 
 From the project directory for the configuration checker (`directory-containing-gms/gms-common/java/gms/core/performance-monitoring/soh/soh-config-checker`)
 
 ```
 gradle run --args="-d directory-containing-gms/gms-common/config/processing/ -o osd.repository.host.name"
 
 > Task :soh-config-checker:run
Loading configuration...
Configuration successfully loaded! 

Station SOH definitions: 99 
Channel SOH definitions: 914 
Channels by monitor type entries: 1881 
Station monitor types for rollup: 294 
Channel monitor types for rollup: 17315 
Entries of monitor type -> soh status: 17366 
Capability rollup definitions: 6 
Station rollup definitions: 594 
Channel rollup definitions: 542916 
 ```
In this case, neither `-g` nor `-s` was specified, so all configuration for all stations
and station groups was analyzed.

Another example with `-s`:
 
```
gradle run --args="-s KDAK -d directory-containing-gms/gms-common/config/processing/ -o osd.repository.host.name"

> Task :soh-config-checker:run
Loading configuration...
Configuration successfully loaded! 

Station SOH definitions: 1 
Channel SOH definitions: 3 
Channels by monitor type entries: 19 
Station monitor types for rollup: 19 
Channel monitor types for rollup: 57 
Entries of monitor type -> soh status: 57 
Capability rollup definitions: 5 
Station rollup definitions: 5 
Channel rollup definitions: 15 

 ```
 
This creates and analyzes configuration for station KDAK for any station group. Note how much
less configuration there is for just one station.

Another example, with both `-s` and `-g`:

```
gradle run --args="-s KDAK -g All_1 I_To_Z -d directory-containing-gms/gms-common/config/processing/ -o osd.repository.host.name"

> Task :soh-config-checker:run
Loading configuration...
Configuration successfully loaded! 

Station SOH definitions: 1 
Channel SOH definitions: 3 
Channels by monitor type entries: 19 
Station monitor types for rollup: 3 
Channel monitor types for rollup: 57 
Entries of monitor type -> soh status: 57 
Capability rollup definitions: 2 
Station rollup definitions: 2 
Channel rollup definitions: 6 
```

This creates and analyzes configuration for station KDAK, but only for station groups
All_1 and I_To_Z

## Running without gradle

The command line tool can be "installed" as a standalone program. Under the directory
containing the config checker (`directory-containing-gms/gms-common/java/gms/core/performance-monitoring/soh/soh-config-checker`)
there is a `build` directory, which contains another directory `distributions`. This
directory contains a tar file `soh-config-checker-<GMS-VERSION>.tar` which can be untarred
to another directory (if desired).

```
$ cp soh-config-checker-<GMS-VERSION>.tar <desired-directory>
$ cd <desired-directory>
$ tar -xf soh-config-checker-<GMS-VERSION>.tar
$ cd <desired-directory/soh-config-checker-<GMS-VERSION>/bin
$ ./soh-config-checker -s KDAK -d directory-containing-gms/gms-common/config/processing/ -o osd.repository.host.name

Loading configuration...
Configuration successfully loaded! 

Station SOH definitions: 1 
Channel SOH definitions: 3 
Channels by monitor type entries: 19 
Station monitor types for rollup: 19 
Channel monitor types for rollup: 57 
Entries of monitor type -> soh status: 57 
Capability rollup definitions: 5 
Station rollup definitions: 5 
Channel rollup definitions: 15 
```

