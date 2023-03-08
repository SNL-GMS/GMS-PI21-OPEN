import os

import requests


def get_resource_path(resource):
    """
    Gets the absolute path to a file/directory in the resources directory
    :param resource: relative path to the file/directory in the resources directory
    :return: absolute path string
    """
    return os.path.join(os.path.dirname(os.path.abspath(__file__)), 'resources', resource)


def get_test_custom_chart_path():
    """
    Gets the absolute path to a test custom chart
    :return: absolute path string
    """
    return get_resource_path('custom-chart')


def get_config_overrides_path():
    """
    Gets the absolute path to a test config overrides
    :return: absolute path string
    """
    return get_resource_path('config_overrides')


def get_test_file_contents(resource):
    """
    Gets the contents of a test resource file
    :param resource: relative path to the resource file in resources directory
    :return: string with contents of the test file
    """
    with open(get_resource_path(resource), 'r') as test_file:
        return test_file.read()


def get_request_response(status_code):
    """
    Gets a requests.Response object with the http status_code set
    :param status_code: http status code to be set
    :return: requests.Response object
    """

    response = requests.Response()
    response.status_code = status_code
    return response
