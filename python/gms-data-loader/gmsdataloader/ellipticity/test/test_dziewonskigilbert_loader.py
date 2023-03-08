import unittest
import os
from unittest import mock
from unittest.mock import patch, call

from gmsdataloader.ellipticity import DziewonskiGilbertMinioConfig
from gmsdataloader.ellipticity import DziewonskiGilbertLoader

WORKING_DIR = os.path.dirname(__file__)


class DziewonskiGilbertLoaderTest(unittest.TestCase):

    def test_load(self):
        with patch('minio.Minio') as client_mocker:
            mock_client = client_mocker.return_value
            mock_client.bucket_exists.return_value = False
            config = DziewonskiGilbertMinioConfig('http://non-existent-minio-service', 'testbucket',
                                                  minio_client=mock_client)
            loader = DziewonskiGilbertLoader(f'{WORKING_DIR}/resources/dziewonski-gilbert', config)

            self.assertIsNotNone(loader, "Ooops, loader is none!")
            
            loader.load()

            mock_client.bucket_exists.assert_called_once_with('testbucket')
            mock_client.make_bucket.assert_called_once_with('testbucket')

            mock_client.put_object.assert_has_calls([
                call('testbucket', 'dziewonski-gilbert/P',
                     mock.ANY, mock.ANY),
                call('testbucket', 'dziewonski-gilbert/PP',
                     mock.ANY, mock.ANY),
                call('testbucket', 'dziewonski-gilbert/sP',
                     mock.ANY, mock.ANY),
                call('testbucket', 'dziewonski-gilbert/SKKPab',
                     mock.ANY, mock.ANY),                     
            ], any_order=True)


if __name__ == '__main__':
    unittest.main()
