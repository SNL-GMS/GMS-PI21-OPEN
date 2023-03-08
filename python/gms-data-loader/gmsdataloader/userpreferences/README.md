# User Preferences Loader

## Purpose

To provide a library to load User Preferences from JSON files to the OSD

## Design and usage

The file `user_preferences_loader.py` is meant to be used as a library by other programs in order to load user preferences data into the OSD.

It is expected for users to instantiate their own instance of `UserPreferencesLoader` and call `load()` to publish data from JSON to the OSD.

If the loader fails to process the JSON data or has an issue with the connection to the OSD, data will not be uploaded.