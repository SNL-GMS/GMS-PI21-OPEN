#!/usr/local/bin python3

import argparse
import sys

from gmsdataloader.processingconfig import ProcessingConfigLoader
from gmsdataloader.stationprocessing import StationProcessingLoader
from gmsdataloader.stationprocessing import StationProcessingLoaderConfig

from gmsdataloader.stationreference import StationReferenceLoader
from gmsdataloader.stationreference import StationReferenceObjectPaths
from gmsdataloader.stationreference import StationReferenceMembershipPaths
from gmsdataloader.stationreference import StationReferenceLoaderConfig

from gmsdataloader.userpreferences import UserPreferencesLoader
from gmsdataloader.userpreferences import UserPreferencesLoaderConfig

OSD_URL_HELP = 'OSD base URL to publish data to'
PYTHON_CONFIG_HELP = 'Specify an alternate loader config (Python INI)'


def setup_station_reference_parser(parser: argparse.ArgumentParser):
    parser.add_argument('-u', '--url', help=OSD_URL_HELP, required=True)
    parser.add_argument('-c', '--config', help=PYTHON_CONFIG_HELP, required=False, default=None)
                        # TODO: remove default='dataloaders/stationreference/resources/config/config.ini')
    parser.add_argument('--ref_networks', help='Path to Reference Networks Array (JSON)', required=False,
                        default='stationdata/reference-network.json')
    parser.add_argument('--ref_stations', help='Path to Reference Stations Array (JSON)', required=False,
                        default='stationdata/reference-station.json')
    parser.add_argument('--ref_sites', help='Path to Reference Sites Array (JSON)', required=False,
                        default='stationdata/reference-site.json')
    parser.add_argument('--ref_chans', help='Path to Reference Channels Array (JSON)', required=False,
                        default='stationdata/reference-channel.json')
    parser.add_argument('--ref_sensors', help='Path to Reference Sensors Array (JSON)', required=False,
                        default='stationdata/reference-sensor.json')
    parser.add_argument('--ref_net_memberships', help='Path to Reference Network Memberships Array (JSON)',
                        required=False, default='stationdata/reference-network-memberships.json')
    parser.add_argument('--ref_sta_memberships', help='Path to Reference Station Memberships Array (JSON)',
                        required=False, default='stationdata/reference-station-memberships.json')
    parser.add_argument('--ref_site_memberships', help='Path to Reference Site Memberships Array (JSON)',
                        required=False, default='stationdata/reference-site-memberships.json')
    parser.set_defaults(action=load_station_reference)


def load_station_reference(args: argparse.Namespace):
    loader = StationReferenceLoader(StationReferenceLoaderConfig(args.url, args.config))

    sta_ref_obj_paths = StationReferenceObjectPaths(ref_networks_path=args.ref_networks,
                                                    ref_stations_path=args.ref_stations, ref_sites_path=args.ref_sites,
                                                    ref_chans_path=args.ref_chans, ref_sensors_path=args.ref_sensors)
    sta_ref_memb_paths = StationReferenceMembershipPaths(ref_net_memb_path=args.ref_net_memberships,
                                                         ref_sta_memb_path=args.ref_sta_memberships,
                                                         ref_site_memb_path=args.ref_site_memberships)
    loader.load(sta_ref_obj_paths, sta_ref_memb_paths)


def setup_station_processing_parser(parser: argparse.ArgumentParser):
    parser.add_argument('-u', '--url', help=OSD_URL_HELP, required=True)
    parser.add_argument('-c', '--config', help=PYTHON_CONFIG_HELP, required=False, default=None)
                        # TODO: remove default='dataloaders/stationprocessing/resources/config/config.ini')
    parser.add_argument('--station_groups', help='Path to Station Groups Array (JSON)', required=False,
                        default='stationdata/processing-station-group.json')
    parser.set_defaults(action=load_station_processing)


def load_station_processing(args: argparse.Namespace):
    loader = StationProcessingLoader(StationProcessingLoaderConfig(args.url, args.config))
    loader.load(station_groups_path=args.station_groups)


def setup_user_preferences_parser(parser: argparse.ArgumentParser):
    parser.add_argument('-u', '--url', help=OSD_URL_HELP, required=True)
    parser.add_argument('-c', '--config', help=PYTHON_CONFIG_HELP, required=False, default=None)
                        # TODO remove: default='dataloaders/userpreferences/resources/config/config.ini')
    parser.add_argument('-f', '--file', help='Specify an alternate user preferences file (JSON)', required=False,
                        default='resources/defaultUserPreferences.json')
    parser.set_defaults(action=load_user_preferences)


def load_user_preferences(args: argparse.Namespace):
    url = args.url
    config_file = args.config
    preferences_file = args.file

    config = UserPreferencesLoaderConfig(url, config_file)
    loader = UserPreferencesLoader(config)

    loader.load_user_preferences(preferences_file)


def setup_processing_config_parser(parser: argparse.ArgumentParser):
    parser.add_argument('--processing-configuration-root',
                        help='Path to Root directory containing Processing Configurations (optional)',
                        required=False,
                        default='config/processing')
    parser.add_argument('-u', '--url', help='Path to location of frameworks configuration service', required=True)
    parser.set_defaults(action=load_processing_config)


def load_processing_config(args: argparse.Namespace):
    loader = ProcessingConfigLoader(args.url, args.processing_configuration_root)
    loader.load()


def setup_station_group_update_parser(parser: argparse.ArgumentParser):
    parser.add_argument('-u', '--url', help=OSD_URL_HELP, required=True)
    parser.add_argument('-f', '--file', help='Path to Station Group Definition Array (JSON)',
                        required=True)
    parser.add_argument('-c', '--config', help=PYTHON_CONFIG_HELP, required=False,
                        default='dataloaders/stationprocessing/resources/config/config.ini')
    parser.set_defaults(action=update_station_groups)


def update_station_groups(args: argparse.Namespace):
    loader = StationProcessingLoader(StationProcessingLoaderConfig(args.url, args.config))
    loader.load_sta_group_updates(station_group_definitions_path=args.file)


if __name__ == '__main__':
    args_parser = argparse.ArgumentParser(prog='gms-data-loader')
    command_parser = args_parser.add_subparsers()

    setup_station_reference_parser(command_parser.add_parser('load-station-reference', aliases=['lsr'],
                                                             help='Load Station Reference Information into the OSD'))
    setup_station_processing_parser(command_parser.add_parser('load-station-processing', aliases=['lsp'],
                                                              help='Load Station Processing Data into the OSD'))
    setup_user_preferences_parser(command_parser.add_parser('load-user-preferences', aliases=['lup'],
                                                            help='Load User Preferences into the OSD'))
    setup_processing_config_parser(command_parser.add_parser('load-processing-config', aliases=['lpc'],
                                                             help='Load Processing Configuration into the Configuration Service'))

    setup_station_group_update_parser(command_parser.add_parser('update-station-groups', aliases=['usg'],
                                                                help='Update Station Groups in the OSD using Station Group Definitions'))
    if len(sys.argv) <= 1:
        args_parser.print_help()
    else:
        args = args_parser.parse_args(sys.argv[1:])
        args.action(args)
