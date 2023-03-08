import os
import importlib.resources as pkg_resources

from configparser import ConfigParser

class StationReferenceLoaderConfig:
    """
    Route Configuration used by StationReferenceLoader
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

        self.sta_ref_obj_urls = StationReferenceObjectUrls(
            base_url=base_url,
            ref_networks_route=self._loader_config['routes']['ref_networks'],
            ref_stations_route=self._loader_config['routes']['ref_stations'],
            ref_sites_route=self._loader_config['routes']['ref_sites'],
            ref_chans_route=self._loader_config['routes']['ref_chans'],
            ref_sensors_route=self._loader_config['routes']['ref_sensors'])
        self.sta_ref_memb_urls = StationReferenceMembershipUrls(
            base_url=base_url,
            ref_network_memb_route=self._loader_config['routes']['ref_network_memberships'],
            ref_station_memb_route=self._loader_config['routes']['ref_station_memberships'],
            ref_site_memb_route=self._loader_config['routes']['ref_site_memberships'])


class StationReferenceObjectUrls:
    def __init__(self, base_url: str, ref_networks_route: str, ref_stations_route: str,
                 ref_sites_route: str, ref_chans_route: str, ref_sensors_route: str) -> None:
        self.ref_networks_url = base_url + ref_networks_route
        self.ref_stations_url = base_url + ref_stations_route
        self.ref_sites_url = base_url + ref_sites_route
        self.ref_chans_url = base_url + ref_chans_route
        self.ref_sensors_url = base_url + ref_sensors_route


class StationReferenceMembershipUrls:
    def __init__(self, base_url: str, ref_network_memb_route: str, ref_station_memb_route: str,
                 ref_site_memb_route: str) -> None:
        self.ref_network_memb_url = base_url + ref_network_memb_route
        self.ref_station_memb_url = base_url + ref_station_memb_route
        self.ref_site_memb_url = base_url + ref_site_memb_route
