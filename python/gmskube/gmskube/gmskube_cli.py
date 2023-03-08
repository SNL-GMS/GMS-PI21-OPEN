#!/usr/bin/env python3

import argparse
import logging
import os
import re
import sys
import traceback
from signal import signal, SIGINT
from types import FrameType
from argparse import ArgumentParser, Namespace

from rich.console import Console

from gmskube import gmskube

console_kwargs = {"log_path": False}
if os.getenv("CI"):
    console_kwargs["force_terminal"] = True
if os.getenv("RICH_LOG_PATH"):
    console_kwargs["log_path"] = True
console = Console(**console_kwargs)

TYPES = ['soh', 'ian', 'logging', 'sb', 'config']


def main() -> None:
    parser = get_parser()
    args = get_args(parser)

    # configure logging - make sure this comes before any call to logging
    # remove any existing logging handlers that may have been setup in imports
    while len(logging.root.handlers):
        logging.root.removeHandler(logging.root.handlers[-1])
    logging.basicConfig(format='[%(levelname)s] %(message)s', level=getattr(logging, args.verbose))
    # capture any messages from the warnings module
    logging.captureWarnings(True)

    # save kubectl context into a file if the env var is set
    if 'KUBECTL_CONTEXT' in os.environ:  # pragma: no branch
        logging.debug('KUBECTL_CONTEXT is set, saving file')
        # save the context into /kubeconfig/config where $KUBECONFIG env var is set in the dockerfile
        # Hard coding the path instead of using the env var to prevent fortify finding
        with open('/kubeconfig/config', "w") as kube_file:
            print(f"{os.getenv('KUBECTL_CONTEXT')}", file=kube_file)
        os.chmod('/kubeconfig/config', 0o600)

    # set SSL cert path for python requests library
    if 'REQUESTS_CA_BUNDLE' not in os.environ:  # pragma: no coverage
        # this path is the default for centos/ubi
        os.environ['REQUESTS_CA_BUNDLE'] = '/etc/pki/tls/certs/ca-bundle.crt'

    # print debug arguments
    logging.debug('Arguments:')
    for arg in vars(args):
        logging.debug(f"    {arg} = {getattr(args, arg) or ''}")

    # print out the entire env for debug
    logging.debug('Environment:\n' + '\n'.join([f'        {key}={value}' for key, value in sorted(os.environ.items())]))

    # call appropriate function if a command was specified, otherwise just print help.
    if hasattr(args, 'command'):
        try:
            gmskube.check_kubernetes_connection()
            args.command(args)
        except Exception as ex:  # pragma no coverage
            console.log(fr'[bold red]\[ERROR] {ex}')
            traceback.print_exc()
            sys.exit(1)
    else:
        help_command(parser)


def get_parser() -> ArgumentParser:
    # Get main argparse parser.
    # Any time new arguments are added, be sure to regenerate the bash_completion:
    #  `shtab gmskube.gmskube_cli.get_parser > bash_completion`

    description = """
description:
  The gmskube command-line program is used to install and configure instances
  of the GMS (Geophysical Monitoring System) system on Kubernetes.

  Each "instance" is an install of a multi-container application that is
  managed as a single unit and runs on a Kubernetes cluster. Each instance is
  contained within its own namespace in Kubernetes.  Various predefined types
  of instances are available.

  Some example instance types would be 'soh', 'ian', 'logging', or 'sb'.

  Multiple copies of 'soh' type instance may be run simultaneously. Each
  instance must be given a unique name to identify it as well as distinguish it
  from other running instances of the same type.

  For example, one instance of 'soh' may be running as 'develop' while another
  instance of 'soh' may be running as 'integration'.

  Different versions of a instance type may be available from the configured
  Docker registry. Released versions of GMS are tagged with a specific version
  number. During development this would correspond to a tag name on the docker images.

configuration:
  Before you can run gmskube, you must first download a Kubeconfig bundle from
  the cluster, and have the kubectl context set to the correct cluster.

  1. Login to Rancher
  2. Click the cluster name
  3. In the upper right, click the blue Kubeconfig File button
  4. Copy/Paste the contents into ~/.kube/config on your development machine
  5. If you have kubectl installed, the KUBECONFIG environment variable should
     already be set.  If not, set KUBECONFIG=~/config

commands:
  See the --help for details of each command.

examples:
  Get usage help for the gmskube tool:
    $ gmskube --help

  Install a SOH deployment of the default tag, with name 'my-test':
    $ gmskube install --type soh my-test

  Install a SOH deployment of the tag 'tag123', with the name 'my-test':
    $ gmskube install --type soh --tag tag123 my-test
"""
    parser = argparse.ArgumentParser(description=description,
                                     formatter_class=argparse.RawDescriptionHelpFormatter,
                                     prog='gmskube')

    # top level arguments
    parser.add_argument('-v', '--verbose', default='INFO', action='store_const', const='DEBUG',
                        help='Enable debug level output.')
    parser.add_argument('--timeout', type=int, default=4,
                        help='Specify the max time in minutes (integer) that gmskube should wait '
                             'for various actions to complete.')

    # Parent parsers contains common arguments that can be reused when adding a parser
    # Only add a parent parser if it will be used in more than one command. Otherwise
    # just add it directly to the command.

    # parent name parser
    parent_name_parser = argparse.ArgumentParser(add_help=False)
    parent_name_parser.add_argument('name', help='Name of the instance')

    # parent tag parser
    parent_tag_parser = argparse.ArgumentParser(add_help=False)
    parent_tag_parser.add_argument('--tag', required=True, type=argparse_tag_name_type,
                                   help='Tag name, which corresponds to the docker tag of the images. '
                                        'The value entered will automatically be transformed according to the '
                                        'definition of the gitlab CI_COMMIT_REF_SLUG variable definition '
                                        '(lowercase, shortened to 63 characters, and with everything except '
                                        '`0-9` and `a-z` replaced with `-`, no leading / trailing `-`).')

    # parent set parser
    parent_set_parser = argparse.ArgumentParser(add_help=False)
    parent_set_parser.add_argument('--set', dest='sets', type=argparse_set_type, action='append',
                                   help='Set a value in the chart to the specified value.  May be specified '
                                        'multiple times for different values.  Examples: `--set foo=bar` to '
                                        'set value `foo` to `bar`.  `--set global.env.GLOBAL_VAR=Hello` to set the '
                                        '`GLOBAL_VAR` environment variable to `Hello` in all application Pods '
                                        'within the instance.  `--set cd11-connman.env.CONNMAN_VAR=World` to '
                                        'set the `CONNMAN_VAR` environment var to `World` only in the '
                                        '`cd11-connman` app\'s Pod. `--set bastion.replicas=0` to set the '
                                        '`replicas` chart value in the bastion chart to `0`.')

    # parent set-string parser
    parent_set_string_parser = argparse.ArgumentParser(add_help=False)
    parent_set_string_parser.add_argument('--set-string', dest='set_strings', type=argparse_set_type, action='append',
                                          help='Similar to `--set` but forces a string value.')

    # parent values parser
    parent_values_parser = argparse.ArgumentParser(add_help=False)
    parent_values_parser.add_argument('--values',
                                      help='Set override values in the chart using a yaml file. The chart '
                                           '`values.yaml` is always included first, existing values second '
                                           '(for upgrade), followed by any override file. The priority '
                                           'will be given to the last (right-most) file specified. This file '
                                           'should only include the specific values you want to override, '
                                           'it should not be the entire `values.yaml` from the chart.')

    # parent injector livedata parser
    parent_injector_livedata_parser = argparse.ArgumentParser(add_help=False)
    # mutual exclusive group for injector and livedata
    injector_livedata_group = parent_injector_livedata_parser.add_mutually_exclusive_group()
    injector_livedata_group.add_argument('--injector', default=False, action='store_true',
                                         help='Include the data injector in the instance')
    injector_livedata_group.add_argument('--livedata', default=False, action='store_true',
                                         help='Include live data in the instance')
    # optional args for injector and live data
    parent_injector_livedata_parser.add_argument('--injector-dataset',
                                                 help='Dataset for the injector. If not specified, the default is '
                                                      'the value set in the helm "values.yaml" file.')
    parent_injector_livedata_parser.add_argument('--connman-port', type=int,
                                                 help='If specified, sets the environment variable to change the '
                                                      'well known port for the CD11 connman service, and configures '
                                                      'the port in kubernetes to be externally visible.')
    parent_injector_livedata_parser.add_argument('--connman-data-manager-ip', type=argparse_ip_address_type,
                                                 help='If specified, sets the environment variable to change the '
                                                      'external IP address of the CD11 dataman service.')
    parent_injector_livedata_parser.add_argument('--connman-data-provider-ip', type=argparse_ip_address_type,
                                                 help='If specified, sets the environment variable to change IP '
                                                      'address of the data provider sending data to the CD11 dataman '
                                                      'service.')
    parent_injector_livedata_parser.add_argument('--dataman-ports', type=argparse_dataman_ports_type,
                                                 help='If specified, sets the environment variable to change the port '
                                                      'range for the CD11 dataman service, and configures the ports '
                                                      'in kubernetes to be externally visible.')

    # parent config parser
    parent_config_parser = argparse.ArgumentParser(add_help=False)
    parent_config_parser.add_argument('--config',
                                      help='Path to a directory of configuration overrides to load into instance')

    # parent dry-run parser
    parent_dryrun_parser = argparse.ArgumentParser(add_help=False)
    parent_dryrun_parser.add_argument('--dry-run', default=0, action='count',
                                      help='View the objects to be applied but do not send them')

    subparsers = parser.add_subparsers(help='Available sub-commands:')

    # parent port parser
    parent_port_parser = argparse.ArgumentParser(add_help=False)
    parent_port_parser.add_argument('--port', type=int,
                                    help='Port to access the instance from outside the cluster. If not specified, '
                                         'then the value is determined using the ingress-ports-config configmap'
                                         'in the GMS namespace.')

    # Install
    install_parser = subparsers.add_parser('install',
                                           parents=[parent_name_parser, parent_tag_parser,
                                                    parent_set_parser, parent_set_string_parser,
                                                    parent_values_parser, parent_injector_livedata_parser,
                                                    parent_config_parser, parent_dryrun_parser,
                                                    parent_port_parser],
                                           help='Install an instance of the system')
    # type and chart are mutually exclusive, and at least one must be specified. Chart arg gets repeated again in
    # upgrade, but we can't implement as a parent parser due to the group here.
    install_type_chart_group = install_parser.add_mutually_exclusive_group(required=True)
    install_type_chart_group.add_argument('--type', choices=TYPES, help='Type of instance')
    install_type_chart_group.add_argument('--chart',
                                          help='Path to a local helm chart directory to deploy. If not specified, '
                                               'the helm chart is automatically extracted from a docker image that '
                                               'contains the chart files for the branch. Note the directory must '
                                               'exist at or below the present directory (PWD), no `../` is allowed.')
    install_parser.add_argument('--wallet-path',
                                help='Optional path to an Oracle Wallet directory. Under normal circumstances '
                                     'the shared cluster-wide wallet or the container wallet, will automatically '
                                     'be used for the instance, so supplying an Oracle Wallet path '
                                     'is not necessary. This argument should only be used when testing a new '
                                     'Oracle Wallet.')
    install_parser.add_argument('--no-istio', dest='istio', default=True, action='store_false',
                                help='Disable istio-injection label in the namespace.')
    install_parser.add_argument('--augment', action='append', type=argparse_augmentation_type,
                                help='Augmentation names to apply to the instance during install. Can be specified multiple '
                                     'times to apply multiple augmentations. The augmentation name must be a valid augmentation '
                                     'found by running `gmskube augment catalog --tag <tag name>`.')

    install_parser.set_defaults(command=install_command)

    # Reconfig
    reconfig_parser = subparsers.add_parser('reconfig',
                                            parents=[parent_name_parser, parent_config_parser, parent_port_parser],
                                            help='Reconfigure a running instance of a system')
    reconfig_parser.set_defaults(command=reconfig_command)

    # Upgrade
    upgrade_parser = subparsers.add_parser('upgrade',
                                           parents=[parent_name_parser, parent_set_parser,
                                                    parent_set_string_parser, parent_values_parser,
                                                    parent_injector_livedata_parser, parent_tag_parser,
                                                    parent_dryrun_parser],
                                           help='Upgrade an instance of the system')
    # for Upgrade, type is not an option since we don't want to let people change the type during an upgrade. Chart
    # is optional here. Not implemented as a parent parser since it won't work with the group in Install.
    upgrade_parser.add_argument('--chart',
                                help='Path to a local helm chart directory to deploy. If not specified, '
                                     'the helm chart is automatically extracted from a docker image that '
                                     'contains the chart files for the branch. Note the directory must '
                                     'exist at or below the present directory (PWD), no `../` is allowed.')
    upgrade_parser.set_defaults(command=upgrade_command)

    # Uninstall
    uninstall_parser = subparsers.add_parser('uninstall',
                                             parents=[parent_name_parser],
                                             help='Uninstall an instance of the system')
    uninstall_parser.set_defaults(command=uninstall_command)

    # List
    list_parser = subparsers.add_parser('list', aliases=['ls'], help='List instances')
    list_parser.add_argument('--user', help='List only instances deployed by the specified user.')
    list_parser.add_argument('--type', choices=TYPES, help='List only instances of the specified type.')
    list_parser.add_argument('--all', '-a', default=False, action='store_true',
                             help='Include all namespaces (system, rancher, etc.), not just GMS instances.')
    list_parser.set_defaults(command=list_command)

    # Augment [Apply, Delete, Catalog]
    augment_parser = subparsers.add_parser('augment', help='Augment a running instance of the system')
    augment_subparsers = augment_parser.add_subparsers(help='Available augment sub-commands:')

    parent_augment_name_parser = argparse.ArgumentParser(add_help=False)
    parent_augment_name_parser.add_argument('--name', '-n', dest='augmentation_name', type=argparse_augmentation_type,
                                            help='Name of augmentation. See --list for available names.')

    parent_augment_chart_parser = argparse.ArgumentParser(add_help=False)
    parent_augment_chart_parser.add_argument('--chart',
                                             help='Path to a local helm chart directory to deploy. If not specified, '
                                             'the helm chart is automatically extracted from a docker image that '
                                             'contains the chart files for the branch. Note the directory must '
                                             'exist at or below the present directory (PWD), no `../` is allowed.')

    # Augment Apply
    augment_apply_parser = augment_subparsers.add_parser('apply',
                                                         parents=[parent_augment_name_parser, parent_augment_chart_parser,
                                                                  parent_name_parser, parent_dryrun_parser, parent_tag_parser],
                                                         help='Apply an augmentation to a running instance of the system')
    augment_apply_parser.set_defaults(command=augment_apply_command)
    augment_apply_parser.add_argument('--set', dest='sets', type=argparse_augment_set_type, action='append',
                                      help='Set a value in the augmentation to the specified value.  May be specified '
                                      'multiple times for different values.  Examples: `--set foo=bar` to '
                                      'set value `foo` to `bar`. `--set env.AUG_VAR=World` to '
                                      'set the `AUG_VAR` environment var to `World` in the '
                                      'augmentation app\'s Pod.')

    # Augment Delete
    augment_delete_parser = augment_subparsers.add_parser('delete',
                                                          parents=[parent_augment_name_parser, parent_augment_chart_parser,
                                                                   parent_name_parser, parent_dryrun_parser, parent_tag_parser],
                                                          help='Delete the specified augmentation')
    augment_delete_parser.set_defaults(command=augment_delete_command)

    # Augment Catalog
    augment_catalog_parser = augment_subparsers.add_parser('catalog', aliases=['cat'],
                                                           parents=[parent_tag_parser, parent_augment_chart_parser],
                                                           help='Catalog of available augmentation names')
    augment_catalog_parser.set_defaults(command=augment_catalog_command)

    # Ingress
    ingress_parser = subparsers.add_parser('ingress',
                                           parents=[parent_name_parser, parent_port_parser],
                                           help='List ingress routes for an instance')
    ingress_parser.set_defaults(command=ingress_command)

    return parser


def get_args(parser: ArgumentParser) -> Namespace:
    args = parser.parse_args()

    # validate instance name if it is present - note this is intentionally not a argparse type because
    # any unknown arguments will cause it to error too soon and give a misleading error message to the user
    if getattr(args, 'name', None) is not None:
        validate_instance_name(args.name)

    # check if livedata is specified for any optional live data args
    if (getattr(args, 'connman_port', None) is not None
        or getattr(args, 'connman_data_manager_ip', None) is not None
        or getattr(args, 'connman_data_provider_ip', None) is not None
        or getattr(args, 'dataman_ports', None) is not None) and not getattr(args, 'livedata', False):
        parser.error('--livedata must be specified if any of --connman-port, --connman-data-manager-ip, '
                     '--connman-data-provider-ip, or --dataman-ports are provided.')

    return args


def validate_instance_name(s: str, pat: re.Pattern = re.compile(r'^[a-z0-9][a-z0-9-]{1,126}[a-z0-9]$')) -> None:
    """
    This checks two limitations that apply to instance names:
    1. Instance name length is between 3 and 128 characters. Until we find out
       otherwise, this is an arbitrary limit.
    2. The instance name will be used as part of a DNS hostname, so it must
       comply with DNS naming rules:
       "hostname labels may contain only the ASCII letters 'a' through 'z' (in
       a case-insensitive manner), the digits '0' through '9', and the hyphen
       ('-'). The original specification of hostnames in RFC 952, mandated that
       labels could not start with a digit or with a hyphen, and must not end
       with a hyphen. However, a subsequent specification (RFC 1123) permitted
       hostname labels to start with digits. No other symbols, punctuation
       characters, or white space are permitted.
    """
    if not pat.match(s):
        raise argparse.ArgumentTypeError(
            'Instance name must be between 3 and 128 characters long, consist only of lower case letters '
            'digits, and hyphens.')


# ---------- Argparse Types -----------------
def argparse_tag_name_type(s: str) -> str:
    """
    Transform the tag name into the CI_COMMIT_REF_SLUG as defined by gitlab:
    Lower-cased, shortened to 63 bytes, and with everything except `0-9` and `a-z` replaced with `-`.
    No leading / trailing `-`
    """

    # s.lower() changes to lower case, re.sub replaces anything other than a-z 0-9 with `-`
    # strip('-') removes any leading or trailing `-` after re.sub, finally [:63] truncates to 63 chars
    return re.sub(r'[^a-z0-9]', '-', s.lower()).strip('-')[:63]


def argparse_set_type(s: str, check_env: bool = True) -> str:
    """
    Use a regular expression to match a helm value of the form:
    VARIABLE=VALUE
    Helm accepts a lot of different values, https://helm.sh/docs/intro/using_helm/#the-format-and-limitations-of---set
    so the regex is not very restrictive to allow for all the different forms
    """

    if not re.match(r'^.+=.*', s):
        raise argparse.ArgumentTypeError(
            'When specifying `--set`, you must supply helm chart name/value pair as: `Name=Value`'
        )

    # Check for deprecated 'env'
    if check_env and re.match(r'^env\..+=.*', s):
        raise argparse.ArgumentTypeError(
            'The old top-level `env` has been replaced with `global.env`. Please update any `--set env.foo=bar` '
            'arguments to `--set global.env.foo=bar`.'
        )

    return s


def argparse_augment_set_type(s: str) -> str:
    """
    Validates the --set for augmentations, without checking the env
    """
    return argparse_set_type(s, check_env=False)


def argparse_ip_address_type(s: str) -> str:
    """
    Really basic IP address validation
    """
    if not re.match(r'^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$', s):
        raise argparse.ArgumentTypeError(
            'Must be a valid IP address (example: 192.168.1.1)'  # NOSONAR - just an example; does not need to be configurable
        )
    return s


def argparse_dataman_ports_type(s: str) -> str:
    """
    Dataman port range should be two numbers separated by a dash
    """

    if not re.match(r'^\d{1,5}-\d{1,5}$', s):
        raise argparse.ArgumentTypeError(
            'Dataman port range must be two integers separated by a dash (example: 8100-8199)'
        )
    return s


def argparse_augmentation_type(s: str) -> str:
    """
    Validate that augmentation name is valid
    """

    if not gmskube.augmentation_exists(s):
        raise argparse.ArgumentTypeError(
            f"Augmentation '{s}' is not a valid augmentation name"
        )
    return s


# ---------- Commands -----------------
# Most commands have "pragma: no coverage" because they only call a function in gmskube
# that has its own unit tests
def install_command(args: Namespace) -> None:  # pragma: no coverage
    gmskube.install(instance_name=args.name,
                    instance_type=args.type,
                    ingress_port=args.port,
                    is_istio=args.istio,
                    custom_chart_path=args.chart,
                    dry_run=args.dry_run,
                    wallet_path=args.wallet_path,
                    image_tag=args.tag,
                    sets=args.sets,
                    set_strings=args.set_strings,
                    values=args.values,
                    injector=args.injector,
                    injector_dataset=args.injector_dataset,
                    livedata=args.livedata,
                    connman_port=args.connman_port,
                    connman_data_manager_ip=args.connman_data_manager_ip,
                    connman_data_provider_ip=args.connman_data_provider_ip,
                    dataman_ports=args.dataman_ports,
                    config_override_path=args.config,
                    timeout=args.timeout,
                    augmentations=args.augment)


def upgrade_command(args: Namespace) -> None:  # pragma: no coverage
    gmskube.upgrade(instance_name=args.name,
                    custom_chart_path=args.chart,
                    dry_run=args.dry_run,
                    image_tag=args.tag,
                    sets=args.sets,
                    set_strings=args.set_strings,
                    values=args.values,
                    injector=args.injector,
                    injector_dataset=args.injector_dataset,
                    livedata=args.livedata,
                    connman_port=args.connman_port,
                    connman_data_manager_ip=args.connman_data_manager_ip,
                    connman_data_provider_ip=args.connman_data_provider_ip,
                    dataman_ports=args.dataman_ports)


def uninstall_command(args: Namespace) -> None:  # pragma: no coverage
    gmskube.uninstall(instance_name=args.name, timeout=args.timeout)


def list_command(args: Namespace) -> None:  # pragma: no coverage
    gmskube.list_instances(username=args.user,
                           instance_type=args.type,
                           show_all=args.all)


def ingress_command(args: Namespace) -> None:  # pragma: no coverage
    gmskube.ingress(instance_name=args.name,
                    ingress_port=args.port)


def reconfig_command(args: Namespace) -> None:  # pragma: no coverage
    gmskube.reconfig(instance_name=args.name,
                     ingress_port=args.port,
                     config_override_path=args.config,
                     timeout=args.timeout)


def augment_apply_command(args: Namespace) -> None:  # pragma: no coverage
    gmskube.augment_apply(instance_name=args.name,
                          augmentation_name=args.augmentation_name,
                          custom_chart_path=args.chart,
                          sets=args.sets,
                          image_tag=args.tag,
                          dry_run=args.dry_run)


def augment_delete_command(args: Namespace) -> None:  # pragma: no coverage
    gmskube.augment_delete(instance_name=args.name,
                           augmentation_name=args.augmentation_name,
                           custom_chart_path=args.chart,
                           image_tag=args.tag,
                           dry_run=args.dry_run)


def augment_catalog_command(args: Namespace) -> None:  # pragma: no coverage
    gmskube.augment_catalog()


def help_command(parser: ArgumentParser | None = None) -> None:
    parser.print_help()


def handler(signal_received: int, frame: FrameType) -> None:  # pragma: no coverage
    # Handle any cleanup here
    sys.exit(0)


if __name__ == "__main__":  # pragma: no coverage
    # register SIGINT handler for ctl-c
    signal(SIGINT, handler)

    main()
