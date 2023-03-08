# Station Processing Loader

## Purpose

To provide a library to load Station Processing data from JSON files to the OSD

## Design and usage

The file `station_processing_loader.py` is meant to be used as a library by other programs in order to load processing data into the OSD.

It is expected for users to instantiate their own instance of `StationProcessingLoader` and call `load()` to publish data from JSON to the OSD.

Additional features include:
* Ability to load station group definitions into  the OSD for Station Group updates via `load_sta_group_updates()`

If the loader fails to process the JSON data or has an issue with the connection to the OSD, data will not be uploaded.