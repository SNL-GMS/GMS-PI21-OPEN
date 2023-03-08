import logging
import sys

from gmsdataloader.genericdataloader import load_data
from gmsdataloader.stationprocessing import StationProcessingLoaderConfig

# Create logger
logger = logging.getLogger('gmsdataloader') # logger name must match config-loader for log capture


class StationProcessingLoader:
    """
        Loads Station Reference Information from the filesystem and publishes them to a specified location
    """

    def __init__(self, config: StationProcessingLoaderConfig, total_retries: int | None = 5) -> None:
        """
        Constructor

        Keyword arguments:
            config -- Configuration to be used by the loader
            total_retries -- Number of times to attempt a successful POST before failing
        """
        self.config = config
        self.total_retries = total_retries

    def load(self, station_groups_path: str) -> None:
        """
        Loads station processing information from the filesystem and publishes them
        to their respective endpoints

        Keyword arguments:
            station_groups_path -- Path to Station Group Array (JSON)
        """
        load_data(*(station_groups_path, self.config.station_groups_new_url), self.total_retries)

    def load_sta_group_updates(self, station_group_definitions_path: str) -> None:
        """
        Loads station group definitions from the filesystem and publishes them
        to their configured endpoint, which updates station groups in the OSD

        Keyword arguments:
            station_group_definitions_path -- Path to Station Group Definition Array (JSON)
        """
        load_data(*(station_group_definitions_path, self.config.station_groups_update_url), self.total_retries)
