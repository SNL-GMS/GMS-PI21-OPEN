#!/usr/bin/env python3

# --------------------------------------------------------------------
#  gmskube - Geophysical Monitoring System Control Utility
#
#  The gmskube command-line program is used to install and configure
#  instances of the GMS (Geophysical Monitoring System) system
#  on Kubernetes.
# --------------------------------------------------------------------
import contextlib
import getpass
import io
import json
import logging
import os
import re
import shlex
import subprocess
import sys
import tarfile
import time
from operator import itemgetter

import requests
from urllib3 import Retry
import yaml
from requests.adapters import HTTPAdapter
from rich.console import Console

console_kwargs = {"log_path": False}
if os.getenv("CI"):
    console_kwargs["force_terminal"] = True
if os.getenv("RICH_LOG_PATH"):
    console_kwargs["log_path"] = True
console = Console(**console_kwargs)
console_kwargs_dry_run = console_kwargs
console_kwargs_dry_run["force_interactive"] = False
console_kwargs_dry_run["highlight"] = False
console_kwargs_dry_run["soft_wrap"] = True


# ---------- Commands -----------------
def install(instance_name: str,
            instance_type: str,
            ingress_port: str,
            is_istio: bool,
            custom_chart_path: str,
            dry_run: bool,
            wallet_path: str,
            image_tag: str,
            sets: list[str],
            set_strings: list[str],
            values: str,
            injector: bool,
            injector_dataset: str,
            livedata: bool,
            connman_port: str,
            connman_data_manager_ip: str,
            connman_data_provider_ip: str,
            dataman_ports: str,
            config_override_path: str,
            timeout: int,
            augmentations: list[str]) -> None:
    """
    Perform helm install command, with some extra options for data load
    :param instance_name: name of the instance to install
    :param instance_type: type of instance
    :param ingress_port: port for ingress to the config-loader
    :param is_istio: enable istio injection
    :param custom_chart_path: path to custom helm chart
    :param dry_run: do not actually install, print yaml instead
    :param wallet_path: path to custom oracle wallet
    :param image_tag: docker image tag
    :param sets: list of --set key=value pairs
    :param set_strings: list of --set-string key=value pairs
    :param values: values override yaml file
    :param injector: enable data injector
    :param injector_dataset: name of the injector dataset
    :param livedata: live data enabled
    :param connman_port: incoming port for connman to listen on
    :param connman_data_manager_ip: external ip address where dataman will be listening
    :param connman_data_provider_ip: external ip address where data will be sending from
    :param dataman_ports: port range for dataman
    :param config_override_path: path to custom config overrides
    :param timeout: timeout for dataload
    :param augmentations: list of augmentations to apply after install
    :return: None
    """
    if dry_run:
        global console
        console = Console(**console_kwargs_dry_run)
        console.log = console.print
    console.log(f'[cyan]Installing {instance_name}')

    base_domain = get_base_domain()
    docker_registry = os.getenv('CI_DOCKER_REGISTRY')
    # get the ingress port
    console.log('Getting ingress port')
    port = get_ingress_port(is_istio, ingress_port)
    console.log(f'Ingress port: {port}')

    # Set the instance type to be custom if loading in a user-defined custom chart directory
    if custom_chart_path is not None:
        instance_type = "custom"

    if not dry_run:
        # create the namespace
        console.log(f'[cyan]Setting up namespace {instance_name}')
        create_namespace(instance_name, instance_type, is_istio)

    # build up the command array
    install_cmd = shlex.split(
        f'helm {"install" if dry_run < 2 else "template"} {instance_name} {instance_type} '
        f'--namespace {instance_name} '
        f'--set "global.baseDomain={base_domain}" '
        f'--set "global.basePort={port}" '
        f'--set "global.imageRegistry={docker_registry}" '
        f'--set "kafka.image.registry={docker_registry}" '
        f'--set "kafka.zookeeper.image.registry={docker_registry}" '
        f'--set "kafka.metrics.kafka.image.registry={docker_registry}" '
        f'--set "kafka.metrics.jmx.image.registry={docker_registry}" '
        f'--set "global.imageTag={image_tag}" '
        f'--set "kafka.image.tag={image_tag}" '
        f'--set "kafka.zookeeper.image.tag={image_tag}" '
        f'--set "kafka.metrics.kafka.image.tag={image_tag}" '
        f'--set "kafka.metrics.jmx.image.tag={image_tag}" '
        f'--set "global.user={getpass.getuser()}" '
        # These --set commands are for LOGGING --BEGIN
        f'--set "elasticsearch.image={docker_registry}/gms-common/logging-elasticsearch" '
        f'--set "elasticsearch.imageTag={image_tag}" '
        f'--set "elasticsearch.ingress.hosts[0]=elasticsearch.{base_domain}" '
        f'--set "elasticsearch.ingress.tls[0].hosts[0]=elasticsearch.{base_domain}" '
        # note helm does not merge lists so we have to set the secretName (https://github.com/helm/helm/issues/5711)
        f'--set "elasticsearch.ingress.tls[0].secretName=ingress-default-cert" '
        f'--set "fluentd.image.repository={docker_registry}/gms-common/logging-fluentd" '
        f'--set "fluentd.image.tag={image_tag}" '
        f'--set "kibana.image={docker_registry}/gms-common/logging-kibana" '
        f'--set "kibana.imageTag={image_tag}" '
        # These --set commands are for LOGGING --END
    )

    if wallet_path is not None:
        install_cmd.extend(shlex.split('--set "global.oracleWalletOverride=true"'))

    if is_istio:
        install_cmd.extend(shlex.split('--set "global.istio=true"'))

    # add any custom helm values set by the --set option
    if sets is not None:
        for item in sets:
            install_cmd.extend(shlex.split(f'--set "{item}" '))

    # add any custom helm values set by the --set-string option
    if set_strings is not None:
        for item in set_strings:
            install_cmd.extend(shlex.split(f'--set-string "{item}" '))

    # add values override file set by the --values option
    if values is not None:
        install_cmd.extend(shlex.split(f'--values "{values}" '))

    # add livedata args
    install_cmd.extend(
        get_livedata_set_args(
            livedata,
            connman_port,
            connman_data_manager_ip,
            connman_data_provider_ip,
            dataman_ports
        )
    )

    # Apply any augmentations
    if augmentations is not None:
        for aug_name in augmentations:
            console.log(f'[cyan]Enabling augmentation {aug_name}')
            install_cmd.extend(
                shlex.split(f'--set "augmentation.{aug_name}.enabled=true" ')
            )

    # add dry-run
    if dry_run:
        install_cmd.extend(shlex.split('--dry-run --debug'))

    # run helm install
    console.log('[cyan]Running helm install')
    return_code, out, err = run_helm_install(command=install_cmd)

    # if dry-run just exit here since everything else after this point requires a real install
    if dry_run:
        sys.exit(return_code)

    if return_code > 0:
        print_error(f'Could not install instance {instance_name}: {err}')
        sys.exit(return_code)

    # Run the config-loader
    console.log('[cyan]Beginning data load')
    if not request_dataload(
        base_domain=base_domain,
        instance_name=instance_name,
        config_overrides=config_override_path,
        timeout=timeout,
        port=port
    ):
        print_error('Data load failed to execute successfully, Exiting')
        sys.exit(1)

    # Add injector after the config-load
    if injector:
        console.log('[cyan]Adding cd11-injector')
        injector_sets = []
        if injector_dataset is not None:
            injector_sets.append(f'env.CD11_INJECTOR_CONFIG_NAME={injector_dataset}')

        augment_apply(
            instance_name=instance_name,
            augmentation_name='cd11-injector',
            custom_chart_path=custom_chart_path,
            sets=injector_sets,
            image_tag=image_tag,
            dry_run=dry_run
        )

    console.log(
        f'\nTo list ingress routes for this instance, run `gmskube ingress {instance_name}`'
    )
    console.log(f'[bold green]{instance_name} installed successfully!')


def upgrade(
    instance_name: str,
    custom_chart_path: str,
    dry_run: bool,
    image_tag: str,
    sets: list[str],
    set_strings: list[str] | None,
    values: str | None,
    injector: bool,
    injector_dataset: str | None,
    livedata: bool,
    connman_port: str | None,
    connman_data_manager_ip: str | None,
    connman_data_provider_ip: str | None,
    dataman_ports: str | None
) -> None:
    """
    Perform helm upgrade command
    :param instance_name: name of the instance to upgrade
    :param custom_chart_path: path to custom helm chart
    :param dry_run: do not actually install, print yaml instead
    :param image_tag: docker image tag
    :param sets: list of --set key=value pairs
    :param set_strings: list of --set-string key=value pairs
    :param values: values override yaml file
    :param injector: enable data injector
    :param injector_dataset: name of the injector dataset
    :param livedata: live data enabled
    :param connman_port: incoming port for connman to listen on
    :param connman_data_manager_ip: external ip address where dataman will be listening
    :param connman_data_provider_ip: external ip address where data will be sending from
    :param dataman_ports: port range for dataman
    :return: None
    """
    if dry_run:
        global console
        console = Console(**console_kwargs_dry_run)
        console.log = console.print
    console.log(f'[cyan]Upgrading {instance_name}')

    # get the instance type
    console.log('Getting instance type')
    if custom_chart_path is not None:
        # Set the instance type to be custom if loading in a user-defined custom chart directory
        instance_type = "custom"
    else:
        # get the instance type from the labels. We don't use args.type here because we don't allow the type
        # to be changed during upgrade.
        instance_type = get_instance_labels(instance_name
                                            ).get('gms/type',
                                                  None)

    # check if the instance type is none and error
    if instance_type is None:
        print_error(
            f'Could not determine the type for instance {instance_name}. Possible causes:\n'
            '        - Instance is not installed. Check by running `gmskube ls`.\n'
            '        - Instance was not installed using `gmskube` and is missing metadata. Uninstall then use `gmskube install`.'
        )
        sys.exit(1)
    console.log(f'Instance type is: {instance_type}')

    # delete helm unmanaged resources. This is a backwards compatibility that can be removed
    # in PI22
    if not dry_run:
        delete_unmanaged_resources(namespace=instance_name)

    # get existing values
    console.log('Getting existing helm values')
    return_code, out, err = run_helm_get_values(namespace=instance_name)

    # save existing values to tmp file
    if return_code == 0:
        console.log('Saving existing helm values to a temporary file')
        with open('/tmp/existing_values.yaml', 'w') as existing_values:
            print(out, file=existing_values)
    else:
        # if we can't get the existing values then error
        print_error(
            f'Unable to get existing values for instance {instance_name}: {err}'
        )
        sys.exit(return_code)

    # Run helm upgrade
    # Provide the values file from the chart followed by the existing values, this will merge them together
    upgrade_cmd = shlex.split(
        f'helm upgrade {instance_name} {instance_type} '
        f'--namespace {instance_name} '
        f"--values {os.path.join('/deploy', instance_type, 'values.yaml')} "
        f'--values /tmp/existing_values.yaml '
        f'--set "global.user={getpass.getuser()}" '
        # all permutations of imageTag need to be passed on upgrade to make it into sub-charts
        f'--set "global.imageTag={image_tag}" '
        f'--set "kafka.image.tag={image_tag}" '
        f'--set "kafka.zookeeper.image.tag={image_tag}" '
        f'--set "elasticsearch.imageTag={image_tag}" '
        f'--set "fluentd.image.tag={image_tag}" '
        f'--set "kibana.imageTag={image_tag}" '
    )

    # add any custom helm values set by the --set option
    if sets is not None:
        for item in sets:
            upgrade_cmd.extend(shlex.split(f'--set "{item}"'))

    # add any custom helm values set by the --set-string option
    if set_strings is not None:
        for item in set_strings:
            upgrade_cmd.extend(shlex.split(f'--set-string "{item}" '))

    # add values override file set by the --values option
    if values is not None:
        upgrade_cmd.extend(shlex.split(f'--values "{values}" '))

    # add livedata args
    upgrade_cmd.extend(
        get_livedata_set_args(
            livedata,
            connman_port,
            connman_data_manager_ip,
            connman_data_provider_ip,
            dataman_ports
        )
    )

    # add injector args
    upgrade_cmd.extend(
        get_injector_set_args(
            injector,
            injector_dataset
        )
    )

    # add dry-run
    if dry_run:
        upgrade_cmd.extend(shlex.split('--dry-run --debug'))

    console.log('[cyan]Running helm upgrade')
    return_code, out, err = run_helm_upgrade(command=upgrade_cmd)

    if return_code > 0:
        print_error(f'Could not upgrade instance {instance_name}: {err}')
        sys.exit(return_code)

    console.log(f'[bold green]{instance_name} upgrade complete!')


def uninstall(instance_name: str, timeout: int) -> None:
    """
    Perform helm uninstall command, wait for pods to terminate, then delete the namespace
    :param instance_name: name of the instance to uninstall
    :param timeout: timeout to wait for helm resources to terminate
    :return: None
    """
    console.log(f'[cyan]Uninstalling {instance_name}')

    # run helm uninstall
    console.log('[cyan]Running helm uninstall')
    return_code, out, err = run_helm_uninstall(namespace=instance_name)

    if return_code != 0:
        console.log(
            'Helm uninstall unsuccessful, will attempt to delete the namespace anyway'
        )

    # wait for resources created by helm to terminate since helm uninstall is async
    timeout_seconds = timeout * 60
    time_waited = 0
    while time_waited < timeout_seconds and return_code == 0:  # pragma: no branch
        # get resources filtered by label
        return_code, out, err = run_kubectl_get_all_helm_resources(namespace=instance_name)

        # check the count of lines returned
        if len(out.splitlines()) == 0:
            break

        if time_waited % 15 == 0:  # pragma: no branch
            # print a message every 15 seconds noting that we are waiting
            console.log(
                f'Waiting for helm resources to terminate, {len(out.splitlines())} resources remaining'
            )

        time.sleep(1)
        time_waited += 1

        if time_waited >= timeout_seconds:  # pragma: no coverage
            print_warning(
                'Timed out waiting for helm resources to terminate, attempting to delete the namespace anyway'
            )

    # Delete the namespace
    console.log('[cyan]Deleting namespace')
    return_code, out, err = run_kubectl_delete_namespace(namespace=instance_name)

    if return_code == 0:
        console.log(f'[bold green]{instance_name} uninstall complete!')
    else:
        print_error(
            f'{instance_name} uninstall unsuccessful, please review errors/warnings above'
        )


def reconfig(instance_name: str, ingress_port: str, config_override_path: str, timeout: int) -> None:
    """
    Perform the instance reconfig command - run a reduced dataload, then rollout restart deployments that require it
    :param instance_name: name of the instance to reconfig
    :param ingress_port: port for ingress to the config-loader
    :param config_override_path: path for config overrides
    :param timeout: timeout for config load
    :return: None
    """
    console.log(f'[cyan]Reconfiguring {instance_name}')

    # get instance istio status
    console.log('Getting instance istio status')
    istio = is_instance_istio(instance_name)
    console.log(f'Instance istio status: {istio}')

    # get the ingress port
    console.log('Getting ingress port')
    port = get_ingress_port(istio, ingress_port)
    console.log(f'Ingress port: {port}')

    console.log('[cyan]Beginning data load')
    if not request_dataload(
        base_domain=get_base_domain(),
        instance_name=instance_name,
        endpoint='reload',
        config_overrides=config_override_path,
        timeout=timeout,
        port=port
    ):
        print_error('Data load failed to execute successfully, Exiting')
        sys.exit(1)

    # restart deployments with restartAfterReconfig label
    console.log('[cyan]Rollout restart deployments')
    console.log('Getting list of deployments with label "restartAfterReconfig=true"')
    return_code, out, err = run_kubectl_get_deployments_restart_after_reconfig(namespace=instance_name)

    if return_code > 0:
        print_error(
            f'Unable to get list of deployment requiring restart: {err}'
        )
        sys.exit(return_code)

    # rollout restart each deployment
    deployments_data = json.loads(out)
    for deployment in deployments_data['items']:
        console.log(f"Restarting deployment {deployment['metadata']['name']}")
        run_kubectl_rollout_restart(
            namespace=instance_name,
            resource=f"deployment {deployment['metadata']['name']}"
        )

    console.log(f'[bold green]{instance_name} reconfig complete!')


def list_instances(username: str, instance_type: str, show_all: bool) -> None:
    """
    List install instances
    :param username: Username to filter results
    :param instance_type: Instance type to filter results
    :param show_all: Show all helm installed instances, even if they do not contain gms metadata
    :return: None
    """

    # Get all the helm instances
    return_code, out, err = run_helm_list()
    if return_code > 0:
        print_error(f'Could not list instances: {err}')
        sys.exit(return_code)

    # column format
    col_format = '%-32s   %-10s   %-8s   %-13s   %-18s   %-14s   %-23s'
    # Setup the header
    print(
        col_format % (
            'NAME',
            'STATUS',
            'TYPE',
            'USER',
            'UPDATED',
            'CD11-PORTS',
            'TAG',
        )
    )
    print(
        col_format % (
            '----',
            '------',
            '----',
            '----',
            '-------',
            '----------',
            '---',
        )
    )

    instances = json.loads(out)

    # get all gms configmaps
    return_code, out, err = run_kubectl_get_configmap_all_namespaces('gms')

    if return_code != 0:
        print_error(f'Unable to get gms configmap from all namespaces: {err}')
        exit(return_code)

    all_gms_configmaps = json.loads(out)

    # loop through each of the helm instances
    for instance in instances:
        # get the labels for this instance, or empty dict if it doesn't exist
        labels = next((
            item['metadata']['labels']
            for item in all_gms_configmaps['items']
            if item['metadata']['labels']["gms/name"] == instance['name']
        ), {})  # pragma: no branch

        # filter instances without gms labels unless the "all" arg is specified
        if not show_all and len(labels) == 0:
            continue

        # filter on the user if provided
        if username is not None and labels.get('gms/user', '?') != username:
            continue

        # filter on the type of provided
        if instance_type is not None and labels.get(
            'gms/type',
            '?'
        ) != instance_type:
            continue

        # Only display something in the CD11-PORTS for instances attached to live data
        livedata = "-"
        if (
            labels.get('gms/cd11-live-data', '') == 'true'  # pragma: no branch
            and labels.get('gms/cd11-connman-port')
            and labels.get('gms/cd11-dataman-port-start')
            and labels.get('gms/cd11-dataman-port-end')
        ):
            livedata = f"{labels.get('gms/cd11-connman-port')},{labels.get('gms/cd11-dataman-port-start')}-{labels.get('gms/cd11-dataman-port-end')}"

        print(
            col_format % (
                instance['name'],
                instance['status'],
                labels.get('gms/type',
                           '?'),
                labels.get('gms/user',
                           '?'),
                labels.get('gms/update-time',
                           '?'),
                livedata,
                labels.get('gms/image-tag',
                           '?'),
            )
        )


def ingress(instance_name: str, ingress_port: str) -> None:
    """
    List the ingress routes for an instance
    :param instance_name: name of the instance to list ingress
    :param ingress_port: the ingress port to the instance
    :return:
    """

    # get instance istio status
    istio = is_instance_istio(instance_name)

    # get the ingress port
    port = get_ingress_port(istio, ingress_port)

    # column format
    col_format = '%-60s   %-70s'
    # Setup the header
    print(col_format % ('SERVICE', 'URL'))
    print(col_format % ('-------', '---'))

    if istio:
        # istio routes are in a virtual service
        return_code, out, err = run_kubectl_get(namespace=instance_name,
                                                resource_type='virtualservice',
                                                resource_name='')

        if return_code != 0:  # pragma: no coverage
            print_error('Unable to get virtualservice details')
            exit(return_code)

        vs_data = json.loads(out)

        # loop through the virtualservice data
        for vs in vs_data['items']:
            host = f"https://{vs['spec']['hosts'][0]}:{port}"
            name = vs['metadata']['name']
            for http in vs['spec']['http']:
                for match in http['match']:
                    try:
                        path = match['uri']['prefix'].lstrip('/')
                        print(col_format % (name, f"{host}/{path}"))
                    except KeyError:
                        # if there is no uri/prefix then just ignore it
                        continue
    else:
        # non-istio routes are in the ingress object
        return_code, out, err = run_kubectl_get(namespace=instance_name,
                                                resource_type='ingress',
                                                resource_name='')

        if return_code != 0:  # pragma: no coverage
            print_error('Unable to get ingress details')
            exit(return_code)

        ingress_data = json.loads(out)

        # loop through the ingress data
        for ingress in ingress_data['items']:
            for rule in ingress['spec']['rules']:
                host = f"https://{rule['host']}:{port}"
                for path in rule['http']['paths']:
                    print(
                        col_format % (
                            path['backend']['service']['name'],
                            f"{host}{path['path']}"
                        )
                    )


def augment_apply(
    instance_name: str,
    augmentation_name: str,
    custom_chart_path: str,
    sets: list[str],
    image_tag: str,
    dry_run: bool
) -> None:
    """
    Perform 'augment apply' command.
    :param instance_name: name of the instance to apply augmentation
    :param augmentation_name: name of augmentation to apply
    :param custom_chart_path: path to custom helm charts
    :param sets: list of --set key=value pairs
    :param image_tag: docker image tag
    :param dry_run: do not actually apply, print yaml instead
    :return: None
    """
    if dry_run:
        global console
        console = Console(**console_kwargs_dry_run)
        console.log = console.print
    if not augmentation_exists(augmentation_name):
        print_error(
            f'Augmentation `{augmentation_name}` is not a valid augmentation name.'
        )
        sys.exit(1)

    try:
        console.log(
            f"[cyan]Applying augmentation '{augmentation_name}' to {instance_name}"
        )

        if sets is None:
            sets = []

        # sets should be in the context of the augmentation application, so append the right scope
        for i in range(len(sets)):
            sets[i] = f'augmentation.{augmentation_name}.{sets[i]}'

        # add the enabled flag
        sets.append(f'augmentation.{augmentation_name}.enabled=true')

        # add global injector flag only for the special case of cd11-injector
        if augmentation_name == 'cd11-injector':
            sets.append('global.injector=True')

        upgrade(
            instance_name=instance_name,
            custom_chart_path=custom_chart_path,
            dry_run=dry_run,
            image_tag=image_tag,
            sets=sets,
            set_strings=None,
            values=None,
            injector=False,
            injector_dataset=None,
            livedata=False,
            connman_port=None,
            connman_data_manager_ip=None,
            connman_data_provider_ip=None,
            dataman_ports=None
        )

    except Exception as e:
        print_error(
            f"Failed to apply augmentation '{augmentation_name}' to {instance_name}: {e}"
        )
        sys.exit(1)

    console.log(
        f"[bold green]Augmentation '{augmentation_name}' successfully applied to {instance_name}"
    )


def augment_delete(
    instance_name: str,
    augmentation_name: str,
    custom_chart_path: str,
    image_tag: str,
    dry_run: bool
) -> None:
    """
    Perform 'augment delete' command.
    :param instance_name: name of the instance to delete augmentation
    :param augmentation_name: name of augmentation to delete
    :param custom_chart_path: path to custom helm charts
    :param image_tag: docker image tag
    :param dry_run: do not actually delete, print yaml instead
    :return: None
    """
    if dry_run:
        global console
        console = Console(**console_kwargs_dry_run)
        console.log = console.print

    if not augmentation_exists(augmentation_name):
        print_error(
            f'Augmentation `{augmentation_name}` is not a valid augmentation name.'
        )
        sys.exit(1)

    try:
        console.log(
            f"[cyan]Deleting '{augmentation_name}' from {instance_name}"
        )

        # set disabled flag
        sets = [f'augmentation.{augmentation_name}.enabled=false']

        upgrade(
            instance_name=instance_name,
            custom_chart_path=custom_chart_path,
            dry_run=dry_run,
            image_tag=image_tag,
            sets=sets,
            set_strings=None,
            values=None,
            injector=False,
            injector_dataset=None,
            livedata=False,
            connman_port=None,
            connman_data_manager_ip=None,
            connman_data_provider_ip=None,
            dataman_ports=None
        )

    except Exception as e:
        print_error(
            f"Failed to delete augmentation '{augmentation_name}' from {instance_name}: {e}"
        )
        sys.exit(1)

    console.log(
        f"[bold green]Augmentation '{augmentation_name}' successfully deleted from {instance_name}"
    )


def augment_catalog() -> None:
    """
    Display a table of the augmentations available to be applied to an
    instance of the GMS system.
    """
    augmentations = []
    with open('/deploy/augmentation/values.yaml', 'r') as file:
        aug_values = yaml.safe_load(file)
    for key, value in aug_values.items():
        with contextlib.suppress(KeyError, TypeError):
            metadata = value['metadata']
            aug = {
                'name': key,
                'type': metadata.get('type', 'none'),
                'labels': metadata.get('labels', []),
                'wait': metadata.get('wait', ""),
                'description': metadata.get('description', "")
            }  # yapf: disable
            augmentations.append(aug)
    col_format = '%-48s   %-8s   %-23s  %-50s'
    print(col_format % ('NAME', 'TYPE', 'LABELS', 'DESCRIPTION'))
    print(col_format % ('----', '----', '------', '-----------'))
    for a in sorted(augmentations, key=itemgetter('type')):
        print(
            col_format % (
                a['name'],
                a['type'],
                ','.join(a.get('labels', [])),
                a['description']
            )
        )  # yapf: disable


# ---------- Other Helper Functions -----------------
def augmentation_exists(augmentation_name: str) -> bool:
    """
    Checks if an augmentation is defined in the augmentation values.yaml file
    :param augmentation_name: Name of the augmentation to check
    :return: True if the augmentation exists, False otherwise
    """
    with open('/deploy/augmentation/values.yaml', 'r') as file:
        aug_values = yaml.safe_load(file)
    for key, value in aug_values.items():
        with contextlib.suppress(KeyError, TypeError):
            if key == augmentation_name:
                value['metadata']
                return True
    return False


def check_kubernetes_connection() -> None:
    """
    Validates the connection to kubernetes is successful
    """
    return_code, out, err = run_kubectl_auth()
    if return_code != 0:
        print_error(
            f'Unable to connect to the kubernetes cluster.\nMessage: {err}\n\n'
            f'Possible things to check:\n'
            f'    - You are connecting to the correct cluster.\n'
            f'    - Your kube config file credentials are valid.\n'
            f'    - Run `kubectl get nodes` and check for error messages.'
        )
        exit(return_code)


def request_dataload(
    base_domain: str,
    instance_name: str,
    endpoint: str = 'load',
    config_overrides: str | None = None,
    timeout: int = 4,
    port: int = 443
) -> bool:
    """
    Send HTTP request to the config-loader initiate a dataload
    :param base_domain: FQDN of the kubernetes cluster where the config-loader service is running
    :param instance_name: Instance name to perform the dataload on
    :param endpoint: HTTP service target endpoint. Default is 'load'.
    :param config_overrides: Directory path for configuration overrides. Default is None.
    :param timeout: Timeout in minutes to wait for config-loader to be alive, and for dataload to complete.
    :param port: The port for making the https request to the config-loader. If None, then the default https port is used.
    :return: True if dataload was successful, False otherwise.
    """

    timeout_seconds = timeout * 60

    # check if config-loader service exists in the instance
    return_code, out, err = run_kubectl_get(namespace=instance_name,
                                            resource_type='service',
                                            resource_name='config-loader')
    if return_code > 0:
        console.log('config-loader service does not exist, skipping data load')
        return True

    try:
        retry_strategy = Retry(
            total=20,
            backoff_factor=0.2,
            status_forcelist=[404],
            allowed_methods=["POST",
                             "GET"]
        )
        adapter = HTTPAdapter(max_retries=retry_strategy)
        http = requests.Session()
        http.mount("https://", adapter)

        # format the url - must be https on kube cluster, and the requests CA bundle env var must be set
        config_loader_url = f"https://{instance_name}.{base_domain}:{port}/config-loader"

        if config_overrides is not None:
            override_file = get_override_tar_file(config_overrides)
            if override_file is None:
                print_error(
                    'Unable to create tar file from user supplied overrides'
                )
                sys.exit(1)
            files = {'files': override_file}
        else:
            files = None

        console.log('Waiting for config loader to be alive')
        time_waited = 0
        while time_waited < timeout_seconds:
            post_response = http.get(
                f"{config_loader_url}/alive",
                allow_redirects=False
            )

            if post_response.status_code == 200:
                break

            if time_waited % 30 == 0:  # pragma: no branch
                # print a message every 30 seconds noting that we are waiting.
                console.log('Waiting for config loader to be alive')

            time.sleep(1)
            time_waited += 1

            if time_waited >= timeout_seconds:  # pragma: no branch
                print_warning(
                    'Timed out waiting for config loader to be alive, will attempt data load anyway'
                )

        console.log('Requesting data load')
        post_response = http.post(
            f"{config_loader_url}/{endpoint}",
            files=files
        )

        if post_response.status_code != 200:
            print_error(
                f'Failed to initiate a data load. {post_response.status_code}: {post_response.reason}'
            )
            sys.exit(1)

        # Wait for results from the config-loader service
        response_json = None
        time_waited = 0
        while time_waited < timeout_seconds:  # pragma: no branch
            time.sleep(1)
            time_waited += 1

            get_response = http.get(f"{config_loader_url}/result")
            if get_response.status_code != 200:
                print_warning(
                    f'Status code from result endpoint was unexpected: {get_response.status_code}'
                )
                continue

            try:
                response_json = get_response.json()
                # print out the chunks of the partial config-loader log
                if partial_result := response_json["partial_result"]:
                    console.log(partial_result.strip())
                if response_json['status'] == 'FINISHED':
                    break
            except json.decoder.JSONDecodeError:
                print_warning(
                    f"Unable to convert response to json: '{get_response.text}'"
                )

        if response_json is None:
            print_error('Data load response status is unknown')
            sys.exit(1)
        elif response_json['status'] != 'FINISHED':
            print_error(
                f'Timed out waiting for data load after {timeout} minutes, Exiting'
            )
            sys.exit(1)
        elif response_json['successful']:
            console.log('Data load successfully completed')
            return True
        else:
            print_error('Data load failed to execute successfully, Exiting')
            sys.exit(1)

    except Exception as e:
        print_error(e)
        sys.exit(1)


def get_override_tar_file(config_dir: str) -> str:
    buffered_tarfile = None
    try:
        # This method will take the input config dir and create a tar file
        filelist = []
        dirlist = [
            f"{config_dir}/processing",
            f"{config_dir}/station-reference/stationdata",
            f"{config_dir}/station-reference/definitions",
            f"{config_dir}/user-preferences"
        ]

        for override_dir in dirlist:
            if os.path.exists(override_dir):
                for root, dirs, files in os.walk(override_dir):
                    for name in files:
                        fullpathfilename = os.path.join(root, name)
                        subpathfilename = os.path.relpath(
                            fullpathfilename,
                            config_dir
                        )
                        filelist.append(subpathfilename)

        # Change to the config override directory
        os.chdir(config_dir)

        # Create the tar file
        fh = io.BytesIO()
        with tarfile.open(fileobj=fh, mode='w:gz') as tar:
            for file in filelist:
                # ignore any filenames that start with '.'
                if not file.startswith('.'):
                    tar.add(file)

        buffered_tarfile = fh.getbuffer()

    except Exception as ex:
        print_error(ex)

    return buffered_tarfile


def get_base_domain() -> str:
    """
    Gets the base domain for ingress
    :return: string representing the base domain for ingress
    """
    config = get_ingress_ports_config()

    try:
        return config['base_domain']
    except KeyError:
        print_error(
            '`base_domain` is not configured in the gms ingress-ports-config configmap'
        )
        sys.exit(1)


def get_ingress_port(istio_enabled: bool, override_port: str) -> str:
    """
    Gets the port for ingress depending on istio enabled or the override port argument is given
    :return: string representing the port number
    """
    if override_port is not None:
        return override_port
    config = get_ingress_ports_config()
    return config['istio_port'] if istio_enabled else config['nginx_port']


def get_ingress_ports_config() -> dict[str, str]:
    """
    Gets the values from the ingress-ports-config config map in the gms namespace
    :return: Dictionary with key value pairs representing the config map data
    """
    return_code, out, err = run_kubectl_get(namespace='gms',
                                            resource_type='configmap',
                                            resource_name='ingress-ports-config')

    if return_code != 0:  # pragma: no coverage
        print_error("Unable to get gms configmap ingress-ports-config")
        exit(return_code)

    config = json.loads(out)
    return config['data']


def get_rancher_project_id() -> str | None:
    """
    Gets the values from the rancher-project-config config map in the gms namespace
    :return: rancher project id string or None
    """
    return_code, out, err = run_kubectl_get(namespace='gms',
                                            resource_type='configmap',
                                            resource_name='rancher-project-config')

    if return_code != 0:
        logging.debug('Configmap gms/rancher-project-config not found')
        return None

    config = json.loads(out)
    return config['data']['id']


def is_instance_istio(name: str) -> bool:
    """
    Tests if the namespace is istio enabled
    :return: True if namespace has istio-injection=enabled, False otherwise
    """
    return_code, out, err = run_kubectl_get(namespace=name,
                                            resource_type='namespace',
                                            resource_name=name)

    if return_code != 0:
        print_error("Unable to get namespace details")
        exit(return_code)

    ns_data = json.loads(out)

    try:
        return ns_data['metadata']['labels']['istio-injection'] == 'enabled'
    except KeyError:
        return False


def get_instance_labels(name: str) -> dict[str, str]:
    """
    Gets the gms labels for a single instance
    :param name: Name of the instance
    :return: Dictionary with gms key value pairs representing the labels
    """
    return_code, out, err = run_kubectl_get(namespace=name,
                                            resource_type='configmap',
                                            resource_name='gms')

    if return_code != 0:
        print_error(f'Unable to get gms configmap for {name}: {err}')
        return {}

    configmap_data = json.loads(out)

    logging.debug(f'Labels for ConfigMap "gms" in Namespace "{name}"')
    logging.debug(configmap_data['metadata']['labels'])

    return configmap_data['metadata']['labels']


def get_injector_set_args(
    injector: bool,
    injector_dataset: str
) -> list[str]:
    """
    Returns a list containing set arguments needed for the injector
    :param injector: boolean indicating if the injector should be enabled
    :param injector_dataset: name of the dataset for the injector
    :return: list with --set arguments for helm
    """

    cmd = []
    if injector:
        cmd.extend(
            shlex.split(
                '--set global.injector=True '
                '--set augmentation.cd11-injector.enabled=True '
            )
        )
    if injector_dataset is not None:
        cmd.extend(
            shlex.split(
                f'--set "augmentation.cd11-injector.env.CD11_INJECTOR_CONFIG_NAME={injector_dataset}"'
            )
        )

    return cmd


def get_livedata_set_args(
    livedata: bool,
    connman_port: str,
    connman_data_manager_ip: str,
    connman_data_provider_ip: str,
    dataman_ports: str
) -> list[str]:
    """
    Returns a list containing set arguments needed for live data
    :param livedata: boolean indicating if live data should be enabled
    :param connman_port: well known port for CD11 connman service
    :param connman_data_manager_ip: external IP address of the CD11 dataman service
    :param connman_data_provider_ip: IP address of the data provider sending data to the CD11 dataman service
    :param dataman_ports: port range for the CD11 dataman service
    :return: list with --set arguments for helm
    """

    cmd = []
    if livedata:
        cmd.extend(shlex.split('--set "global.liveData=True"'))
    if connman_port is not None:
        cmd.extend(shlex.split(f'--set "da-connman.connPort={connman_port}"'))
    if connman_data_manager_ip is not None:
        cmd.extend(
            shlex.split(
                f'--set "da-connman.env.GMS_CONFIG_CONNMAN__DATA_MANAGER_IP_ADDRESS={connman_data_manager_ip}"'
            )
        )
    if connman_data_provider_ip is not None:
        cmd.extend(
            shlex.split(
                f'--set "da-connman.env.GMS_CONFIG_CONNMAN__DATA_PROVIDER_IP_ADDRESS={connman_data_provider_ip}"'
            )
        )
    if dataman_ports is not None:
        cmd.extend(
            shlex.split(
                f'--set "da-dataman.dataPortStart={dataman_ports.split("-")[0]}"'
            )
        )
        cmd.extend(
            shlex.split(
                f'--set "da-dataman.dataPortEnd={dataman_ports.split("-")[1]}"'
            )
        )

    return cmd


def create_namespace(namespace: str, instance_type: str, is_istio: bool) -> None:
    """
    Create a new kubernetes namespace. Adds istio label and rancher
    project annotations if applicable.

    Args:
        namespace: Name of the namespace to create
        instance_type: Name of the instance type
        is_istio: If true, adds the istio-injection label to the namespace

    Raises:
        SystemExit:  If there's a failure in either creating, labeling,
            or annotating the namespace.
    """

    return_code, out, err = run_kubectl_create_namespace(namespace=namespace)
    if return_code > 0:
        exit(return_code)

    # standard labels
    labels = (f'app.kubernetes.io/instance={namespace} '
              f'app.kubernetes.io/name={instance_type} '
              f'app.kubernetes.io/part-of={instance_type} ')

    if is_istio:
        console.log('Adding "istio-injection=enabled" label')
        labels += 'istio-injection=enabled '

    return_code, out, err = run_kubectl_label_namespace(namespace=namespace, label=labels)
    if return_code > 0:
        exit(return_code)

    rancher_project_id = get_rancher_project_id()
    if rancher_project_id is not None:
        console.log('Adding rancher project annotation')
        return_code, out, err = run_kubectl_annotate_namespace(
            namespace=namespace,
            annotation=f'field.cattle.io/projectId="{rancher_project_id}"')
        if return_code > 0:
            exit(return_code)


def delete_unmanaged_resources(namespace: str) -> None:
    """
    Deletes legacy resources that are not managed by Helm. This function
    is for backwards compatiblity with instances installed with unmanaged
    secrets/configmaps. It will delete the unmanaged resources during
    upgrade and Helm will recreate them. This can be removed in PI22.

    Args:
        namespace: Name of the namespace to delete resources
    """
    run_kubectl_delete_unmanaged_resource(namespace, 'secret', 'ingress-default-cert')
    run_kubectl_delete_unmanaged_resource(namespace, 'secret', 'oracle-wallet')
    run_kubectl_delete_unmanaged_resource(namespace, 'configmap', 'ldap-ca-cert')
    run_kubectl_delete_unmanaged_resource(namespace, 'secret', 'oracle-wallet')
    run_kubectl_delete_unmanaged_resource(namespace, 'secret', 'ldap-bindpass')
    run_kubectl_delete_unmanaged_resource(namespace, 'configmap', 'logging-ldap-config')


def run_command(command: str | list[str], print_output: bool = True, stdin: bool = None) -> tuple[int, str]:
    """
    Execute the specified system command. This will always be executed with /deploy as the PWD.
    :param command: The command string or list to execute. It is best to build this with shlex.split(), but if you don't it will be done internally.
    :param print_output: If True, print the stdout from the command
    :param stdin: The optional "input" argument should be data to be sent to the child process, or None, if no data should be sent to the child.
    :return: A tuple of the command return code, stdout string, and stderr string
    """

    if type(command) is not list:
        command = shlex.split(command)

    logging.debug(f'Running command: {" ".join(command)}')

    # always change to /deploy since this is where helm expects to be running
    # For debugging, you can either create /deploy on your machine, create a soft-link, or temporarily change this value
    # This is hardcoded to prevent a critical fortify finding
    os.chdir("/deploy")

    cmd = subprocess.Popen(
        command,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        stdin=subprocess.PIPE
    )
    out, err = cmd.communicate(input=(stdin.encode() if stdin else None))
    out = out.decode()
    err = err.decode()

    if print_output:
        console.log(out)
        if len(err) > 0:
            print_warning(f'{err}')

    return cmd.returncode, out, err


def print_warning(message: str) -> None:
    """
    Print a warning message in bold yellow.
    :param message: Message string to print
    """
    console.log(f'[bold yellow]\[WARNING] {message}')


def print_error(message: str) -> None:
    """
    Print an error message in bold red.
    :param message: Message string to print
    """
    console.log(f'[bold red]\[ERROR] {message}')


def is_valid_fqdn(s: str) -> bool:
    """
    Validate if string is a fully qualified domain name.
    Hostnames are composed of a series of labels concatenated with dots. Each label is 1 to 63 characters
    long, and may contain: the ASCII letters a-z and A-Z, the digits 0-9, and the hyphen ('-').
    Additionally: labels cannot start or end with hyphens (RFC 952) labels can start with numbers
    (RFC 1123) trailing dot is not allowed max length of ascii hostname including dots is 253 characters
    TLD (last label) is at least 2 characters and only ASCII letters we want at least 1 level above TLD.
    :param s: string that represent the FQDN to test
    :return: True if the string is a valid FQDN, False otherwise.
    """

    pat = re.compile(
        r'(?=^.{4,253}$)(^((?!-)[a-zA-Z0-9-]{0,62}[a-zA-Z0-9]\.)+[a-zA-Z]{2,63}$)'
    )
    return bool(pat.match(s))


# External run commands - each command is its own function so it can be mocked out during unit test
# These all have "pragma: no coverage" because they are always mocked out in unit tests
# Also none of these have their own unit tests since they call external commands
def run_helm_list() -> tuple[int, str]:  # pragma: no coverage
    return run_command(
        'helm list --all --all-namespaces --output json',
        print_output=False
    )


def run_helm_install(command: str | list[str]) -> tuple[int, str]:  # pragma: no coverage
    return run_command(command, print_output=True)


def run_helm_get_values(namespace: str) -> tuple[int, str]:  # pragma: no coverage
    return run_command(
        f'helm get values {namespace} --all --namespace {namespace}',
        print_output=False
    )


def run_helm_uninstall(namespace: str) -> tuple[int, str]:  # pragma: no coverage
    return run_command(
        f'helm uninstall {namespace} --namespace {namespace}',
        print_output=True
    )


def run_helm_upgrade(command: str | list[str]) -> tuple[int, str]:  # pragma: no coverage
    return run_command(command, print_output=True)


def run_kubectl_label_namespace(namespace: str, label: str) -> tuple[int, str]:  # pragma: no coverage
    return run_command(
        f'kubectl label namespace {namespace} {label}',
        print_output=True
    )


def run_kubectl_annotate_namespace(namespace: str, annotation: str) -> tuple[int, str]:  # pragma: no coverage
    return run_command(
        f'kubectl annotate namespace {namespace} {annotation}',
        print_output=True
    )


def run_kubectl_create_namespace(namespace: str) -> tuple[int, str]:  # pragma: no coverage
    return run_command(
        f'kubectl create namespace {namespace}',
        print_output=True
    )


def run_kubectl_delete_namespace(namespace: str) -> tuple[int, str]:  # pragma: no coverage
    return run_command(
        f'kubectl delete namespace {namespace}',
        print_output=True
    )


def run_kubectl_rollout_restart(namespace: str, resource: str) -> tuple[int, str]:  # pragma: no coverage
    return run_command(
        f'kubectl rollout restart {resource} --namespace {namespace}',
        print_output=True
    )


def run_kubectl_get(
    namespace: str,
    resource_type: str,
    resource_name: str
) -> tuple[int, str]:  # pragma: no coverage
    return run_command(
        f'kubectl --namespace {namespace} get {resource_type} {resource_name} --output json',
        print_output=False
    )


def run_kubectl_get_configmap_all_namespaces(
    configmap_name: str
) -> tuple[int, str]:  # pragma: no coverage
    # must use --field-selector with --all-namespaces to get configmaps by name
    return run_command(
        f'kubectl get configmap --all-namespaces --field-selector metadata.name=={configmap_name} --output json',
        print_output=False
    )


def run_kubectl_get_deployments_restart_after_reconfig(
    namespace: str
) -> tuple[int, str]:  # pragma: no coverage
    return run_command(
        f'kubectl get deployment --selector restartAfterReconfig=true '
        f'--namespace {namespace} --output json',
        print_output=False
    )


def run_kubectl_get_all_helm_resources(namespace: str) -> tuple[int, str]:  # pragma: no coverage
    return run_command(
        f"kubectl get daemonset,deployment,replicaset,statefulset,pvc --no-headers "
        f"--selector='app.kubernetes.io/managed-by==Helm' "
        f"--output name --namespace {namespace}",
        print_output=False
    )


def run_kubectl_apply(
    kube_object: str,
    print_output: bool = False,
    dry_run: bool = False
) -> tuple[int, str]:  # pragma: no coverage
    if dry_run:
        return run_command(
            'kubectl apply --dry-run=client -f -',
            print_output=print_output,
            stdin=kube_object
        )
    else:
        return run_command(
            'kubectl apply -f -',
            print_output=print_output,
            stdin=kube_object
        )


def run_kubectl_auth() -> tuple[int, str]:  # pragma: no coverage
    return run_command(
        "kubectl auth can-i get '*' --quiet",
        print_output=False
    )


def run_kubectl_delete_unmanaged_resource(
    namespace: str,
    resource_type: str,
    resource_name: str
) -> tuple[int, str]:  # pragma: no coverage
    return run_command(
        f"kubectl delete {resource_type} "
        f"--field-selector='metadata.name={resource_name}' "
        "--selector='app.kubernetes.io/managed-by!=Helm' "
        "--ignore-not-found=true "
        f"--namespace {namespace}",
        print_output=False
    )
