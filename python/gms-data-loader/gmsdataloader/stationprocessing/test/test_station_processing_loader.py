import os
import unittest
from unittest.mock import patch, call

from gmsdataloader.genericdataloader import read_json_data
from gmsdataloader.stationprocessing import StationProcessingLoader
from gmsdataloader.stationprocessing import StationProcessingLoaderConfig

WORKING_DIR = os.path.dirname(__file__)

DUMMY_JSON = f'{WORKING_DIR}/resources/json/dummy.json'
BROKEN_JSON = f'{WORKING_DIR}/resources/json/broken.json'

DEFAULT_LOADER_CONFIG = StationProcessingLoaderConfig('test-proc-loader.org',
                                                      f'{WORKING_DIR}/resources/config/test_config.ini')
DEFAULT_STA_PROC_LOADER = StationProcessingLoader(DEFAULT_LOADER_CONFIG, total_retries=1)


class TestStationProcessingLoader(unittest.TestCase):

    def test_load_dummy_data(self):
        with patch('requests.post') as mocked_post:
            DEFAULT_STA_PROC_LOADER.load(DUMMY_JSON)

            expected_data = read_json_data(DUMMY_JSON)

            self.assertEqual(mocked_post.call_count, 1)

            mocked_post.assert_has_calls(
                [call(DEFAULT_LOADER_CONFIG.station_groups_new_url, json=expected_data)])

            mocked_post.reset_mock()

            DEFAULT_STA_PROC_LOADER.load_sta_group_updates(DUMMY_JSON)

            expected_data = read_json_data(DUMMY_JSON)

            self.assertEqual(mocked_post.call_count, 1)

            mocked_post.assert_has_calls(
                [call(DEFAULT_LOADER_CONFIG.station_groups_update_url, json=expected_data)])

    def test_load_file_system_failure(self):
        with patch('requests.post') as mocked_post:
            # When we fail to read data for station groups, no data should be posted
            with self.assertRaises(ValueError):
                DEFAULT_STA_PROC_LOADER.load(BROKEN_JSON)

            with self.assertRaises(OSError):
                DEFAULT_STA_PROC_LOADER.load('')

            mocked_post.assert_not_called()

            mocked_post.reset_mock()

            with self.assertRaises(ValueError):
                DEFAULT_STA_PROC_LOADER.load_sta_group_updates(BROKEN_JSON)

            with self.assertRaises(OSError):
                DEFAULT_STA_PROC_LOADER.load_sta_group_updates('')

            mocked_post.assert_not_called()
