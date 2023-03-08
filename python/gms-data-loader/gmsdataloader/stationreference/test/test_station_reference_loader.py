import os
import unittest
from dataclasses import replace
from unittest.mock import patch, call, MagicMock

from requests import HTTPError

from gmsdataloader.genericdataloader import read_json_data
from gmsdataloader.stationreference import StationReferenceLoader
from gmsdataloader.stationreference import StationReferenceObjectPaths
from gmsdataloader.stationreference import StationReferenceMembershipPaths
from gmsdataloader.stationreference import StationReferenceLoaderConfig

POST_REQUEST = 'requests.post'

WORKING_DIR = os.path.dirname(__file__)

DUMMY_JSON = f'{WORKING_DIR}/resources/json/dummy.json'
BROKEN_JSON = f'{WORKING_DIR}/resources/json/broken.json'

DUMMY_INPUT_REF_OBJ_PATHS = StationReferenceObjectPaths(DUMMY_JSON, DUMMY_JSON, DUMMY_JSON, DUMMY_JSON, DUMMY_JSON)
DUMMY_INPUT_REF_MEMB_PATHS = StationReferenceMembershipPaths(DUMMY_JSON, DUMMY_JSON, DUMMY_JSON)

DEFAULT_LOADER_CONFIG = StationReferenceLoaderConfig('test-ref-loader.org',
                                                     f'{WORKING_DIR}/resources/config/test_config.ini')
DEFAULT_STA_REF_LOADER = StationReferenceLoader(DEFAULT_LOADER_CONFIG, total_retries=1)


class TestStationReferenceLoader(unittest.TestCase):

    def test_load_dummy_data(self):
        with patch(POST_REQUEST) as mocked_post:
            DEFAULT_STA_REF_LOADER.load(DUMMY_INPUT_REF_OBJ_PATHS, DUMMY_INPUT_REF_MEMB_PATHS)

            expected_data = read_json_data(DUMMY_JSON)

            self.assertEqual(mocked_post.call_count, 8)

            mocked_post.assert_has_calls(
                [call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_networks_url, json=expected_data),
                 call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_stations_url, json=expected_data),
                 call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_sites_url, json=expected_data),
                 call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_chans_url, json=expected_data),
                 call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_sensors_url, json=expected_data),
                 call(DEFAULT_LOADER_CONFIG.sta_ref_memb_urls.ref_network_memb_url, json=expected_data),
                 call(DEFAULT_LOADER_CONFIG.sta_ref_memb_urls.ref_station_memb_url, json=expected_data),
                 call(DEFAULT_LOADER_CONFIG.sta_ref_memb_urls.ref_site_memb_url, json=expected_data)],
                any_order=True)

    def test_load_file_system_failure(self):
        expected_data = read_json_data(DUMMY_JSON)

        with patch(POST_REQUEST) as mocked_post:
            # When we fail to post on the load reference objects step, we should stop and not publish the failed
            # reference object data or membership data
            ref_obj_phase_expected_calls = [
                call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_networks_url, json=expected_data),
                call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_sites_url, json=expected_data),
                call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_chans_url, json=expected_data),
                call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_sensors_url, json=expected_data)]

            with self.assertRaises(ValueError):
                DEFAULT_STA_REF_LOADER.load(
                    replace(DUMMY_INPUT_REF_OBJ_PATHS, ref_stations_path=BROKEN_JSON),
                    DUMMY_INPUT_REF_MEMB_PATHS)

            self.assertEqual(mocked_post.call_count, len(ref_obj_phase_expected_calls))
            mocked_post.assert_has_calls(ref_obj_phase_expected_calls, any_order=True)

            mocked_post.reset_mock()

            with self.assertRaises(OSError):
                DEFAULT_STA_REF_LOADER.load(replace(DUMMY_INPUT_REF_OBJ_PATHS, ref_stations_path=''),
                                            DUMMY_INPUT_REF_MEMB_PATHS)

            self.assertEqual(mocked_post.call_count, len(ref_obj_phase_expected_calls))
            mocked_post.assert_has_calls(ref_obj_phase_expected_calls, any_order=True)

            mocked_post.reset_mock()

            # When we fail to post on the load reference memberships step, we should only fail to publish the failed
            # membership data
            ref_mem_phase_expected_calls = [
                call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_networks_url, json=expected_data),
                call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_stations_url, json=expected_data),
                call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_sites_url, json=expected_data),
                call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_chans_url, json=expected_data),
                call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_sensors_url, json=expected_data),
                call(DEFAULT_LOADER_CONFIG.sta_ref_memb_urls.ref_station_memb_url, json=expected_data),
                call(DEFAULT_LOADER_CONFIG.sta_ref_memb_urls.ref_site_memb_url, json=expected_data)]

            with self.assertRaises(ValueError):
                DEFAULT_STA_REF_LOADER.load(DUMMY_INPUT_REF_OBJ_PATHS,
                                            replace(DUMMY_INPUT_REF_MEMB_PATHS, ref_net_memb_path=BROKEN_JSON))

            self.assertEqual(mocked_post.call_count, len(ref_mem_phase_expected_calls))
            mocked_post.assert_has_calls(ref_mem_phase_expected_calls, any_order=True)

            mocked_post.reset_mock()

            with self.assertRaises(OSError):
                DEFAULT_STA_REF_LOADER.load(DUMMY_INPUT_REF_OBJ_PATHS,
                                            replace(DUMMY_INPUT_REF_MEMB_PATHS, ref_net_memb_path=''))

            self.assertEqual(mocked_post.call_count, len(ref_mem_phase_expected_calls))
            mocked_post.assert_has_calls(ref_mem_phase_expected_calls, any_order=True)

    def test_load_data_post_failure(self):
        expected_data = read_json_data(DUMMY_JSON)

        with patch(POST_REQUEST,
                   new=MagicMock(side_effect=mocked_post_ref_networks_respond_500)) as mocked_post_fail_ref_net:
            # When we fail to post on the load reference objects step, we should stop and not publish membership data
            ref_obj_phase_expected_calls = [
                call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_networks_url, json=expected_data),
                call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_stations_url, json=expected_data),
                call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_sites_url, json=expected_data),
                call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_chans_url, json=expected_data),
                call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_sensors_url, json=expected_data)]
            with self.assertRaises(HTTPError):
                DEFAULT_STA_REF_LOADER.load(DUMMY_INPUT_REF_OBJ_PATHS, DUMMY_INPUT_REF_MEMB_PATHS)

            self.assertEqual(mocked_post_fail_ref_net.call_count, len(ref_obj_phase_expected_calls))

            mocked_post_fail_ref_net.assert_has_calls(
                ref_obj_phase_expected_calls,
                any_order=True)

        with patch(POST_REQUEST,
                   new=MagicMock(side_effect=mocked_post_ref_net_mem_respond_500)) as mocked_post_fail_net_mem:
            # When we fail to post on the load reference memberships step, we should expect POSTs to have been called
            # for all memberships, but an HTTPError to be raised
            ref_mem_phase_expected_calls = [
                call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_networks_url, json=expected_data),
                call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_stations_url, json=expected_data),
                call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_sites_url, json=expected_data),
                call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_chans_url, json=expected_data),
                call(DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_sensors_url, json=expected_data),
                call(DEFAULT_LOADER_CONFIG.sta_ref_memb_urls.ref_network_memb_url, json=expected_data),
                call(DEFAULT_LOADER_CONFIG.sta_ref_memb_urls.ref_station_memb_url, json=expected_data),
                call(DEFAULT_LOADER_CONFIG.sta_ref_memb_urls.ref_site_memb_url, json=expected_data)]
            with self.assertRaises(HTTPError):
                DEFAULT_STA_REF_LOADER.load(DUMMY_INPUT_REF_OBJ_PATHS, DUMMY_INPUT_REF_MEMB_PATHS)

            self.assertEqual(mocked_post_fail_net_mem.call_count, len(ref_mem_phase_expected_calls))

            mocked_post_fail_net_mem.assert_has_calls(
                ref_mem_phase_expected_calls,
                any_order=True)


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


def mocked_post_ref_networks_respond_500(*args, **kwargs) -> MockResponse:
    if DEFAULT_LOADER_CONFIG.sta_ref_obj_urls.ref_networks_url in args[0]:
        return MockResponse(500, 'Server Error', 'Mocked server error')
    else:
        return MockResponse(200, 'OK', 'Successful')


def mocked_post_ref_net_mem_respond_500(*args, **kwargs) -> MockResponse:
    if DEFAULT_LOADER_CONFIG.sta_ref_memb_urls.ref_network_memb_url in args[0]:
        return MockResponse(500, 'Server Error', 'Mocked server error')
    else:
        return MockResponse(200, 'OK', 'Successful')


if __name__ == '__main__':
    unittest.main()
