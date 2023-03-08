# GMS Data Loader

## Purpose

To provide a high-level script to load data into the OSD. 

The supported data to upload includes station processing data (e.g. Station Groups), station reference data (e.g. Reference Networks, Reference Stations, etc.) and processing configuration data.

##Design and usage

The `gms_data_loader.py` script works via a command-line interface, with a set of subcommands for each type of data loader.

* Calling the script with the `load-station-reference` or `lsr` command will allow the user to load station reference information to configured OSD endpoints.
    * `-u, --url`: Base URL to publish data to
    * `-c, --config`: [optional] Path to loader configuration file ([Python INI](https://docs.python.org/3.8/library/configparser.html#supported-ini-file-structure))
        * `[routes]` Routes to publish data to
            * `ref_networks`
            * `ref_stations`
            * `ref_sites`
            * `ref_chans`
            * `ref_sensors`
            * `ref_network_memberships`
            * `ref_station_memberships`
            * `ref_site_memberships`
        * If not supplied, then gms-common/python/gms-data-loader/dataloaders/stationreference/resources/config/config.ini will be used.

    * `--ref_networks`: [optional] Path to Reference Networks Array (JSON)
        * If not supplied, then gms-common/python/gms-data-loader/stationdata/reference-network.json will be used.
    * `--ref_stations`: [optional] Path to Reference Stations Array (JSON)
        * If not supplied, then gms-common/python/gms-data-loader/stationdata/reference-station.json will be used.
    * `--ref_sites`: [optional] Path to Reference Sites Array (JSON)
        * If not supplied, then gms-common/python/gms-data-loader/stationdata/reference-site.json will be used.
    * `--ref_chans`: [optional] Path to Reference Channels Array (JSON)
        * If not supplied, then gms-common/python/gms-data-loader/stationdata/reference-channel.json will be used.
    * `--ref_sensors`: [optional] Path to Reference Sensors Array (JSON)
        * If not supplied, then gms-common/python/gms-data-loader/stationdata/reference-sensor.json will be used.
    * `--ref_net_memberships`: [optional] Path to Reference Network Memberships Array (JSON)
        * If not supplied, then gms-common/python/gms-data-loader/stationdata/reference-network-memberships.json will be used.
    * `--ref_sta_memberships`: [optional] Path to Reference Station Memberships Array (JSON)
        * If not supplied, then gms-common/python/gms-data-loader/stationdata/reference-station-memberships.json will be used.
    * `--ref_site_memberships`: [optional] Path to Reference Site Memberships Array (JSON)
        * If not supplied, then gms-common/python/gms-data-loader/stationdata/reference-site-memberships.json will be used.
    
* Calling the script with the `load-station-processing` or `lsp` command will allow the user to load station processing information to configured OSD endpoints.
    * `-u, --url`: Base URL to publish data to
    * `-c, --config`: [optional] Path to loader configuration file ([Python INI](https://docs.python.org/3.8/library/configparser.html#supported-ini-file-structure))
        * `[routes]` Routes to publish data to
            * `station_groups_new`
            * `station_groups_update`
        * If not supplied, then gms-common/python/gms-data-loader/dataloaders/stationprocessing/resources/config/config.ini will be used.
    * `--station_groups`: [optional] Path to Station Groups Array (JSON)
        * If not supplied, then gms-common/python/gms-data-loader/stationdata/processing-station-group.json will be used.
    
* Calling the script with the `load-user-preferences` or `lup` command will allow the user to load user preferences to the OSD.
    * `-u, --url`: URL to publish data to
    * `-c, --config`: [optional] Path to loader configuration file ([Python INI](https://docs.python.org/3.8/library/configparser.html#supported-ini-file-structure))
        * `[routes]` Routes to publish data to
            * `user_preferences`
        * If not supplied, then gms-common/python/gms-data-loader/dataloaders/userpreferences/resources/config/config.ini will be used.
    * `-f, --file`: [optional] Specify an alternate user preferences file (JSON)
        * If not supplied, then gms-common/python/gms-data-loader/resources/defaultUserPreferences.json will be used.
    
* Calling the script with the `load-processing-config` or `lpc` command will allow the user to load processing configuration to the OSD.
    * `-u, --url`: URL of the processing-configuration-service which will store the processing configuration in the database
    * `--processing-configuration-root`: [optional] Root directory of the location of Processing Configuration sub-directories.
        * If not supplied, then /gms-common/config/processing will be used os the root directory. 
        
* Calling the script with the `update-station-groups` or `usg` command will allow the user to load station group definitions to configured OSD endpoint, which will update the Station Groups in the OSD.
    * `-u, --url`: Base URL to publish data to
    * `-f, --file`: Path to Station Group Definition Array (JSON)
    * `-c, --config`: [optional] Path to loader configuration file ([Python INI](https://docs.python.org/3.8/library/configparser.html#supported-ini-file-structure))
        * `[routes]` Routes to publish data to
            * `station_groups_new`
            * `station_groups_update`
        * If not supplied, then gms-common/python/gms-data-loader/dataloaders/stationprocessing/resources/config/config.ini will be used.

## Testing

This package utilizes `unittest` to run its unit and component integration tests for the data loading libraries. All commands are assumed to be ran from the top-level `gms-data-loader` directory

To only run the unit tests, the command is `python3 -m unittest discover -s gmsdataloader/`

To run integration tests only, the command is `DOCKER_IMAGE_TAG=develop python3 -m unittest discover -s integration/`. The `DOCKER_IMAGE_TAG` must be provided to ensure that the correct images are pulled from artifactory for the component integration tests.

To run all tests, the command is `DOCKER_IMAGE_TAG=develop python3 -m unittest discover`. 