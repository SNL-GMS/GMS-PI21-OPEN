import os
import importlib.resources as pkg_resources

from configparser import ConfigParser

class StationProcessingLoaderConfig:
    """
    Route Configuration used by StationProcessingLoader
    """

    def __init__(self, base_url: str, config_path: str | None = None) -> None:
        """
        Constructor

        Keyword arguments:
            base_url -- base URL for the loader to publish to
            config_path -- configuration file path (Python ConfigParser format)
        """

        # use the default config.ini from our package if one was not specified
        if not config_path:
            with pkg_resources.path(__package__, 'resources') as resources:
                config_path = os.path.join(resources, 'config/config.ini')
            
        self._loader_config = ConfigParser()
        with open(config_path) as config_file:
            self._loader_config.read_file(config_file)

        self.station_groups_new_url = base_url + self._loader_config['routes']['station_groups_new']
        self.station_groups_update_url = base_url + self._loader_config['routes']['station_groups_update']
