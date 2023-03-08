import os
import importlib.resources as pkg_resources

from configparser import ConfigParser

class UserPreferencesLoaderConfig:
    """
    Route Configuration used by UserPreferencesLoader
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

        self.user_prefs_obj_urls = UserPreferenceObjectUrls(
            base_url=base_url,
            user_preferences_route=self._loader_config['routes']['user_preferences'])


class UserPreferenceObjectUrls:
    def __init__(self, base_url: str, user_preferences_route: str) -> None:
        self.user_preferences_url = base_url + user_preferences_route
