import os
from typing import Type

basedir = os.path.abspath(os.path.dirname(__file__))

class BaseConfig:
    CONFIG_NAME = "base"
    LOG_LEVEL = "INFO"
    DEBUG = False
    EXECUTOR_TYPE = 'thread'
    EXECUTOR_PROPAGATE_EXCEPTIONS = True
    PROCESSING_CONFIG_SERVICE_NAME = "frameworks-configuration-service"
    OSD_SERVICE_NAME = "frameworks-osd-service"
    USER_MANAGER_SERVICE_NAME = "user-manager-service"
    MINIO_SERVICE_NAME = "minio"
    MINIO_FEATURE_PREDICTION_SERVICE_BUCKET = "feature-prediction-models"
    BASE_CONFIG_PATH = "/opt/gms/base"
    OVERRIDE_CONFIG_PATH = "/opt/gms/override"
    SERVICE_WAIT_TIMEOUT = 600
    SERVICE_CHECK_INTERVAL = 5
    TARFILE_NAME_FULLPATH = '/tmp/tardata.tar.gz'

class DevelopmentConfig(BaseConfig):
    CONFIG_NAME = "dev"
    DEBUG = True
    TESTING = False

class TestingConfig(BaseConfig):
    CONFIG_NAME = "test"
    DEBUG = True
    TESTING = True

class ProductionConfig(BaseConfig):
    CONFIG_NAME = "prod"
    DEBUG = False
    TESTING = False

EXPORT_CONFIGS: list[Type[BaseConfig]] = [
    DevelopmentConfig,
    TestingConfig,
    ProductionConfig,
]

config_by_name = {cfg.CONFIG_NAME: cfg for cfg in EXPORT_CONFIGS}
