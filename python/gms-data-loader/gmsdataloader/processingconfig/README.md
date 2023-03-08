# Processing Configuration Loader

## Purpose

To provide a library to load Processing Configuration from JSON files to PostgreSQL to be used by the frameworks configuration service

## Design and usage

The processing_config loader is meant to be used in one of 2 ways, as a python script run through the gmsctl container or as a docker image run from the gmsctl shell script

1) gmsctl container
The file `processing_config_loader.py` is meant to be used as a library by other programs in order to load processing configuration into PostgreSQL.
It is expected for users to instantiate their own instance of `ProcessingConfigLoader` and call `load()` to publish data from JSON to the frameworks configuration service.
The arguments are: 
    Optional:
        processing-configuration-root: root directory of processing configuraiton
            defaults to dataloaders/processing_config/processing-configuration-root
    Required:
        url: url of frameworks configuration service
example:
python3 gms_data_loader.py load-processing-config --url=http://127.0.0.1:8080 

2) gmsctl bash script
The image gms-data-loader can be used to upload processing configuration after the system is up.  This allows users to change configuration without requiring recompilation.
When the docker container is run by gmsctl, the specified directory is volume mounted and the correct arguments passed to the gms-data-loader image.
The arguments are:
    Required:
        processing-configuration-root: root directory of processing configuraiton
        deployment-name: The deployment name to be updated
            gmsctl will determine the correct url to pass to the gms-data-loader image.
example (run by gmsctl):
docker run -v <local-path>:/gms-data-loader/config/processing gms-data-loader load-processing-config --url=http://127.0.0.1:8080

If the loader fails to process the JSON data or has an issue with the connection to the frameworks configuraiton service, data will not be uploaded.