import datetime
import json
import logging
import os
from http import HTTPStatus

import requests
from requests.adapters import HTTPAdapter
from requests.packages.urllib3.util.retry import Retry

# Create logger: the 'gmsdataloader' name must match in config-loader for log capture to work
logger = logging.getLogger('gmsdataloader')  # Must match log capture in config-loader


class Configuration:
    """
    Class to store a Configuration and list of configuration options.
    """

    def __init__(self, name: str) -> None:
        self.name = name

        self.configurationOptions = []  # NOSONAR - Jackson not unmarshalling this correctly if using snake_case
        self.changeTime = datetime.datetime.now().timestamp()  # NOSONAR - Jackson not unmarshalling this correctly if using snake_case

    def get_name(self) -> str:
        return self.name

    def add_configuration_option(self, configuration_option_json: str) -> None:
        configuration_option = json.loads(configuration_option_json)
        if type(configuration_option) == list:
            for configuration in configuration_option:
                self.configurationOptions.append(configuration)
        else:
            self.configurationOptions.append(
                configuration_option)  # NOSONAR - Jackson not unmarshalling this correctly if using snake_case


class ProcessingConfigLoader:
    json_file_type = '.json'

    """
    Loads Processing Configuration from the file system and publishes it to the specified endpoint
    """

    def __init__(self, url: str, processing_config_root: str, override_root: str | None = None) -> None:
        """
        Constructor

        Keyword arguments:
        processing_config_root -- path to processing configuration root directory
        override_root -- path to processing configuration override root directory
        url -- the endpoint to publish to
        """
        self.processing_config_root = processing_config_root
        self.override_root = override_root
        self.api_endpoint = url + "/processing-cfg/put-all"

        logger.info(f"processing_config_root: {self.processing_config_root}")
        if self.override_root:
            logger.info(f"override_root: {self.override_root}")
        logger.info(f"endpoint: {self.api_endpoint}")

    def load(self) -> bool:
        """
        Loads Procesing Configuraiton from the file system
        """
        config_files = self.get_config_files()
        cfg_list = self.create_configurations(config_files)

        if not cfg_list:
            return False

        if not self.post_cfgs(cfg_list):
            return False

        return True

    def get_config_files(self) -> dict:  # NOSONAR - consider refactoring later to reduce cognitive complexity
        """
        Traverses files located in processing_config_root and overrides to get all the relative paths to the json and yaml files
        """

        # dictionary of configuraton names, with each configuration name containing a list of configuration files
        config_files = {}

        # fill out our the config files list first with any override files that are present
        if self.override_root:
            if os.path.exists(self.override_root) and os.path.isdir(self.override_root):
                for name, path in [(f.name, f.path) for f in os.scandir(self.override_root) if f.is_dir()]:
                    config_files[name] = []
                    for config_filename in [f for f in os.listdir(path) if f.endswith(self.json_file_type)]:
                        config_files[name].append(
                            {'name': config_filename, 'path': os.path.join(path, config_filename),
                             'override': True})
            else:
                logger.warning(
                    f'Processing configuration overrides {self.override_root} must be a directory. Ignoring.')

        # look at our base configuration and get anything that wasn't in overrides
        if not os.path.isdir(self.processing_config_root):
            raise ValueError(f'Processing configuration root {self.processing_config_root} must be a directory.')

        for name, path in [(f.name, f.path) for f in os.scandir(self.processing_config_root) if f.is_dir()]:
            # read this folder only if we have not seen a corresponding folder already from the overrides
            if name not in config_files:
                config_files[name] = []
                for config_filename in [f for f in os.listdir(path) if f.endswith(self.json_file_type)]:
                    config_files[name].append(
                        {'name': config_filename, 'path': os.path.join(path, config_filename), 'override': False})

        return config_files

    def create_configurations(self, config_files: dict) -> list[Configuration]:
        """
        Creates Configuration Objects from json files located in the fileSet collection
        """
        cfg_list = []

        for config_name in sorted(config_files.keys()):
            logger.info(f"Reading processing configuration options for {config_name}")
            cfg = Configuration(config_name)
            for config_file in config_files[config_name]:
                if config_file['override']:
                    logger.info(f"- Loading override options for {config_file['name']}")
                else:
                    logger.info(f"- Loading default options for {config_file['name']}")
                with open(config_file['path']) as file:
                    if config_file['name'].endswith(self.json_file_type):
                        data = json.dumps(json.load(file))
                    else:
                        raise ValueError(f"Processing configuration file {config_file['path']} must be a json file")
                    cfg.add_configuration_option(data)
            cfg_list.append(cfg)

        return cfg_list

    def post_cfgs(self, cfg_list: list[Configuration]) -> bool:
        """
        Create json array from Configuration objects and post to the processing-configuration-service
        """
        json_list = []
        for cfg in cfg_list:
            json_list.append(json.loads(json.dumps(cfg.__dict__)))

        retry_strategy = Retry(total=10,
                               backoff_factor=1,
                               status_forcelist=[404, 429, 502, 503, 504],
                               allowed_methods=["POST"])
        adapter = HTTPAdapter(max_retries=retry_strategy)
        http = requests.Session()
        http.mount("https://", adapter)
        http.mount("http://", adapter)

        logger.info("Processing configuration posted to: " + self.api_endpoint)

        headers = {'Content-Type': 'application/json', 'Accept': 'application/json'}
        response = http.post(url=self.api_endpoint, json=json_list, headers=headers)

        if response.status_code == 200:
            logger.info("Response: 200 in processing_config_loader")
        else:
            logger.error("Response: Error in processing_config_loader retreving configuration objects")
            return False

        return True
