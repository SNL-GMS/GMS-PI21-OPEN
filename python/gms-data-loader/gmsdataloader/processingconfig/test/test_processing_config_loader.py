import json
import os
import unittest
from unittest.mock import patch, call, MagicMock

import requests

from gmsdataloader.processingconfig import Configuration
from gmsdataloader.processingconfig import ProcessingConfigLoader

URL = "http://127.0.0.1:8080"  #NOSONAR - this does not need to be configurable since it is just for this test

WORKING_DIR = os.path.dirname(__file__)

DEFAULT_PROC_CFG_LOADER = ProcessingConfigLoader(url=URL, processing_config_root=f'{WORKING_DIR}/resources/json')


class TestProcessingConfigLoader(unittest.TestCase):
    constraint2_1 = 'test_constraint2.1'
    constraint1_2 = 'test_constraint1.2'
    constraint1_1 = 'test_constraint1.1'
    config_json = 'config.json'

    def test_bad_processing_cfg_root(self):
        proc_cfg_loader = ProcessingConfigLoader(url=URL, processing_config_root="not a directory")
        with self.assertRaises(ValueError):
            proc_cfg_loader.load()

    def test_get_config_files_single_config_in_one_file(self):
        config_dict = DEFAULT_PROC_CFG_LOADER.get_config_files()
        first_key = next(iter(config_dict.keys()))
        first_value = next(iter(config_dict.values()))
        self.assertEqual(first_key, 'test_config')
        self.assertEqual(first_value[0], {'name': self.config_json, 'path': f'{WORKING_DIR}/resources/json/test_config/config.json', 'override': False})

    def test_create_configurations_single_config_in_one_file(self):
        config_dict = {'test_config': [{'name': self.config_json, 'path': f'{WORKING_DIR}/resources/json/test_config/config.json', 'override': False}]}
        cfg_list = DEFAULT_PROC_CFG_LOADER.create_configurations(config_dict)
        self.assertEqual(len(cfg_list), 1)
        test_config = cfg_list[0]
        self.assertEqual(test_config.get_name(), 'test_config')
        self.assertEqual(len(test_config.configurationOptions), 1)

        self.verify_config_option(test_config.configurationOptions[0], 'test_constraint', 1)

    def test_get_config_files_multiple_configs_in_one_file(self):
        DEFAULT_PROC_CFG_LOADER = ProcessingConfigLoader(url=URL, processing_config_root=f'{WORKING_DIR}/resources/multiple_configurations_in_one_file')
        config_dict = DEFAULT_PROC_CFG_LOADER.get_config_files()
        first_key = next(iter(config_dict.keys()))
        first_value = next(iter(config_dict.values()))
        self.assertEqual(first_key, 'multi_config')
        self.assertEqual(first_value[0], {'name': self.config_json, 'path': f'{WORKING_DIR}/resources/multiple_configurations_in_one_file/multi_config/config.json', 'override': False})

    def test_create_configurations_multiple_configs_in_one_file(self):
        config_dict = {'multi_config': [{'name': self.config_json, 'path': f'{WORKING_DIR}/resources/multiple_configurations_in_one_file/multi_config/config.json', 'override': False}]}

        cfg_list = DEFAULT_PROC_CFG_LOADER.create_configurations(config_dict)

        self.assertEqual(len(cfg_list), 1)
        multi_config = cfg_list[0]
        self.assertEqual(multi_config.get_name(), 'multi_config')
        self.assertEqual(len(multi_config.configurationOptions), 3)
        self.verify_config_option(multi_config.configurationOptions[0], self.constraint1_1, 1)
        self.verify_config_option(multi_config.configurationOptions[1], self.constraint1_2, 2)
        self.verify_config_option(multi_config.configurationOptions[2], self.constraint2_1, 1)

    def test_get_config_files_mixed_configs_in_multiple_files(self):
        DEFAULT_PROC_CFG_LOADER = ProcessingConfigLoader(url=URL, processing_config_root=f'{WORKING_DIR}/resources/mixed_configurations_in_multiple_files')

        config_dict = DEFAULT_PROC_CFG_LOADER.get_config_files()

        keys = iter(sorted(config_dict.keys()))
        key = next(keys)
        self.assertEqual(key, 'multi_config')
        self.assertTrue({'name': self.config_json,
                         'path': f'{WORKING_DIR}/resources/mixed_configurations_in_multiple_files/multi_config/config.json',
                         'override': False} in config_dict[key])
        key = next(keys)
        self.assertEqual(key, 'test_config')
        self.assertTrue({'name': self.config_json,
                         'path': f'{WORKING_DIR}/resources/mixed_configurations_in_multiple_files/test_config/config.json',
                         'override': False} in config_dict[key])

    def test_create_configurations_mixed_configs_in_multiple_files(self):
      DEFAULT_PROC_CFG_LOADER = ProcessingConfigLoader(url=URL, processing_config_root=f'{WORKING_DIR}/resources/mixed_configurations_in_multiple_files')
      config_dict = DEFAULT_PROC_CFG_LOADER.get_config_files()

      cfg_list = DEFAULT_PROC_CFG_LOADER.create_configurations(config_dict)

      self.assertEqual(len(cfg_list), 2)

      test_config = cfg_list[0]
      self.assertEqual(test_config.get_name(), 'multi_config')
      self.assertEqual(len(test_config.configurationOptions), 6)
      sorted_config_options=sorted(test_config.configurationOptions, key=lambda option: option["name"])
      self.verify_config_option(sorted_config_options[0], self.constraint1_1, 1)
      self.verify_config_option(sorted_config_options[1], self.constraint1_2, 2)
      self.verify_config_option(sorted_config_options[2], self.constraint2_1, 1)
      self.verify_config_option(sorted_config_options[3], 'test_constraint3.1', 1)
      self.verify_config_option(sorted_config_options[4], 'test_constraint3.2', 2)
      self.verify_config_option(sorted_config_options[5], 'test_constraint4.1', 1)

      test_config = cfg_list[1]
      self.assertEqual(len(test_config.configurationOptions), 2)
      sorted_config_options = sorted(test_config.configurationOptions, key=lambda option: option["name"])
      self.verify_config_option(sorted_config_options[0], 'another_test_constraint', 1)
      self.verify_config_option(sorted_config_options[1], 'test_constraint', 1)

    def test_get_config_files_mixed_configs_in_one_direcotry(self):
        DEFAULT_PROC_CFG_LOADER = ProcessingConfigLoader(url=URL, processing_config_root=f'{WORKING_DIR}/resources/mixed_configurations_in_same_direcotry')

        config_dict = DEFAULT_PROC_CFG_LOADER.get_config_files()

        keys = iter(sorted(config_dict.keys()))
        key = next(keys)
        self.assertEqual(key, 'multi_config')
        self.assertTrue({'name': self.config_json,
                         'path': f'{WORKING_DIR}/resources/mixed_configurations_in_same_direcotry/multi_config/config.json',
                         'override': False} in config_dict[key])

    def test_create_configurations_mixed_configs_in_one_direcotry(self):
      DEFAULT_PROC_CFG_LOADER = ProcessingConfigLoader(url=URL, processing_config_root=f'{WORKING_DIR}/resources/mixed_configurations_in_same_direcotry')
      config_dict = DEFAULT_PROC_CFG_LOADER.get_config_files()

      cfg_list = DEFAULT_PROC_CFG_LOADER.create_configurations(config_dict)

      self.assertEqual(len(cfg_list), 1)

      test_config = cfg_list[0]
      self.assertEqual(test_config.get_name(), 'multi_config')
      self.assertEqual(len(test_config.configurationOptions), 4)

      sorted_config_options=sorted(test_config.configurationOptions, key=lambda option: option["name"])
      self.verify_config_option(sorted_config_options[0], 'another_test_constraint', 1)
      self.verify_config_option(sorted_config_options[1], self.constraint1_1, 1)
      self.verify_config_option(sorted_config_options[2], self.constraint1_2, 2)
      self.verify_config_option(sorted_config_options[3], self.constraint2_1, 1)

    def test_post_cfgs(self):
        cfg = Configuration("test")

        # this time is read in from the test/resources/json/test_config/config.json file
        cfg.change_time = 1583881197.399642
        cfg_list = [cfg]
        json_list = [json.loads(json.dumps(cfg.__dict__))]

        requests.Session.post = MagicMock('post')

        with patch('requests.Session.post') as mocked_post:
            mock_response = requests.Response
            mock_response.status_code = 200
            mocked_post.return_value = mock_response

            DEFAULT_PROC_CFG_LOADER.post_cfgs(cfg_list)
            self.assertEqual(mocked_post.call_count, 1)
            mocked_post.assert_has_calls(
                [call(url=f'{URL}/processing-cfg/put-all', json=json_list,
                      headers={'Content-Type': 'application/json', 'Accept': 'application/json'})])

    def test_load(self ):
        cfg = Configuration("test")
        data = json.loads(
            '{"name": "test", "configuration_options": [{"name": "test_constraint", "constraints": [{"constraintType": "DEFAULT"}], "parameters": {"defaultNetwork": "demo"}}]}')
        cfg.configurationOptions.append(data)


        with patch('requests.Session.post') as mocked_post:
            mock_response = requests.Response
            mock_response.status_code = 200
            mocked_post.return_value = mock_response

            DEFAULT_PROC_CFG_LOADER.load()

            mocked_post.assert_called_once()

    def test_bad_filetype(self):
        proc_cfg_loader = ProcessingConfigLoader(url=URL, processing_config_root=f'{WORKING_DIR}/resources/bad_filetype')

        config_dict = {'test_config': [{'name': 'bad_filetype.txt',
                                        'path': f'{WORKING_DIR}/resources/bad_filetype/test_config/bad_filetype.txt',
                                        'override': False}]}
        with self.assertRaises(ValueError):
            proc_cfg_loader.create_configurations(config_dict)

    def test_invalid_input(self):
        proc_cfg_loader = ProcessingConfigLoader(url=URL, processing_config_root=f'{WORKING_DIR}/resources/invalid_input')
        config_dict = {'test_config': [{'name': 'invalid_input.json',
                                        'path': f'{WORKING_DIR}/resources/invalid_input/test_config/invalid_input.json',
                                        'override': False}]}
        with self.assertRaises(ValueError):
            proc_cfg_loader.create_configurations(config_dict)

    def verify_config_option(self, configuration_option, option_name: str, constraint_count: int):
      self.assertEqual(configuration_option['name'], option_name)
      constraints = configuration_option['constraints']
      self.assertEqual(len(constraints), constraint_count)
      for i in range(constraint_count):
        self.assertEqual(constraints[i-1]['constraintType'], 'DEFAULT')
      parameters = configuration_option['parameters']
      self.assertEqual(parameters['defaultNetwork'], 'demo')


if __name__ == '__main__':
  unittest.main()
