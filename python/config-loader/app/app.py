#!/usr/bin/env python3

# --------------------------------------------------------------------
#  config-loader
#
#  The config-loader service...
# --------------------------------------------------------------------

import logging
import logging.handlers
import os
import shutil
import tarfile
from io import StringIO

from flask import Flask, jsonify
from flask import current_app
from flask import request
from flask import Response

logger = logging.getLogger(__package__)

def create_app(env=None):
    from . import app_state
    from . import executor
    from . import config_by_name
    from . import initiate_load
    from . import initiate_reload
    from . import log_queue

    app = Flask(__name__)
    app.config.from_object(config_by_name[env or "test"])

    logging.basicConfig(format='[%(asctime)s] [%(process)s] [%(levelname)s] %(message)s', level=app.config['LOG_LEVEL'])
    
    executor.init_app(app)
    
    with app.app_context():

        # Environment option to automatically load default configuration on startup for testing
        if environment_bool('GMS_CONFIG_AUTOLOAD_DEFAULTS'):
            with app.test_request_context():
                initiate_load('load_results')
        
        @app.route("/config-loader/alive")
        def alive() -> tuple[Response, int]:
            return jsonify("alive"), 200  # If we are running, then we report that we are alive.

        @app.route("/config-loader/initialized")
        def initialized() -> tuple[Response, int]:
            if app_state.get_state() == 'loaded':
                return jsonify("loaded")
            else:
                return jsonify("not loaded")
            
        @app.route("/config-loader/load", methods=['POST'])
        def load() -> tuple[Response, int]:
            f = request.files.get('files')
            if f and valid_override_path(current_app.config["OVERRIDE_CONFIG_PATH"]):
                # Clear the previous overrides
                if os.path.exists(current_app.config["OVERRIDE_CONFIG_PATH"]):
                    shutil.rmtree(current_app.config["OVERRIDE_CONFIG_PATH"])
                    
                f.save(current_app.config["TARFILE_NAME_FULLPATH"])
                tar = tarfile.open(current_app.config["TARFILE_NAME_FULLPATH"])
                tar.extractall(current_app.config["OVERRIDE_CONFIG_PATH"])
                tar.close()

                # Remove the tar file
                if os.path.exists(current_app.config["TARFILE_NAME_FULLPATH"]):
                    os.remove(current_app.config["TARFILE_NAME_FULLPATH"])                

            return initiate_load('load_results')
            
        @app.route("/config-loader/reload", methods=['POST'])
        def reload() -> tuple[Response, int]:
            f = request.files['files']
            if f and valid_override_path(current_app.config["OVERRIDE_CONFIG_PATH"]):
                # Clear the previous overrides
                if os.path.exists(current_app.config["OVERRIDE_CONFIG_PATH"]):
                    shutil.rmtree(current_app.config["OVERRIDE_CONFIG_PATH"])
                    
                f.save(current_app.config["TARFILE_NAME_FULLPATH"])
                tar = tarfile.open(current_app.config["TARFILE_NAME_FULLPATH"])
                tar.extractall(current_app.config["OVERRIDE_CONFIG_PATH"])
                tar.close()
                
                # Remove the tar file
                if os.path.exists(current_app.config["TARFILE_NAME_FULLPATH"]):
                    os.remove(current_app.config["TARFILE_NAME_FULLPATH"])

            return initiate_reload('load_results')
    
        @app.route("/config-loader/result")
        def result() -> Response:
            # get any partial log output in the queue with the queue listener
            partial_log_stream = StringIO()
            handler = logging.StreamHandler(partial_log_stream)
            listener = logging.handlers.QueueListener(log_queue, handler)
            formatter = logging.Formatter('[%(levelname)s] %(message)s')
            handler.setFormatter(formatter)
            handler.setLevel(logging.DEBUG)
            listener.start()
            listener.stop()

            # send current state plus partial log
            if not executor.futures.done('load_results'):
                return jsonify({'status': executor.futures._state('load_results'), 'partial_result': partial_log_stream.getvalue(), 'result': ''})

            # get final state, any remaining partial log, and the final log
            future = executor.futures.pop('load_results')
            success, log_output = future.result()
            return jsonify({'status': 'FINISHED', 'successful': success, 'partial_result': partial_log_stream.getvalue(), 'result': log_output})

        @app.route("/config-loader/service-internal-state")
        def service_state() -> dict:
            return app_state.as_dict()

        @app.errorhandler(404)
        def not_found_error(error: int) -> tuple[str, int]:
            return '404 error', 404

    return app

def environment_bool(name: str) -> bool:
    value = os.environ.get(name, 'false').lower()
    return not (value == '0' or value == 'false')  # Consider *anything* except '0' or 'false' to be True

def valid_override_path(path: str) -> bool:
    "Ensure that the override path won't overwrite any system directories (to appease Fortify)"
    dangerous_paths = [ "/etc", "/usr", "/bin", "/sbin", "/dev" ]
    for p in dangerous_paths:
        if path.startswith(p):
            logger.error(f"Specified CONFIG_OVERRIDE_PATH '{path}' would overwrite system files. Ignoring overrides...")
            return False
    return True
