import logging
import sys
from concurrent.futures.thread import ThreadPoolExecutor
from dataclasses import dataclass

from gmsdataloader.genericdataloader import load_data
from gmsdataloader.stationreference import StationReferenceLoaderConfig

# Create logger
logger = logging.getLogger('gmsdataloader') # logger name must match config-loader for log capture


@dataclass
class StationReferenceObjectPaths:
    """
    Data Class containing paths to Station Reference Object sources

    Keyword arguments:
            ref_networks_path -- Path to Reference Networks Array (JSON)
            ref_stations_path -- Path to Reference Stations Array (JSON)
            ref_sites_path -- Path to Reference Sites Array (JSON)
            ref_chans_path -- Path to Reference Channels Array (JSON)
            ref_sensors_path -- Path to Reference Sensors Array (JSON)
            total_retries -- Number of times to attempt a successful POST before failing
    """

    ref_networks_path: str
    ref_stations_path: str
    ref_sites_path: str
    ref_chans_path: str
    ref_sensors_path: str


@dataclass
class StationReferenceMembershipPaths:
    """
    Data Class containing paths to Station Reference Membership sources

    Keyword arguments:
        ref_net_memb_path -- Path to Reference Network Memberships Array (JSON)
        ref_sta_memb_path -- Path to Reference Station Memberships Array (JSON)
        ref_site_memb_path -- Path to Reference Site Memberships Array (JSON)
    """

    ref_net_memb_path: str
    ref_sta_memb_path: str
    ref_site_memb_path: str


class StationReferenceLoader:
    """
    Loads Station Reference Information from the filesystem and publishes them to a specified location

    NOTE: Publishing of station reference information follows this dependency hierarchy:
            1. Reference Objects (i.e. Networks, Stations, Sites, Channels, Sensors)
            2. Reference Memberships (i.e. Network Memberships, Station Memberships, Site Memberships)

            If any reference objects (e.g. Networks, Channels) within the first step fails, none of the reference
            memberships are loaded

            However, since actions within each step are run concurrently, it is possible for one or more actions in the
            same step to fail while the rest in the step succeed
    """

    def __init__(self, config: StationReferenceLoaderConfig, total_retries: int | None = 5) -> None:
        """
        Constructor

        Keyword arguments:
            config -- Configuration to be used by the loader
        """
        self.config = config
        self.total_retries = total_retries

    def load_ref_objects(self,
                         ref_networks_path: str,
                         ref_stations_path: str,
                         ref_sites_path: str,
                         ref_chans_path: str,
                         ref_sensors_path: str) -> None:
        """
        Loads data for reference objects given file paths and publishes to their respective configured endpoints

        Keyword arguments:
            ref_networks_path -- Path to Reference Networks Array (JSON)
            ref_stations_path -- Path to Reference Stations Array (JSON)
            ref_sites_path -- Path to Reference Sites Array (JSON)
            ref_chans_path -- Path to Reference Channels Array (JSON)
            ref_sensors_path -- Path to Reference Sensors Array (JSON)
        """
        future_list = list()
        with ThreadPoolExecutor(max_workers=5) as executor:
            ref_obj_urls = self.config.sta_ref_obj_urls
            future_list.append(
                executor.submit(load_data, *(ref_networks_path, ref_obj_urls.ref_networks_url), self.total_retries))
            future_list.append(
                executor.submit(load_data, *(ref_stations_path, ref_obj_urls.ref_stations_url), self.total_retries))
            future_list.append(executor.submit(load_data, *(ref_sites_path, ref_obj_urls.ref_sites_url), self.total_retries))
            future_list.append(executor.submit(load_data, *(ref_chans_path, ref_obj_urls.ref_chans_url), self.total_retries))
            future_list.append(executor.submit(load_data, *(ref_sensors_path, ref_obj_urls.ref_sensors_url), self.total_retries))

        for future in future_list:
            if future.exception() is not None:
                raise future.exception()

        logger.info("Reference Objects (Networks, Stations, Sites, Channels, Sensors) loaded successfully")

    def load_ref_memberships(self,
                             ref_net_memb_path: str,
                             ref_sta_memb_path: str,
                             ref_site_memb_path: str) -> None:
        """
        Loads data for reference objects given file paths and publishes to their respective configured endpoints

        Keyword arguments:
            ref_net_memb_path -- Path to Reference Network Memberships Array (JSON)
            ref_sta_memb_path -- Path to Reference Station Memberships Array (JSON)
            ref_site_memb_path -- Path to Reference Site Memberships Array (JSON)
        """
        future_list = list()
        with ThreadPoolExecutor(max_workers=3) as executor:
            membership_urls = self.config.sta_ref_memb_urls
            future_list.append(
                executor.submit(load_data, *(ref_net_memb_path, membership_urls.ref_network_memb_url), self.total_retries))
            future_list.append(
                executor.submit(load_data, *(ref_sta_memb_path, membership_urls.ref_station_memb_url), self.total_retries))
            future_list.append(
                executor.submit(load_data, *(ref_site_memb_path, membership_urls.ref_site_memb_url), self.total_retries))

        for future in future_list:
            if future.exception() is not None:
                raise future.exception()

        logger.info("Reference memberships (Networks, Stations, Sites) loaded successfully")

    def load(self,
             sta_ref_obj_paths: StationReferenceObjectPaths,
             sta_ref_memb_paths: StationReferenceMembershipPaths) -> None:
        """
        High-level loader function that loads station reference information from the filesystem and publishes them
        to their respective endpoints

        Keyword arguments:
            ref_networks_path -- Path to Reference Networks Array (JSON)
            ref_stations_path -- Path to Reference Stations Array (JSON)
            ref_sites_path -- Path to Reference Sites Array (JSON)
            ref_chans_path -- Path to Reference Channels Array (JSON)
            ref_sensors_path -- Path to Reference Sensors Array (JSON)
            ref_net_memb_path -- Path to Reference Network Memberships Array (JSON)
            ref_sta_memb_path -- Path to Reference Station Memberships Array (JSON)
            ref_site_memb_path -- Path to Reference Site Memberships Array (JSON)
        """
        self.load_ref_objects(ref_networks_path=sta_ref_obj_paths.ref_networks_path,
                              ref_stations_path=sta_ref_obj_paths.ref_stations_path,
                              ref_sites_path=sta_ref_obj_paths.ref_sites_path,
                              ref_chans_path=sta_ref_obj_paths.ref_chans_path,
                              ref_sensors_path=sta_ref_obj_paths.ref_sensors_path)
        self.load_ref_memberships(ref_net_memb_path=sta_ref_memb_paths.ref_net_memb_path,
                                  ref_sta_memb_path=sta_ref_memb_paths.ref_sta_memb_path,
                                  ref_site_memb_path=sta_ref_memb_paths.ref_site_memb_path)
