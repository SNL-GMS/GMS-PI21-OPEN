import os
import unittest

from testcontainers.compose import DockerCompose

from gmsdataloader.processingconfig import ProcessingConfigLoader
from gmsdataloader.stationprocessing import StationProcessingLoader
from gmsdataloader.stationprocessing import StationProcessingLoaderConfig
from gmsdataloader.stationreference import StationReferenceLoader
from gmsdataloader.stationreference import StationReferenceLoaderConfig
from gmsdataloader.stationreference import StationReferenceMembershipPaths
from gmsdataloader.stationreference import StationReferenceObjectPaths
from gmsdataloader.userpreferences import UserPreferencesLoader
from gmsdataloader.userpreferences import UserPreferencesLoaderConfig

WORKING_DIR = os.path.dirname(__file__)
RESOURCES_DIR = f'{WORKING_DIR}/resources'
ALIVE = '/alive'


class TestOsdDataLoadDocker(unittest.TestCase):
    compose = None

    @classmethod
    def setUpClass(cls):
        cls.compose = DockerCompose(f'{RESOURCES_DIR}/docker', compose_file_name='docker-compose.yml')
        cls.compose.start()
        cls.osd_url = f'http://localhost:{cls.compose.get_service_port("frameworks-osd-service", 8080)}/osd'
        cls.config_service_url = 'http://localhost:' + cls.compose.get_service_port('frameworks-configuration-service',
                                                                                    8080)

    @classmethod
    def tearDownClass(cls):
        cls.compose.stop()

    def test_post_ref_data_docker(self):
        station_ref_object_paths = StationReferenceObjectPaths(
            ref_networks_path=f'{RESOURCES_DIR}/json/starefMK01/reference-network.json',
            ref_stations_path=f'{RESOURCES_DIR}/json/starefMK01/reference-station.json',
            ref_chans_path=f'{RESOURCES_DIR}/json/starefMK01/reference-channel.json',
            ref_sites_path=f'{RESOURCES_DIR}/json/starefMK01/reference-site.json',
            ref_sensors_path=f'{RESOURCES_DIR}/json/starefMK01/reference-sensor.json')
        station_ref_memb_paths = StationReferenceMembershipPaths(
            ref_net_memb_path=f'{RESOURCES_DIR}/json/starefMK01/reference-network-membership.json',
            ref_sta_memb_path=f'{RESOURCES_DIR}/json/starefMK01/reference-station-membership.json',
            ref_site_memb_path=f'{RESOURCES_DIR}/json/starefMK01/reference-site-membership.json')

        sta_ref_loader = StationReferenceLoader(StationReferenceLoaderConfig(
            base_url=self.osd_url,
            config_path=f'{RESOURCES_DIR}/config/test_staref_config.ini'))

        self.compose.wait_for(self.osd_url.replace('/osd', ALIVE))
        sta_ref_loader.load(station_ref_object_paths, station_ref_memb_paths)

    def test_post_proc_data_docker(self):
        sta_proc_loader = StationProcessingLoader(StationProcessingLoaderConfig(
            base_url=self.osd_url,
            config_path=f'{RESOURCES_DIR}/config/test_staproc_config.ini'))

        self.compose.wait_for(self.osd_url.replace('/osd', ALIVE))
        sta_proc_loader.load(
            station_groups_path=f'{RESOURCES_DIR}/json/stagroupCd11MKAR/station_group.json')

    def test_post_user_prefs_docker(self):
        user_prefs_loader = UserPreferencesLoader(UserPreferencesLoaderConfig(
            self.osd_url,
            f'{RESOURCES_DIR}/config/test_userprefs_config.ini'
        ))

        self.compose.wait_for(self.osd_url.replace('/osd', ALIVE))
        user_prefs_loader.load_user_preferences(
            f'{RESOURCES_DIR}/json/defaultUserPreferences/user_preferences.json')

    def test_post_processing_config_data(self):
        loader = ProcessingConfigLoader(
            url=self.config_service_url,
            processing_config_root=f'{WORKING_DIR}/resources/config/processing')
        self.compose.wait_for(self.config_service_url + ALIVE)
        loaded = loader.load()
        self.assertTrue(loaded)

    def test_post_processing_config_data_mixed_format(self):
        loader = ProcessingConfigLoader(
            url=self.config_service_url,
            processing_config_root=f'{WORKING_DIR}/resources/config/processing_mixed_format')
        self.compose.wait_for(self.config_service_url + ALIVE)
        loaded = loader.load()
        self.assertTrue(loaded)

    def test_update_station_groups(self):
        sta_proc_loader = StationProcessingLoader(StationProcessingLoaderConfig(
            base_url=self.osd_url,
            config_path=f'{RESOURCES_DIR}/config/test_staproc_config.ini'))

        self.compose.wait_for(self.osd_url.replace('/osd', ALIVE))
        sta_proc_loader.load_sta_group_updates(
            station_group_definitions_path=f'{RESOURCES_DIR}/json/stagroupCd11MKAR/station_group_definition.json')


if __name__ == '__main__':
    unittest.main()
