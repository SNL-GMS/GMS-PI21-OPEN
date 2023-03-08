import os
import logging
import requests

from gmsdataloader.processingconfig import ProcessingConfigLoader
from gmsdataloader.stationprocessing import StationProcessingLoader
from gmsdataloader.stationprocessing import StationProcessingLoaderConfig
from gmsdataloader.stationreference import StationReferenceLoader
from gmsdataloader.stationreference import StationReferenceObjectPaths
from gmsdataloader.stationreference import StationReferenceMembershipPaths
from gmsdataloader.stationreference import StationReferenceLoaderConfig
from gmsdataloader.userpreferences import UserPreferencesLoader
from gmsdataloader.userpreferences import UserPreferencesLoaderConfig
from gmsdataloader.earthmodels import EarthModelsLoader
from gmsdataloader.earthmodels import EarthModelMinioConfig
from gmsdataloader.mediumvelocity import MediumVelocityLoader
from gmsdataloader.mediumvelocity import MediumVelocityMinioConfig
from gmsdataloader.ellipticity import DziewonskiGilbertLoader
from gmsdataloader.ellipticity import DziewonskiGilbertMinioConfig
from gmsdataloader import ConfigOverrideResolver

# Create logger
logger = logging.getLogger('gmsdataloader') # logger name must match config-loader for log capture


class GmsDataloader:

    def __init__(self, default_config_path: str, override_config_path: str) -> None:
        self.default_config_path  = default_config_path
        self.override_config_path = override_config_path
        self.resolver = ConfigOverrideResolver(default_config_path, override_config_path)

    def load_processing_config(self, processing_config_url: str) -> None:
        
        # processing configuration must be in a 'processing' subdirectory
        processing_config_path = os.path.join(self.default_config_path, "processing")
        processing_config_override_path = os.path.join(self.override_config_path, "processing") if self.override_config_path else None
            
        processing_config_loader = ProcessingConfigLoader(processing_config_url, processing_config_path, processing_config_override_path)
        if processing_config_loader.load() == False:
            raise RuntimeError("Failed to load processing configuration.")

    def load_station_reference_data(self, osd_url: str) -> None:
        station_reference_loader = StationReferenceLoader(StationReferenceLoaderConfig(osd_url))

        # locate station reference files
        ref_networks_path = self.resolver.path("station-reference/stationdata/reference-network.json")
        ref_stations_path = self.resolver.path("station-reference/stationdata/reference-station.json")
        ref_sites_path    = self.resolver.path("station-reference/stationdata/reference-site.json")
        ref_chans_path    = self.resolver.path("station-reference/stationdata/reference-channel.json")
        ref_sensors_path  = self.resolver.path("station-reference/stationdata/reference-sensor.json")
        
        station_reference_object_paths = StationReferenceObjectPaths(ref_networks_path=ref_networks_path,
                                                                     ref_stations_path=ref_stations_path,
                                                                     ref_sites_path=ref_sites_path,
                                                                     ref_chans_path=ref_chans_path,
                                                                     ref_sensors_path=ref_sensors_path)
        
        # locate station reference membership files
        ref_net_memb_path  = self.resolver.path("station-reference/stationdata/reference-network-memberships.json")
        ref_sta_memb_path  = self.resolver.path("station-reference/stationdata/reference-station-memberships.json")
        ref_site_memb_path = self.resolver.path("station-reference/stationdata/reference-site-memberships.json")
        
        station_reference_membership_paths = StationReferenceMembershipPaths(ref_net_memb_path=ref_net_memb_path,
                                                                             ref_sta_memb_path=ref_sta_memb_path,
                                                                             ref_site_memb_path=ref_site_memb_path)
        
        station_reference_loader.load(station_reference_object_paths, station_reference_membership_paths)

        
    def load_station_processing_data(self, osd_url: str) -> None:
        station_groups_path = self.resolver.path("station-reference/stationdata/processing-station-group.json")
        station_processing_loader = StationProcessingLoader(StationProcessingLoaderConfig(osd_url))
        station_processing_loader.load(station_groups_path=station_groups_path)
        

    def update_station_group_definitions(self, osd_url: str) -> None:
        # Note: this file is optional and may not be present.  It is just a no-op if this is not there.
        relative_station_group_definitions_path = "station-reference/definitions/processing-station-group-definition.json"
        try:
            station_group_definitions_path = self.resolver.path(relative_station_group_definitions_path)
            station_processing_loader = StationProcessingLoader(StationProcessingLoaderConfig(osd_url))
            station_processing_loader.load_sta_group_updates(station_group_definitions_path=station_group_definitions_path)
        except requests.HTTPError as ex:
            logger.error(f"Failed to update station groups based on '{relative_station_group_definitions_path}'. {ex}")
        except OSError as ex:
            # Not an error. Note to the log that no action needs to be taken.
            logger.info(f"No '{relative_station_group_definitions_path}' file present. No action to take.")


    def load_user_preferences(self, osd_url: str) -> None:
        user_preferences_file = self.resolver.path("user-preferences/defaultUserPreferences.json")
        user_preferences_loader_config = UserPreferencesLoaderConfig(osd_url)
        user_preferences_loader = UserPreferencesLoader(user_preferences_loader_config)
        user_preferences_loader.load_user_preferences(user_preferences_file)

    def load_earthmodel_data(self, minio_url: str, minio_bucket: str) -> None:
        earthmodels_config = EarthModelMinioConfig(minio_url, minio_bucket)
        processing_config_path = os.path.join(self.default_config_path, "earth-models")
        earth_models_loader = EarthModelsLoader(processing_config_path, earthmodels_config)
        earth_models_loader.load()

    def load_mediumvelocity_data(self, minio_url, minio_bucket):
        mediumvelocity_config = MediumVelocityMinioConfig(minio_url, minio_bucket)
        processing_config_path = os.path.join(self.default_config_path, "mediumvelocity")
        mediumvelocity_loader = MediumVelocityLoader(processing_config_path, mediumvelocity_config)
        mediumvelocity_loader.load()

    def load_dziewonski_gilbert_data(self, minio_url, minio_bucket):
        dg_config = DziewonskiGilbertMinioConfig(minio_url, minio_bucket)
        processing_config_path = os.path.join(self.default_config_path, os.path.join("ellipticity-correction", "dziewonski-gilbert"))
        dg_loader = DziewonskiGilbertLoader(processing_config_path, dg_config)
        dg_loader.load()

