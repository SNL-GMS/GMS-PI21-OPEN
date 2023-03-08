import unittest
import os
from unittest.mock import patch, call

from gmsdataloader.mediumvelocity import MediumVelocityMinioConfig
from gmsdataloader.mediumvelocity import MediumVelocityLoader

WORKING_DIR = os.path.dirname(__file__)


class MediumVelocityLoaderTest(unittest.TestCase):

    def test_get_minio_prefix_map(self):
        with patch('gmsdataloader.mediumvelocity.MediumVelocityMinioConfig') as mock_config:
            loader = MediumVelocityLoader(f'{WORKING_DIR}/resources/mediumvelocity', mock_config)
            prefix_map = loader.get_minio_prefix_map()

            print(prefix_map)
            self.assertTrue('mediumvelocity/ak135' in prefix_map)
            self.assertTrue(
                f'{WORKING_DIR}/resources/mediumvelocity/ak135.json' in prefix_map['mediumvelocity/ak135']
            )

    def test_load(self):
        with patch('minio.Minio') as client_mocker:
            mock_client = client_mocker.return_value
            mock_client.bucket_exists.return_value = False
            config = MediumVelocityMinioConfig('http://non-existent-minio-service', 'testbucket', minio_client=mock_client)
            loader = MediumVelocityLoader(f'{WORKING_DIR}/resources/mediumvelocity', config)

            loader.load()
            mock_client.bucket_exists.assert_called_once_with('testbucket')
            mock_client.make_bucket.assert_called_once_with('testbucket')
            mock_client.fput_object.assert_has_calls([call('testbucket', 'mediumvelocity/ak135', f'{WORKING_DIR}/resources/mediumvelocity/ak135.json')])


if __name__ == '__main__':
    unittest.main()
