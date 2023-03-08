import unittest
import os
from unittest.mock import patch, call, MagicMock

from gmsdataloader.earthmodels import EarthModelMinioConfig
from gmsdataloader.earthmodels import EarthModelsLoader

WORKING_DIR = os.path.dirname(__file__)


class EarthModelLoaderTest(unittest.TestCase):

    def test_get_minio_prefix_map(self):
        with patch('gmsdataloader.earthmodels.EarthModelMinioConfig') as mock_config:
            loader = EarthModelsLoader(f'{WORKING_DIR}/resources', mock_config)
            prefix_map = loader.get_minio_prefix_map()
            self.assertTrue('traveltime/iaspei' in prefix_map)
            self.assertTrue('traveltime/ak135' in prefix_map)

            self.assertTrue(
                {'phase': 'S', 'path': f'{WORKING_DIR}/resources/traveltime/iaspei/S.json'} in prefix_map[
                    'traveltime/iaspei']
            )

            self.assertTrue(
                {'phase': 'P', 'path': f'{WORKING_DIR}/resources/traveltime/ak135/P.json'} in prefix_map[
                    'traveltime/ak135']
            )
            self.assertTrue(
                {'phase': 'sP', 'path': f'{WORKING_DIR}/resources/traveltime/ak135/~sP.json'} in prefix_map[
                    'traveltime/ak135']
            )

    def test_load(self):
        with patch('minio.Minio') as client_mocker:
            mock_client = client_mocker.return_value
            mock_client.bucket_exists.return_value = False
            config = EarthModelMinioConfig('http://non-existent-minio-service', 'testbucket', minio_client=mock_client)
            loader = EarthModelsLoader(f'{WORKING_DIR}/resources', config)

            loader.load()
            mock_client.bucket_exists.assert_called_once_with('testbucket')
            mock_client.make_bucket.assert_called_once_with('testbucket')
            mock_client.fput_object.assert_has_calls(
                [call('testbucket', 'traveltime/ak135/P', f'{WORKING_DIR}/resources/traveltime/ak135/P.json'),
                 call('testbucket', 'traveltime/ak135/sP', f'{WORKING_DIR}/resources/traveltime/ak135/~sP.json'),
                 call('testbucket', 'traveltime/iaspei/S', f'{WORKING_DIR}/resources/traveltime/iaspei/S.json')],
                any_order=True
            )


if __name__ == '__main__':
    unittest.main()
