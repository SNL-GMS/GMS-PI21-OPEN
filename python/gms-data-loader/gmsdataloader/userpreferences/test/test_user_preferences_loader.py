import os
import unittest
from unittest.mock import patch, call, MagicMock

from requests import HTTPError

import sys

from gmsdataloader.genericdataloader import read_json_data
from gmsdataloader.userpreferences import UserPreferencesLoader
from gmsdataloader.userpreferences import UserPreferencesObjectPaths
from gmsdataloader.userpreferences import UserPreferencesLoaderConfig


POST_REQUEST = 'requests.post'

WORKING_DIR = os.path.dirname(__file__)

DUMMY_JSON = f'{WORKING_DIR}/resources/json/dummy.json'
BROKEN_JSON = f'{WORKING_DIR}/resources/json/broken.json'

DUMMY_USER_PREFS_OBJ_PATHS = UserPreferencesObjectPaths(DUMMY_JSON)
BROKEN_USER_PREFS_OBJ_PATHS = UserPreferencesObjectPaths(BROKEN_JSON)
BAD_PATH_USER_PREFS_OBJ_PATHS = UserPreferencesObjectPaths('')

DEFAULT_LOADER_CONFIG = UserPreferencesLoaderConfig('test-ref-loader.org',
                                                    f'{WORKING_DIR}/resources/config/test_config.ini')
DEFAULT_LOADER = UserPreferencesLoader(DEFAULT_LOADER_CONFIG, total_retries=1)


class TestUserPreferencesLoader(unittest.TestCase):

    def test_load_dummy_data(self):
        with patch(POST_REQUEST) as mocked_post:
            DEFAULT_LOADER.load(DUMMY_USER_PREFS_OBJ_PATHS)

            expected_data = read_json_data(DUMMY_JSON)

            self.assertEqual(mocked_post.call_count, 1)

            mocked_post.assert_has_calls(
                [call(DEFAULT_LOADER_CONFIG.user_prefs_obj_urls.user_preferences_url, json=expected_data)])

    def test_load_file_system_failure(self):
        with patch(POST_REQUEST) as mocked_post:
            with self.assertRaises(ValueError):
                DEFAULT_LOADER.load(BROKEN_USER_PREFS_OBJ_PATHS)

            mocked_post.assert_not_called()

    def test_load_data_post_failure(self):
        expected_data = read_json_data(DUMMY_JSON)

        with patch(POST_REQUEST,
                   new=MagicMock(side_effect=mocked_post_user_preferences_respond_500)) as mocked_post_fail_ref_net:
            ref_obj_phase_expected_calls = [
                call(DEFAULT_LOADER_CONFIG.user_prefs_obj_urls.user_preferences_url, json=expected_data)]
            with self.assertRaises(HTTPError):
                DEFAULT_LOADER.load(DUMMY_USER_PREFS_OBJ_PATHS)

            self.assertEqual(len(ref_obj_phase_expected_calls), mocked_post_fail_ref_net.call_count)

            mocked_post_fail_ref_net.assert_has_calls(ref_obj_phase_expected_calls)


class MockResponse:
    def __init__(self, status_code: int, reason: str, text: str):
        self.status_code = status_code
        self.reason = reason
        self.text = text

    @property
    def ok(self) -> bool:  # NOSONAR : this is mocking the Response.ok function, which doesn't follow sonar conventions
        if self.status_code < 400:
            return True
        else:
            return False


def mocked_post_user_preferences_respond_500(*args, **kwargs) -> MockResponse:
    if DEFAULT_LOADER_CONFIG.user_prefs_obj_urls.user_preferences_url in args[0]:
        return MockResponse(500, 'Server Error', 'Mocked server error')
    else:
        return MockResponse(200, 'OK', 'Successful')



if __name__ == '__main__':
    unittest.main()
