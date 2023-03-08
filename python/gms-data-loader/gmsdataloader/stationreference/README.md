# Station Reference Loader

## Purpose

To provide a library to load Station Reference Information from JSON files to the OSD

## Design and usage

The file `station_reference_loader.py` is meant to be used as a library by other programs in order to load processing data into the OSD.

It is expected for users to instantiate their own instance of `StationReferenceLoader` and call `load()` to publish data from JSON to the OSD.

Publishing of station reference information follows this dependency hierarchy:
1. Reference Objects (i.e. Networks, Stations, Sites, Channels, Sensors)
2. Reference Memberships (i.e. Network Memberships, Station Memberships, Site Memberships)

If any reference objects (e.g. Networks, Channels) within the first step fails, none of the reference
memberships are loaded

However, since actions within each step are run concurrently, it is possible for one or more actions in the
same step to fail while the rest in the step succeed