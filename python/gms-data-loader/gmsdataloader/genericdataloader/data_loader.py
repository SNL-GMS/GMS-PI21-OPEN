import json
import logging

import requests
from requests import HTTPError
from requests.adapters import HTTPAdapter
from requests.packages.urllib3.util.retry import Retry

# Create logger
logger = logging.getLogger('gmsdataloader') # logger name must match config-loader for log capture


def read_json_data(path: str) -> object:
    with open(path) as json_data:
        return json.load(json_data)


def load_data(data_file_path: str, url: str, total_retries: str | None = 5) -> None:
    """
    Loads data referenced in the filesystem and publishes it to the specified endpoint

    Keyword arguments:
        data_file_path -- file path to load the data from
        url -- the endpoint to publish to
    """
    ref_data = read_json_data(data_file_path)

    if total_retries == 1:
        response = requests.post(url, json=ref_data)
    else:
        retry_strategy = Retry(total=total_retries,
                               backoff_factor=1,
                               status_forcelist=[ 404, 429, 502, 503, 504 ],
                               allowed_methods=["POST"])

        adapter = HTTPAdapter(max_retries=retry_strategy)
        http = requests.Session()
        http.mount("https://", adapter)
        http.mount("http://", adapter)
        response = http.post(url, json=ref_data)
        
    if not response.ok:
        http_err_str = "\n\tError: {} {}\n\tBody Text: \n\t{}".format(response.status_code, response.reason,
                                                                      response.text)
        raise HTTPError(http_err_str, response=response)

    logger.info("Data posted to " + url + " successfully")
