#!/usr/bin/env python3

import logging
import logging.handlers
import time
import os
from datetime import datetime
from io import StringIO

import requests
from flask import current_app
from gmsdataloader import GmsDataloader

from . import app_state
from . import executor
from . import log_queue

logger = logging.getLogger(__package__)

# logger for gmsdataloader package
gmsdataloader_logger = logging.getLogger('gmsdataloader')

# top level logger
top_logger = logging.getLogger()


def config_services_available() -> bool:
    """
    Return True if the processing configuration and osd services are both available.
    """
    processing_config_service_ready = False
    osd_service_ready = False
    minio_service_ready_or_nonexistent = False

    try:
        response = requests.get(f'http://{ current_app.config["PROCESSING_CONFIG_SERVICE_NAME"] }:8080/{ current_app.config["PROCESSING_CONFIG_SERVICE_NAME"] }/alive', timeout=1)
        if response.status_code == 200:
            processing_config_service_ready = True
        else:
            logger.info(f'{ current_app.config["PROCESSING_CONFIG_SERVICE_NAME"] } not alive... waiting')
            
    except Exception:
        logger.info(f'{ current_app.config["PROCESSING_CONFIG_SERVICE_NAME"] } not reachable... waiting')
        # logger.info(e)

    try:
        response = requests.get(f'http://{ current_app.config["OSD_SERVICE_NAME"] }:8080/{ current_app.config["OSD_SERVICE_NAME"] }/alive', timeout=1)
        if response.status_code == 200:
            osd_service_ready = True
        else:
            logger.info(f'{ current_app.config["OSD_SERVICE_NAME"] } not alive... waiting')
            
    except Exception:
        logger.info(f'{ current_app.config["OSD_SERVICE_NAME"] } not reachable... waiting')
        # logger.info(e)

    try:
        response = requests.get(f'http://{ current_app.config["USER_MANAGER_SERVICE_NAME"] }:8080/{ current_app.config["USER_MANAGER_SERVICE_NAME"] }/alive', timeout=1)
        if response.status_code == 200:
            osd_service_ready = True
        else:
            logger.info(f'{ current_app.config["USER_MANAGER_SERVICE_NAME"] } not alive... waiting')
            
    except Exception:
        logger.info(f'{ current_app.config["USER_MANAGER_SERVICE_NAME"] } not reachable... waiting')
        # logger.info(e)

    # If the minio credentials are not set in the environment, assume we don't need to load minio.
    if os.getenv('MINIO_ROOT_USER') is not None:
        # TODO: Look into how to handle both IAN and SOH.
        try:
            response = requests.get(f'http://{ current_app.config["MINIO_SERVICE_NAME"] }:9000/minio/health/ready')
            if response.status_code == 200:
                minio_service_ready_or_nonexistent = True
            else:
                logger.info(f'{current_app.config["MINIO_SERVICE_NAME"]} not ready... waiting')

        except Exception:
            logger.info(f'{current_app.config["MINIO_SERVICE_NAME"]} not reachable... waiting')
    else:
        logger.info('MINIO_ROOT_USER not set, assuming minio is not deployed')
        minio_service_ready_or_nonexistent = True

    return processing_config_service_ready and osd_service_ready and minio_service_ready_or_nonexistent


def dataload(load_processing_config: bool = False,
             load_station_reference_data: bool = False,
             load_station_processing_data: bool = False,
             update_station_group_definitions: bool = False,
             load_user_preferences: bool = False,
             load_earth_models: bool = False,
             load_mediumvelocity: bool = False,
             load_dg: bool = False) -> bool:

    try:
        processing_config_url = f'http://{ current_app.config["PROCESSING_CONFIG_SERVICE_NAME"] }:8080/{ current_app.config["PROCESSING_CONFIG_SERVICE_NAME"] }'
        osd_url = f'http://{ current_app.config["OSD_SERVICE_NAME"] }:8080/{ current_app.config["OSD_SERVICE_NAME"] }/osd'
        user_manager_service_url = f'http://{ current_app.config["USER_MANAGER_SERVICE_NAME"] }:8080/{ current_app.config["USER_MANAGER_SERVICE_NAME"] }'

        start_time = datetime.now()
        
        dataloader = GmsDataloader(current_app.config["BASE_CONFIG_PATH"], current_app.config["OVERRIDE_CONFIG_PATH"]) 

        if load_processing_config:
            logger.info('Loading processing configuration...')
            dataloader.load_processing_config(processing_config_url)
            logger.info('Processing configuration loaded successfully')

        if load_station_reference_data:
            logger.info('Loading station reference data...')
            dataloader.load_station_reference_data(osd_url)
            logger.info('Station reference data loaded successfully')

        if load_station_processing_data:
            logger.info('Loading station processing data...')
            dataloader.load_station_processing_data(osd_url)
            logger.info('Station processing data loaded successfully')

        #-- this must always be done AFTER the stations processing data has been loaded
        if update_station_group_definitions:
            logger.info('Updating station group definitions...')
            dataloader.update_station_group_definitions(osd_url)
            logger.info('Station group definitions updated successfully')
                
        if load_user_preferences:
            logger.info('Loading user preferences...')
            dataloader.load_user_preferences(user_manager_service_url)
            logger.info('User preference data loaded successfully')

        # If the minio credentials are not set in the environment, assume we don't need to load minio.
        if load_earth_models and os.getenv("MINIO_ROOT_USER") is not None:
            minio_url = f'{ current_app.config["MINIO_SERVICE_NAME"] }:9000'
            logger.info("Loading earth models...")
            dataloader.load_earthmodel_data(minio_url, current_app.config["MINIO_FEATURE_PREDICTION_SERVICE_BUCKET"])
            logger.info("Earth models loaded successfully")

        if load_mediumvelocity and os.getenv("MINIO_ROOT_USER") is not None:
            minio_url = f'{current_app.config["MINIO_SERVICE_NAME"]}:9000'
            logger.info("Loading mediumvelocity...")
            dataloader.load_mediumvelocity_data(minio_url, current_app.config["MINIO_FEATURE_PREDICTION_SERVICE_BUCKET"])
            logger.info("mediumvelocity loaded successfully")

        if load_dg and os.getenv("MINIO_ROOT_USER") is not None:
            minio_url = f'{current_app.config["MINIO_SERVICE_NAME"]}:9000'
            logger.info("Loading DG ellipticty tables...")
            dataloader.load_dziewonski_gilbert_data(minio_url, current_app.config["MINIO_FEATURE_PREDICTION_SERVICE_BUCKET"])
            logger.info("Dziewonski-Gilbert tables loaded successfully")

    except Exception as ex:
        logger.error(ex)
        return False

    delta_time = datetime.now() - start_time
    logger.info(f'Dataload completed successfully in { str(delta_time).split(".")[0] }') # leave off microseconds
    return True


@executor.job
def loader(reload: bool = False) -> tuple[bool, str]:
    app_state.set_loading()
    
    action = 'reload' if reload else 'load'

    # queue handler for streaming partial log output
    queue_handler = logging.handlers.QueueHandler(log_queue)
    top_logger.addHandler(queue_handler)

    logger.info(f'Processing { action } request...')

    loader_log_output = ""

    #-- keep trying until config services are available (or we time out)
    waited = 0
    while True:
        check_interval = current_app.config['SERVICE_CHECK_INTERVAL']
        
        if config_services_available():
            
            # -- add a logging handler to capture dataload logging into a string
            # Setup the console handler with a StringIO object
            log_stream = StringIO()

            # create console handler and set level to debug
            formatter = logging.Formatter('[%(levelname)s] %(message)s')
            capture_handler = logging.StreamHandler(log_stream)
            capture_handler.setLevel(logging.DEBUG)
            capture_handler.setFormatter(formatter)

            # add console handler to this logger and the gmsdataloader logger
            logger.addHandler(capture_handler)
            gmsdataloader_logger.addHandler(capture_handler)

            if not reload:
                load_successful = dataload(load_processing_config=True,
                                           load_station_reference_data=True,
                                           load_station_processing_data=True,
                                           update_station_group_definitions=True,
                                           load_user_preferences=True,
                                           load_earth_models=True,
                                           load_mediumvelocity=True,
                                           load_dg=True)
            else:
                load_successful = dataload(load_processing_config=True,
                                           update_station_group_definitions=True)

            loader_log_output = log_stream.getvalue()
            log_stream.close()
            
            logger.removeHandler(capture_handler)
            gmsdataloader_logger.removeHandler(capture_handler)
            
            if load_successful:
                app_state.set_loaded(reload)  #-- load/reload successful
                top_logger.removeHandler(queue_handler)
                return True, loader_log_output
            else:
                break
            
        elif waited + check_interval <= current_app.config['SERVICE_WAIT_TIMEOUT']:
            time.sleep(check_interval)
            waited += check_interval
            
        else:
            loader_log_output = f'{ action } TIMED OUT: config services still unavailable after { waited } seconds'
            logger.error(loader_log_output)
            break

    app_state.set_started()  #-- not successful: go back to 'started' state
    top_logger.removeHandler(queue_handler)
    return False, loader_log_output
