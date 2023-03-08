import logging
import sys
from concurrent.futures.thread import ThreadPoolExecutor
from dataclasses import dataclass

from gmsdataloader.genericdataloader.data_loader import load_data
from gmsdataloader.userpreferences.user_preferences_loader_config import UserPreferencesLoaderConfig

@dataclass
class UserPreferencesObjectPaths:
    """
    Data Class containing paths to User Preferences Object sources

    Keyword arguments:
            user_preferences_path -- Path to User Preferences Data (JSON)
    """

    user_preferences_path: str


class UserPreferencesLoader:
    """
    Loads User Preferences Information from the filesystem and publishes them to a specified location
    """

    def __init__(self, config: UserPreferencesLoaderConfig, total_retries: int | None = 5) -> None:
        """
        Constructor

        Keyword arguments:
            config -- Configuration to be used by the loader
            total_retries -- Number of times to attempt a successful POST before failing
        """
        self.config = config
        self.total_retries = total_retries


    def load_user_preferences(self, user_preferences_path: str) -> None:
        """
        Loads data for user preferences objects given file paths and publishes to the respective configured endpoint

        Keyword arguments:
            user_preferences_path -- Path to User Preferences Array (JSON)
        """
        future_list = list()
        with ThreadPoolExecutor(max_workers=1) as executor:
            user_prefs_obj_urls = self.config.user_prefs_obj_urls
            future_list.append(
                executor.submit(load_data, *(user_preferences_path, user_prefs_obj_urls.user_preferences_url, self.total_retries)))

        for future in future_list:
            if future.exception() is not None:
                raise future.exception()

    def load(self, user_prefs_obj_paths: UserPreferencesObjectPaths) -> None:
        """
        High-level loader function that reads user preference information from the filesystem and publishes them to its
        respective endpoint

        Keyword arguments:
            user_preferences_path -- Path to User Preferences Array (JSON)
        """
        self.load_user_preferences(user_preferences_path=user_prefs_obj_paths.user_preferences_path)

