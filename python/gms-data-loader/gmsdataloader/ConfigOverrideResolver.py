import os
import logging


# Create logger
logger = logging.getLogger('gmsdataloader') # logger name must match config-loader for log capture


class ConfigOverrideResolver:
    """
    Resolve the full path to a configuration file based on a relative file path.
    This will use the override version if available, otherwise 
    """

    def __init__(self, default_config_root: str, override_config_root: str | None = None) -> None:
        """
        default_config_root: the path to the default configuration root directory
        override_config_root: if available, the path to the override configuration root directory
        """
        self.default_config_root  = default_config_root
        self.override_config_root = override_config_root

    def path(self, relative_file_path: str) -> str:
        """
        Resolve the full path to a configuration file based on a relative file path.

        This will use the override path if available, otherwise it will return the default
        path. If the file is not available, an exception will be thrown.

        relative_file_path: the path relative to the config 
        """
        if self.override_config_root:
            override_path = os.path.join(self.override_config_root, relative_file_path)
            if os.path.exists(override_path):
                logger.info(f"Loading override { relative_file_path }")
                return override_path
    
        default_path = os.path.join(self.default_config_root, relative_file_path)
        if os.path.exists(default_path):
            logger.info(f"Loading default { relative_file_path }")
            return default_path

        # file not found in either location
        raise OSError(f"Configuration file { default_path } not found.")

