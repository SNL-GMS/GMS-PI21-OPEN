import json.decoder
import shlex

import pytest

from gmskube import gmskube
from test.helpers import get_test_custom_chart_path, get_test_file_contents, get_config_overrides_path, get_request_response


# ----- Uninstall tests
def test_uninstall_success(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.run_helm_uninstall',
        return_value=(0,
                      "",
                      "")
    )
    mocker.patch(
        'gmskube.gmskube.run_kubectl_get_all_helm_resources',
        return_value=(0,
                      "",
                      "")
    )
    mocker.patch(
        'gmskube.gmskube.run_kubectl_delete_namespace',
        return_value=(0,
                      "",
                      "")
    )
    gmskube.uninstall(instance_name="test", timeout=4)

    out, err = capsys.readouterr()
    assert 'Uninstalling test' in out
    assert 'Running helm uninstall' in out
    assert 'Deleting namespace' in out
    assert 'test uninstall complete' in out


def test_uninstall_helm_uninstall_fail(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.run_helm_uninstall',
        return_value=(1,
                      "",
                      "")
    )
    mocker.patch(
        'gmskube.gmskube.run_kubectl_get_all_helm_resources',
        return_value=(0,
                      "",
                      "")
    )
    mocker.patch(
        'gmskube.gmskube.run_kubectl_delete_namespace',
        return_value=(0,
                      "",
                      "")
    )
    gmskube.uninstall(instance_name="test", timeout=4)

    out, err = capsys.readouterr()
    expected = [
        "Helm uninstall unsuccessful, will attempt to delete the namespace "
        "anyway",
        "Deleting namespace",
        "test uninstall complete"
    ]
    for line in expected:
        for word in line.split():
            assert word in out


def test_uninstall_namespace_delete_fail(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.run_helm_uninstall',
        return_value=(0,
                      "",
                      "")
    )
    mocker.patch(
        'gmskube.gmskube.run_kubectl_get_all_helm_resources',
        return_value=(0,
                      "",
                      "")
    )
    mocker.patch(
        'gmskube.gmskube.run_kubectl_delete_namespace',
        return_value=(1,
                      "",
                      "")
    )
    gmskube.uninstall(instance_name="test", timeout=4)

    out, err = capsys.readouterr()
    expected = (
        "test uninstall unsuccessful, please review errors/warnings above"
    )
    for word in expected.split():
        assert word in out
    assert 'test uninstall complete' not in out


# ----- Install tests
def test_install_soh_livedata(mocker, capsys):
    mocker.patch('gmskube.gmskube.create_namespace', return_value=None)
    mocker.patch('gmskube.gmskube.run_helm_install', return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.request_dataload', return_value=True)
    mocker.patch('gmskube.gmskube.get_ingress_port', return_value='443')
    mocker.patch(
        'gmskube.gmskube.get_base_domain',
        return_value='test.cluster.local'
    )
    gmskube.install(
        instance_name="test",
        timeout=4,
        sets=["name=value"],
        set_strings=["name=value"],
        values=None,
        injector=None,
        injector_dataset=None,
        livedata=True,
        connman_port="123",
        connman_data_manager_ip="127.0.0.1",
        connman_data_provider_ip="127.0.0.1",
        dataman_ports="123-456",
        wallet_path=None,
        custom_chart_path=None,
        instance_type="soh",
        image_tag="test",
        config_override_path="",
        dry_run=False,
        is_istio=False,
        ingress_port=None,
        augmentations=None
    )

    out, err = capsys.readouterr()
    expected = [
        "Installing test",
        "Getting ingress port",
        "Ingress port: 443",
        "Setting up namespace test",
        "Running helm install",
        "Beginning data load",
        "test installed successfully"
    ]
    for line in expected:
        for word in line.split():
            assert word in out


def test_install_soh_injector(mocker, capsys):
    mocker.patch('gmskube.gmskube.create_namespace', return_value=None)
    mocker.patch('gmskube.gmskube.run_helm_install', return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.request_dataload', return_value=True)
    mocker.patch('gmskube.gmskube.get_ingress_port', return_value='443')
    mocker.patch(
        'gmskube.gmskube.get_base_domain',
        return_value='test.cluster.local'
    )
    mocker.patch('gmskube.gmskube.augment_apply', return_value=None)

    gmskube.install(
        instance_name="test",
        timeout=4,
        sets=["name=value"],
        set_strings=None,
        values="/tmp/test",
        injector=True,
        injector_dataset="test",
        livedata=False,
        connman_port=None,
        connman_data_manager_ip=None,
        connman_data_provider_ip=None,
        dataman_ports=None,
        wallet_path=None,
        custom_chart_path=None,
        instance_type="soh",
        image_tag="test",
        config_override_path="",
        dry_run=False,
        is_istio=False,
        ingress_port=None,
        augmentations=None
    )

    out, err = capsys.readouterr()
    expected = [
        "Installing test",
        "Getting ingress port",
        "Ingress port: 443",
        "Setting up namespace test",
        "Running helm install",
        "Beginning data load",
        "Adding cd11-injector",
        "test installed successfully"
    ]
    for line in expected:
        for word in line.split():
            assert word in out


def test_install_custom_chart(mocker, capsys):
    mocker.patch('gmskube.gmskube.create_namespace', return_value=None)
    mocker.patch('gmskube.gmskube.run_helm_install', return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.request_dataload', return_value=True)
    mocker.patch('gmskube.gmskube.get_ingress_port', return_value='443')
    mocker.patch(
        'gmskube.gmskube.get_base_domain',
        return_value='test.cluster.local'
    )
    mocker.patch('gmskube.gmskube.augment_apply', return_value=None)

    gmskube.install(
        instance_name="test",
        timeout=4,
        sets=["name=value"],
        set_strings=None,
        values=None,
        injector=True,
        injector_dataset=None,
        livedata=False,
        connman_port=None,
        connman_data_manager_ip=None,
        connman_data_provider_ip=None,
        dataman_ports=None,
        wallet_path=None,
        custom_chart_path=get_test_custom_chart_path(),
        instance_type=None,
        image_tag="test",
        config_override_path="",
        dry_run=False,
        is_istio=False,
        ingress_port=None,
        augmentations=None
    )

    out, err = capsys.readouterr()


def test_install_dry_run(mocker, capsys):
    mocker.patch('gmskube.gmskube.create_namespace', return_value=None)
    mocker.patch('gmskube.gmskube.run_helm_install', return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.request_dataload', return_value=True)
    mocker.patch('gmskube.gmskube.get_ingress_port', return_value='443')
    mocker.patch(
        'gmskube.gmskube.get_base_domain',
        return_value='test.cluster.local'
    )

    # dry-run causes a SystemExit
    with pytest.raises(SystemExit):
        gmskube.install(
            instance_name="test",
            timeout=4,
            sets=None,
            set_strings=None,
            values=None,
            injector=False,
            injector_dataset=None,
            livedata=False,
            connman_port=None,
            connman_data_manager_ip=None,
            connman_data_provider_ip=None,
            dataman_ports=None,
            wallet_path=None,
            custom_chart_path=None,
            instance_type="soh",
            image_tag="test",
            config_override_path=None,
            dry_run=True,
            is_istio=False,
            ingress_port=None,
            augmentations=None
        )

    out, err = capsys.readouterr()
    assert 'Setting up namespace test' not in out
    assert 'test installed successfully' not in out


def test_install_istio(mocker, capsys):
    mocker.patch('gmskube.gmskube.create_namespace', return_value=None)
    mocker.patch('gmskube.gmskube.run_helm_install', return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.request_dataload', return_value=True)
    mocker.patch('gmskube.gmskube.get_ingress_port', return_value='8443')
    mocker.patch(
        'gmskube.gmskube.run_kubectl_label_namespace',
        return_value=(0,
                      "",
                      "")
    )
    mocker.patch(
        'gmskube.gmskube.get_base_domain',
        return_value='test.cluster.local'
    )

    gmskube.install(
        instance_name="test",
        timeout=4,
        sets=None,
        set_strings=None,
        values=None,
        injector=False,
        injector_dataset=None,
        livedata=False,
        connman_port=None,
        connman_data_manager_ip=None,
        connman_data_provider_ip=None,
        dataman_ports=None,
        wallet_path=None,
        custom_chart_path=None,
        instance_type="soh",
        image_tag="test",
        config_override_path=None,
        dry_run=False,
        is_istio=True,
        ingress_port=None,
        augmentations=None
    )

    out, err = capsys.readouterr()
    assert 'Ingress port: 8443' in out


def test_install_with_augmentations(mocker, capsys):
    mocker.patch('gmskube.gmskube.create_namespace', return_value=None)
    mocker.patch('gmskube.gmskube.run_helm_install', return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.request_dataload', return_value=True)
    mocker.patch('gmskube.gmskube.get_ingress_port', return_value='443')
    mocker.patch('gmskube.gmskube.augment_apply', return_value=None)
    mocker.patch(
        'gmskube.gmskube.get_base_domain',
        return_value='test.cluster.local'
    )
    gmskube.install(
        instance_name="test",
        timeout=4,
        sets=None,
        set_strings=None,
        values=None,
        injector=True,
        injector_dataset="test",
        livedata=False,
        connman_port=None,
        connman_data_manager_ip=None,
        connman_data_provider_ip=None,
        dataman_ports=None,
        wallet_path=None,
        custom_chart_path=None,
        instance_type="soh",
        image_tag="test",
        config_override_path="",
        dry_run=False,
        is_istio=False,
        ingress_port=None,
        augmentations=['test1',
                       'test2']
    )

    out, err = capsys.readouterr()
    assert 'Enabling augmentation test1' in out
    assert 'Enabling augmentation test2' in out


def test_install_with_augmentations_sets(mocker, capsys):
    mocker.patch('gmskube.gmskube.create_namespace', return_value=None)
    mocker.patch('gmskube.gmskube.run_helm_install', return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.request_dataload', return_value=True)
    mocker.patch('gmskube.gmskube.get_ingress_port', return_value='443')
    mocker.patch('gmskube.gmskube.augment_apply', return_value=None)
    mocker.patch(
        'gmskube.gmskube.get_base_domain',
        return_value='test.cluster.local'
    )
    gmskube.install(
        instance_name="test",
        timeout=4,
        sets=['name=value'],
        set_strings=None,
        values=None,
        injector=True,
        injector_dataset="test",
        livedata=False,
        connman_port=None,
        connman_data_manager_ip=None,
        connman_data_provider_ip=None,
        dataman_ports=None,
        wallet_path=None,
        custom_chart_path=None,
        instance_type="soh",
        image_tag="test",
        config_override_path="",
        dry_run=False,
        is_istio=False,
        ingress_port=None,
        augmentations=['test1',
                       'test2']
    )

    out, err = capsys.readouterr()
    assert 'Enabling augmentation test1' in out
    assert 'Enabling augmentation test2' in out


def test_install_dataload_fail(mocker, capsys):
    mocker.patch('gmskube.gmskube.create_namespace', return_value=None)
    mocker.patch('gmskube.gmskube.run_helm_install', return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.request_dataload', return_value=False)
    mocker.patch('gmskube.gmskube.get_ingress_port', return_value='443')
    mocker.patch(
        'gmskube.gmskube.get_base_domain',
        return_value='test.cluster.local'
    )
    with pytest.raises(SystemExit):
        gmskube.install(
            instance_name="test",
            timeout=4,
            sets=["name=value"],
            set_strings=None,
            values=None,
            injector=True,
            injector_dataset="test",
            livedata=True,
            connman_port="123",
            connman_data_manager_ip="127.0.0.1",
            connman_data_provider_ip="127.0.0.1",
            dataman_ports="123-456",
            wallet_path=None,
            custom_chart_path=None,
            instance_type="soh",
            image_tag="test",
            config_override_path="",
            dry_run=False,
            is_istio=False,
            ingress_port=None,
            augmentations=None
        )

    out, err = capsys.readouterr()
    expected = "Data load failed to execute successfully, Exiting"
    for word in expected.split():
        assert word in out


def test_install_helm_install_fail(mocker, capsys):
    mocker.patch('gmskube.gmskube.create_namespace', return_value=None)
    mocker.patch('gmskube.gmskube.run_helm_install', return_value=(1, "", ""))
    mocker.patch('gmskube.gmskube.get_ingress_port', return_value='443')
    mocker.patch(
        'gmskube.gmskube.get_base_domain',
        return_value='test.cluster.local'
    )

    with pytest.raises(SystemExit):
        gmskube.install(
            instance_name="test",
            timeout=4,
            sets=["name=value"],
            set_strings=None,
            values=None,
            injector=True,
            injector_dataset="test",
            livedata=False,
            connman_port=None,
            connman_data_manager_ip=None,
            connman_data_provider_ip=None,
            dataman_ports=None,
            wallet_path=None,
            custom_chart_path=None,
            instance_type="soh",
            image_tag="test",
            config_override_path="",
            dry_run=False,
            is_istio=False,
            ingress_port=None,
            augmentations=None
        )

    out, err = capsys.readouterr()
    assert 'Could not install instance test' in out
    assert 'test installed successfully' not in out


# ----- Upgrade tests
def test_upgrade_soh_livedata(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.get_instance_labels',
        return_value={'gms/type': 'soh'}
    )
    mocker.patch(
        'gmskube.gmskube.run_helm_get_values',
        return_value=(
            0,
            get_test_file_contents('cmds/run_helm_get_values.yaml'),
            ""
        )
    )
    mocker.patch('gmskube.gmskube.run_helm_upgrade', return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.delete_unmanaged_resources', return_value=None)

    gmskube.upgrade(
        instance_name="test",
        sets=["name=value"],
        set_strings=["name=value"],
        values="/tmp/test",
        injector=False,
        injector_dataset=None,
        livedata=True,
        connman_port="123",
        connman_data_manager_ip="127.0.0.1",
        connman_data_provider_ip="127.0.0.1",
        dataman_ports="123-456",
        custom_chart_path=None,
        image_tag="test",
        dry_run=False
    )

    out, err = capsys.readouterr()
    assert 'Upgrading test' in out
    assert 'Getting instance type' in out
    assert 'Instance type is: soh' in out
    assert 'Getting existing helm values' in out
    assert 'Saving existing helm values to a temporary file' in out
    assert 'Running helm upgrade' in out
    assert 'test upgrade complete!' in out


def test_upgrade_soh_injector(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.get_instance_labels',
        return_value={'gms/type': 'soh'}
    )
    mocker.patch(
        'gmskube.gmskube.run_helm_get_values',
        return_value=(
            0,
            get_test_file_contents('cmds/run_helm_get_values.yaml'),
            ""
        )
    )
    mocker.patch('gmskube.gmskube.run_helm_upgrade', return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.delete_unmanaged_resources', return_value=None)

    gmskube.upgrade(
        instance_name="test",
        sets=["name=value"],
        set_strings=None,
        values=None,
        injector=True,
        injector_dataset="test",
        livedata=False,
        connman_port=None,
        connman_data_manager_ip=None,
        connman_data_provider_ip=None,
        dataman_ports=None,
        custom_chart_path=None,
        image_tag="test",
        dry_run=False
    )

    out, err = capsys.readouterr()
    assert 'Upgrading test' in out
    assert 'Getting instance type' in out
    assert 'Instance type is: soh' in out
    assert 'Getting existing helm values' in out
    assert 'Saving existing helm values to a temporary file' in out
    assert 'Running helm upgrade' in out
    assert 'test upgrade complete!' in out


def test_upgrade_dry_run(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.get_instance_labels',
        return_value={'gms/type': 'soh'}
    )
    mocker.patch(
        'gmskube.gmskube.run_helm_get_values',
        return_value=(
            0,
            get_test_file_contents('cmds/run_helm_get_values.yaml'),
            ""
        )
    )
    mocker.patch('gmskube.gmskube.run_helm_upgrade', return_value=(0, "", ""))

    gmskube.upgrade(
        instance_name="test",
        sets=None,
        set_strings=None,
        values=None,
        injector=False,
        injector_dataset=None,
        livedata=False,
        connman_port=None,
        connman_data_manager_ip=None,
        connman_data_provider_ip=None,
        dataman_ports=None,
        custom_chart_path=None,
        image_tag="test",
        dry_run=True
    )

    out, err = capsys.readouterr()
    assert 'Upgrading test' in out
    assert 'Getting instance type' in out
    assert 'Instance type is: soh' in out
    assert 'Getting existing helm values' in out
    assert 'Saving existing helm values to a temporary file' in out
    assert 'Running helm upgrade' in out
    assert 'test upgrade complete!' in out


def test_upgrade_custom_chart(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.run_helm_get_values',
        return_value=(
            0,
            get_test_file_contents('cmds/run_helm_get_values.yaml'),
            ""
        )
    )
    mocker.patch('gmskube.gmskube.run_helm_upgrade', return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.delete_unmanaged_resources', return_value=None)

    gmskube.upgrade(
        instance_name="test",
        sets=None,
        set_strings=None,
        values=None,
        injector=False,
        injector_dataset=None,
        livedata=False,
        connman_port=None,
        connman_data_manager_ip=None,
        connman_data_provider_ip=None,
        dataman_ports=None,
        custom_chart_path=get_test_custom_chart_path(),
        image_tag="test",
        dry_run=False
    )

    out, err = capsys.readouterr()
    assert 'Instance type is: custom' in out


def test_upgrade_get_instance_type_fail(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.get_instance_labels',
        return_value={'invalid': 'value'}
    )

    with pytest.raises(SystemExit):
        gmskube.upgrade(
            instance_name="test",
            sets=None,
            set_strings=None,
            values=None,
            injector=False,
            injector_dataset=None,
            livedata=False,
            connman_port=None,
            connman_data_manager_ip=None,
            connman_data_provider_ip=None,
            dataman_ports=None,
            custom_chart_path=None,
            image_tag="test",
            dry_run=False
        )

    out, err = capsys.readouterr()
    assert 'Could not determine the type for instance' in out


def test_upgrade_helm_get_values_fail(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.get_instance_labels',
        return_value={'gms/type': 'soh'}
    )
    mocker.patch(
        'gmskube.gmskube.run_helm_get_values',
        return_value=(1,
                      "",
                      "helm get values failed")
    )
    mocker.patch('gmskube.gmskube.delete_unmanaged_resources', return_value=None)

    with pytest.raises(SystemExit):
        gmskube.upgrade(
            instance_name="test",
            sets=["name=value"],
            set_strings=None,
            values=None,
            injector=True,
            injector_dataset="test",
            livedata=False,
            connman_port=None,
            connman_data_manager_ip=None,
            connman_data_provider_ip=None,
            dataman_ports=None,
            custom_chart_path=None,
            image_tag="test",
            dry_run=False
        )

    out, err = capsys.readouterr()
    expected = (
        "Unable to get existing values for instance test: helm get values "
        "failed"
    )
    for word in expected.split():
        assert word in out


def test_upgrade_helm_upgrade_fail(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.get_instance_labels',
        return_value={'gms/type': 'soh'}
    )
    mocker.patch(
        'gmskube.gmskube.run_helm_get_values',
        return_value=(
            0,
            get_test_file_contents('cmds/run_helm_get_values.yaml'),
            ""
        )
    )
    mocker.patch(
        'gmskube.gmskube.run_helm_upgrade',
        return_value=(1,
                      "",
                      "helm upgrade failed")
    )
    mocker.patch('gmskube.gmskube.delete_unmanaged_resources', return_value=None)

    with pytest.raises(SystemExit):
        gmskube.upgrade(
            instance_name="test",
            sets=["name=value"],
            set_strings=None,
            values=None,
            injector=True,
            injector_dataset="test",
            livedata=False,
            connman_port=None,
            connman_data_manager_ip=None,
            connman_data_provider_ip=None,
            dataman_ports=None,
            custom_chart_path=None,
            image_tag="test",
            dry_run=False
        )

    out, err = capsys.readouterr()
    expected = "Could not upgrade instance test: helm upgrade failed"
    for word in expected.split():
        assert word in out


# ----- Reconfig tests
def test_reconfig(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.get_base_domain',
        return_value='test.cluster.local'
    )
    mocker.patch('gmskube.gmskube.request_dataload', return_value=True)
    mocker.patch('gmskube.gmskube.is_instance_istio', return_value=False)
    mocker.patch('gmskube.gmskube.get_ingress_port', return_value='443')
    mocker.patch(
        'gmskube.gmskube.run_kubectl_get_deployments_restart_after_reconfig',
        return_value=(
            0,
            get_test_file_contents(
                'cmds/run_kubectl_get_deployments_restart.json'
            ),
            ""
        )
    )
    mocker.patch(
        'gmskube.gmskube.run_kubectl_rollout_restart',
        return_value=(0,
                      "",
                      "")
    )
    gmskube.reconfig(
        instance_name="test",
        timeout=4,
        config_override_path="test",
        ingress_port=None
    )

    out, err = capsys.readouterr()
    expected = [
        "Reconfiguring test",
        "Getting instance istio status",
        "Instance istio status: False",
        "Getting ingress port",
        "Ingress port: 443",
        "Beginning data load",
        "Rollout restart deployments",
        "Getting list of deployments with label \"restartAfterReconfig=true\"",
        "Restarting deployment acei-merge-processor",
        "Restarting deployment cd11-rsdf-processor",
        "Restarting deployment da-connman",
        "test reconfig complete"
    ]
    for line in expected:
        for word in line.split():
            assert word in out


def test_reconfig_dataload_fail(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.get_base_domain',
        return_value='test.cluster.local'
    )
    mocker.patch('gmskube.gmskube.request_dataload', return_value=False)
    mocker.patch('gmskube.gmskube.is_instance_istio', return_value=False)
    mocker.patch('gmskube.gmskube.get_ingress_port', return_value='443')

    with pytest.raises(SystemExit):
        gmskube.reconfig(
            instance_name="test",
            timeout=4,
            config_override_path="test",
            ingress_port=None
        )

    out, err = capsys.readouterr()
    expected = "Data load failed to execute successfully, Exiting"
    for word in expected.split():
        assert word in out


def test_reconfig_get_deployments_restart_after_reconfig_fail(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.get_base_domain',
        return_value='test.cluster.local'
    )
    mocker.patch('gmskube.gmskube.request_dataload', return_value=True)
    mocker.patch('gmskube.gmskube.is_instance_istio', return_value=False)
    mocker.patch('gmskube.gmskube.get_ingress_port', return_value='443')
    mocker.patch(
        'gmskube.gmskube.run_kubectl_get_deployments_restart_after_reconfig',
        return_value=(1,
                      "",
                      "get deployments restart failed")
    )

    with pytest.raises(SystemExit):
        gmskube.reconfig(
            instance_name="test",
            timeout=4,
            config_override_path="test",
            ingress_port=None
        )

    out, err = capsys.readouterr()
    expected = (
        "Unable to get list of deployment requiring restart: get deployments "
        "restart failed"
    )
    for word in expected.split():
        assert word in out


# ----- List tests
def test_list(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.run_helm_list',
        return_value=(
            0,
            get_test_file_contents('cmds/run_helm_list.json'),
            ""
        )
    )
    mocker.patch(
        'gmskube.gmskube.run_kubectl_get_configmap_all_namespaces',
        return_value=(
            0,
            get_test_file_contents(
                'cmds/run_kubectl_get_configmap_all_namespaces.json'
            ),
            ""
        )
    )

    gmskube.list_instances(username=None, instance_type=None, show_all=False)

    out, err = capsys.readouterr()
    assert 'fleet-agent-local                  deployed     ?          ?               ?                    -                ?' not in out
    assert 'grafana                            deployed     grafana    testuser        2021-11-23T160029Z   -                develop' in out
    assert 'logging                            deployed     logging    otheruser       2021-11-24T013156Z   -                develop' in out
    assert 'test                               deployed     ian        testuser        2021-12-20T210438Z   8041,9000-9090   develop' in out


def test_list_show_all(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.run_helm_list',
        return_value=(
            0,
            get_test_file_contents('cmds/run_helm_list.json'),
            ""
        )
    )
    mocker.patch(
        'gmskube.gmskube.run_kubectl_get_configmap_all_namespaces',
        return_value=(
            0,
            get_test_file_contents(
                'cmds/run_kubectl_get_configmap_all_namespaces.json'
            ),
            ""
        )
    )

    gmskube.list_instances(username=None, instance_type=None, show_all=True)

    out, err = capsys.readouterr()
    assert 'fleet-agent-local                  deployed     ?          ?               ?                    -                ?' in out
    assert 'grafana                            deployed     grafana    testuser        2021-11-23T160029Z   -                develop' in out
    assert 'logging                            deployed     logging    otheruser       2021-11-24T013156Z   -                develop' in out
    assert 'test                               deployed     ian        testuser        2021-12-20T210438Z   8041,9000-9090   develop' in out


def test_list_username(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.run_helm_list',
        return_value=(
            0,
            get_test_file_contents('cmds/run_helm_list.json'),
            ""
        )
    )
    mocker.patch(
        'gmskube.gmskube.run_kubectl_get_configmap_all_namespaces',
        return_value=(
            0,
            get_test_file_contents(
                'cmds/run_kubectl_get_configmap_all_namespaces.json'
            ),
            ""
        )
    )

    gmskube.list_instances(
        username='testuser',
        instance_type=None,
        show_all=False
    )

    out, err = capsys.readouterr()
    assert 'fleet-agent-local                  deployed     ?          ?               ?                    -                ?' not in out
    assert 'grafana                            deployed     grafana    testuser        2021-11-23T160029Z   -                develop' in out
    assert 'logging                            deployed     logging    otheruser       2021-11-24T013156Z   -                develop' not in out


def test_list_instance_type(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.run_helm_list',
        return_value=(
            0,
            get_test_file_contents('cmds/run_helm_list.json'),
            ""
        )
    )
    mocker.patch(
        'gmskube.gmskube.run_kubectl_get_configmap_all_namespaces',
        return_value=(
            0,
            get_test_file_contents(
                'cmds/run_kubectl_get_configmap_all_namespaces.json'
            ),
            ""
        )
    )

    gmskube.list_instances(
        username=None,
        instance_type='logging',
        show_all=False
    )

    out, err = capsys.readouterr()
    assert 'fleet-agent-local                  deployed     ?          ?               ?                    -                ?' not in out
    assert 'grafana                            deployed     grafana    testuser        2021-11-23T160029Z   -                develop' not in out
    assert 'logging                            deployed     logging    otheruser       2021-11-24T013156Z   -                develop' in out


def test_list_helm_list_fail(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.run_helm_list',
        return_value=(1,
                      "",
                      "helm list failed")
    )

    with pytest.raises(SystemExit):
        gmskube.list_instances(
            username=None,
            instance_type=None,
            show_all=False
        )

    out, err = capsys.readouterr()
    assert 'Could not list instances: helm list failed' in out


def test_list_kubectl_get_configmap_all_namespaces_fail(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.run_helm_list',
        return_value=(
            0,
            get_test_file_contents('cmds/run_helm_list.json'),
            ""
        )
    )
    mocker.patch(
        'gmskube.gmskube.run_kubectl_get_configmap_all_namespaces',
        return_value=(1,
                      "",
                      "get configmap fail")
    )

    with pytest.raises(SystemExit):
        gmskube.list_instances(
            username=None,
            instance_type=None,
            show_all=False
        )

    out, err = capsys.readouterr()
    expected = (
        "Unable to get gms configmap from all namespaces: get configmap fail"
    )
    for word in expected.split():
        assert word in out


# ----- Ingress tests
def test_ingress_istio(mocker, capsys):
    mocker.patch('gmskube.gmskube.is_instance_istio', return_value=True)
    mocker.patch('gmskube.gmskube.get_ingress_port', return_value='8443')
    mocker.patch(
        'gmskube.gmskube.run_kubectl_get',
        return_value=(
            0,
            get_test_file_contents('cmds/run_kubectl_get_virtualservice.json'),
            ""
        )
    )
    gmskube.ingress(instance_name='test', ingress_port=None)

    out, err = capsys.readouterr()
    assert out.count('\n') == 18
    assert 'cache-service                                                  https://test.cluster.com:8443/cache-service' in out
    assert 'prometheus                                                     https://prometheus-test.cluster.com:8443/' in out
    assert 'reactive-interaction-gateway                                   https://test.cluster.com:8443/reactive-interaction-gateway/api/' in out


def test_ingress_non_istio(mocker, capsys):
    mocker.patch('gmskube.gmskube.is_instance_istio', return_value=False)
    mocker.patch('gmskube.gmskube.get_ingress_port', return_value='443')
    mocker.patch(
        'gmskube.gmskube.run_kubectl_get',
        return_value=(
            0,
            get_test_file_contents('cmds/run_kubectl_get_ingress.json'),
            ""
        )
    )
    gmskube.ingress(instance_name='test', ingress_port=None)

    out, err = capsys.readouterr()
    assert 'https://test-develop.cluster.gms.domain.com:443/acei-merge-processor' in out


def test_ingress_get_virtualservice_fail(mocker, capsys):
    mocker.patch('gmskube.gmskube.is_instance_istio', return_value=True)
    mocker.patch('gmskube.gmskube.get_ingress_port', return_value='8443')
    mocker.patch('gmskube.gmskube.run_kubectl_get', return_value=(1, "", ""))

    with pytest.raises(SystemExit):
        gmskube.ingress(instance_name='test', ingress_port=None)

    out, err = capsys.readouterr()
    assert 'Unable to get virtualservice details' in out


def test_ingress_get_ingress_fail(mocker, capsys):
    mocker.patch('gmskube.gmskube.is_instance_istio', return_value=False)
    mocker.patch('gmskube.gmskube.get_ingress_port', return_value='443')
    mocker.patch('gmskube.gmskube.run_kubectl_get', return_value=(1, "", ""))

    with pytest.raises(SystemExit):
        gmskube.ingress(instance_name='test', ingress_port=None)

    out, err = capsys.readouterr()
    assert 'Unable to get ingress details' in out


# ----- Augment Apply tests
def test_augment_apply(mocker, capsys):
    mocker.patch('gmskube.gmskube.augmentation_exists', return_value=True)
    mocker.patch('gmskube.gmskube.upgrade', return_value=None)

    gmskube.augment_apply(
        instance_name="test",
        augmentation_name='test',
        dry_run=False,
        sets=['key=value'],
        custom_chart_path=None,
        image_tag="test"
    )

    out, err = capsys.readouterr()
    assert "Augmentation 'test' successfully applied to test" in out


def test_augment_apply_not_exist(mocker, capsys):
    mocker.patch('gmskube.gmskube.augmentation_exists', return_value=False)

    with pytest.raises(SystemExit):
        gmskube.augment_apply(
            instance_name="test",
            augmentation_name='test',
            dry_run=False,
            sets=None,
            custom_chart_path=None,
            image_tag="test"
        )

    out, err = capsys.readouterr()
    expected = "Augmentation `test` is not a valid augmentation name."
    for word in expected.split():
        assert word in out


def test_augment_apply_exception(mocker, capsys):
    mocker.patch('gmskube.gmskube.augmentation_exists', return_value=True)
    mocker.patch(
        'gmskube.gmskube.upgrade',
        side_effect=Exception('test exception message')
    )

    with pytest.raises(SystemExit):
        gmskube.augment_apply(
            instance_name="test",
            augmentation_name='test',
            dry_run=False,
            sets=None,
            custom_chart_path=None,
            image_tag="test"
        )

    out, err = capsys.readouterr()
    expected = (
        "Failed to apply augmentation 'test' to test: test exception message"
    )
    for word in expected.split():
        assert word in out


# ----- Augment Delete tests
def test_augment_delete(mocker, capsys):
    mocker.patch('gmskube.gmskube.augmentation_exists', return_value=True)
    mocker.patch('gmskube.gmskube.upgrade', return_value=None)

    gmskube.augment_delete(
        instance_name="test",
        augmentation_name='test',
        dry_run=False,
        custom_chart_path=None,
        image_tag="test"
    )

    out, err = capsys.readouterr()
    assert "Augmentation 'test' successfully deleted from test" in out


def test_augment_delete_not_exist(mocker, capsys):
    mocker.patch('gmskube.gmskube.augmentation_exists', return_value=False)

    with pytest.raises(SystemExit):
        gmskube.augment_delete(
            instance_name="test",
            augmentation_name='test',
            dry_run=False,
            custom_chart_path=None,
            image_tag="test"
        )

    out, err = capsys.readouterr()
    expected = "Augmentation `test` is not a valid augmentation name."
    for word in expected.split():
        assert word in out


def test_augment_delete_exception(mocker, capsys):
    mocker.patch('gmskube.gmskube.augmentation_exists', return_value=True)
    mocker.patch(
        'gmskube.gmskube.upgrade',
        side_effect=Exception('test exception message')
    )

    with pytest.raises(SystemExit):
        gmskube.augment_delete(
            instance_name="test",
            augmentation_name='test',
            dry_run=False,
            custom_chart_path=None,
            image_tag="test"
        )

    out, err = capsys.readouterr()
    expected = (
        "Failed to delete augmentation 'test' from test: test exception "
        "message"
    )
    for word in expected.split():
        assert word in out


# ----- Augment Catalog tests
def test_augment_catalog(mocker, capsys):
    mocker.patch(
        'builtins.open',
        mocker.mock_open(
            read_data=get_test_file_contents("augmentation/test_values.yaml")
        )
    )
    gmskube.augment_catalog()

    out, err = capsys.readouterr()
    assert 'aug1                                               harness    ian,sb,database          my awesome augmentation' in out
    assert 'aug-missing-labels                                 harness                   ' in out
    assert 'aug-missing-type                                   none       ian            ' in out


# ----- Other function tests
# ----- augmentation_exists tests
def test_augmentation_exists(mocker):
    mocker.patch(
        'builtins.open',
        mocker.mock_open(
            read_data=get_test_file_contents("augmentation/test_values.yaml")
        )
    )
    result = gmskube.augmentation_exists('aug1')

    assert result


def test_augmentation_exists_fail(mocker):
    mocker.patch(
        'builtins.open',
        mocker.mock_open(
            read_data=get_test_file_contents("augmentation/test_values.yaml")
        )
    )
    result = gmskube.augmentation_exists('does-not-exist')

    assert not result


# ----- check_kubernetes_connection tests
def test_check_kubernetes_connection(mocker):
    mocker.patch('gmskube.gmskube.run_kubectl_auth', return_value=(0, "", ""))
    gmskube.check_kubernetes_connection()


def test_check_kubernetes_connection_fail(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.run_kubectl_auth',
        return_value=(1,
                      '',
                      'run kubectl auth failed')
    )

    with pytest.raises(SystemExit):
        gmskube.check_kubernetes_connection()

    out, err = capsys.readouterr()
    assert 'Unable to connect to the kubernetes cluster' in out
    assert 'run kubectl auth failed' in out


# ----- request_data_load tests
def test_request_dataload(mocker, capsys):
    mocker.patch('gmskube.gmskube.run_kubectl_get', return_value=(0, "", ""))
    mocker.patch(
        'requests.Session.get',
        return_value=get_request_response(200)
    )
    mocker.patch(
        'requests.Session.post',
        return_value=get_request_response(200)
    )
    mocker.patch(
        'json.loads',
        return_value={
            'status': 'FINISHED',
            'successful': True,
            'partial_result': 'partial dataload log',
            'result': 'dataload log'
        }
    )

    result = gmskube.request_dataload(
        base_domain="test.cluster.com",
        instance_name="test"
    )

    out, err = capsys.readouterr()
    assert 'Waiting for config loader to be alive' in out
    assert 'Requesting data load' in out
    assert 'partial dataload log' in out
    assert 'Data load successfully completed' in out
    assert result is True


def test_request_dataload_kubectl_get_fail(mocker, capsys):
    mocker.patch('gmskube.gmskube.run_kubectl_get', return_value=(1, "", ""))

    result = gmskube.request_dataload(
        base_domain="test.cluster.com",
        instance_name="test"
    )

    out, err = capsys.readouterr()
    expected = "config-loader service does not exist, skipping data load"
    for word in expected.split():
        assert word in out
    assert result is True


def test_request_dataload_config_overrides(mocker, capsys):
    mocker.patch('gmskube.gmskube.run_kubectl_get', return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.get_override_tar_file', return_value='test')
    mocker.patch(
        'requests.Session.get',
        return_value=get_request_response(200)
    )
    mocker.patch(
        'requests.Session.post',
        return_value=get_request_response(200)
    )
    mocker.patch(
        'json.loads',
        return_value={
            'status': 'FINISHED',
            'successful': True,
            'partial_result': 'partial dataload log',
            'result': 'dataload log'
        }
    )

    result = gmskube.request_dataload(
        base_domain="test.cluster.com",
        instance_name="test",
        config_overrides=get_config_overrides_path()
    )

    out, err = capsys.readouterr()
    assert 'Requesting data load' in out
    assert 'partial dataload log' in out
    assert 'Data load successfully completed' in out
    assert result is True


def test_request_dataload_config_overrides_get_override_tar_file_fail(
    mocker,
    capsys
):
    mocker.patch('gmskube.gmskube.run_kubectl_get', return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.get_override_tar_file', return_value=None)

    with pytest.raises(SystemExit):
        gmskube.request_dataload(
            base_domain="test.cluster.com",
            instance_name="test",
            config_overrides=get_config_overrides_path()
        )

    out, err = capsys.readouterr()
    expected = "Unable to create tar file from user supplied overrides"
    for word in expected.split():
        assert word in out


def test_request_dataload_alive_timeout(mocker, capsys):
    mocker.patch('gmskube.gmskube.run_kubectl_get', return_value=(0, "", ""))
    mocker.patch(
        'requests.Session.get',
        return_value=get_request_response(500)
    )
    mocker.patch(
        'requests.Session.post',
        return_value=get_request_response(200)
    )
    mocker.patch(
        'json.loads',
        return_value={
            'status': 'FINISHED',
            'successful': True,
            'partial_result': 'partial dataload log',
            'result': 'dataload log'
        }
    )

    with pytest.raises(SystemExit):
        gmskube.request_dataload(
            base_domain="test.cluster.com",
            instance_name="test",
            timeout=0.006
        )

    out, err = capsys.readouterr()
    expected = [
        "Waiting for config loader to be alive",
        "Timed out waiting for config loader to be alive, will attempt data "
        "load anyway",
        "Data load response status is unknown"
    ]
    for line in expected:
        for word in line.split():
            assert word in out


def test_request_dataload_post_load_fail(mocker, capsys):
    mocker.patch('gmskube.gmskube.run_kubectl_get', return_value=(0, "", ""))
    mocker.patch(
        'requests.Session.get',
        return_value=get_request_response(200)
    )
    mocker.patch(
        'requests.Session.post',
        return_value=get_request_response(500)
    )

    with pytest.raises(SystemExit):
        gmskube.request_dataload(
            base_domain="test.cluster.com",
            instance_name="test"
        )

    out, err = capsys.readouterr()
    assert 'Requesting data load' in out
    assert 'Failed to initiate a data load. 500: None' in out


def test_request_dataload_result_response_unsuccessful(mocker, capsys):
    mocker.patch('gmskube.gmskube.run_kubectl_get', return_value=(0, "", ""))
    mocker.patch(
        'requests.Session.get',
        return_value=get_request_response(200)
    )
    mocker.patch(
        'requests.Session.post',
        return_value=get_request_response(200)
    )
    mocker.patch(
        'json.loads',
        return_value={
            'status': 'FINISHED',
            'successful': False,
            'partial_result': 'partial dataload log',
            'result': 'dataload log'
        }
    )

    with pytest.raises(SystemExit):
        gmskube.request_dataload(
            base_domain="test.cluster.com",
            instance_name="test"
        )

    out, err = capsys.readouterr()
    expected = "Data load failed to execute successfully, Exiting"
    for word in expected.split():
        assert word in out


def test_request_dataload_status_not_finished(mocker, capsys):
    mocker.patch('gmskube.gmskube.run_kubectl_get', return_value=(0, "", ""))
    mocker.patch(
        'requests.Session.get',
        return_value=get_request_response(200)
    )
    mocker.patch(
        'requests.Session.post',
        return_value=get_request_response(200)
    )
    mocker.patch(
        'json.loads',
        return_value={
            'status': 'NOT DONE',
            'successful': True,
            'partial_result': 'partial dataload log',
            'result': 'dataload log'
        }
    )

    with pytest.raises(SystemExit):
        gmskube.request_dataload(
            base_domain="test.cluster.com",
            instance_name="test",
            timeout=0.006
        )

    out, err = capsys.readouterr()
    expected = [
        "partial dataload log",
        "Timed out waiting for data load after 0.006 minutes, Exiting"
    ]
    for line in expected:
        for word in line.split():
            assert word in out


def test_request_dataload_exception(mocker, capsys):
    mocker.patch('gmskube.gmskube.run_kubectl_get', return_value=(0, "", ""))
    mocker.patch(
        'gmskube.gmskube.get_override_tar_file',
        side_effect=Exception('test exception message')
    )

    with pytest.raises(SystemExit):
        gmskube.request_dataload(
            base_domain="test.cluster.com",
            instance_name="test",
            config_overrides=get_config_overrides_path()
        )

    out, err = capsys.readouterr()
    assert 'test exception message' in out


def test_request_dataload_json_decode_exception(mocker, capsys):
    mocker.patch('gmskube.gmskube.run_kubectl_get', return_value=(0, "", ""))
    mocker.patch(
        'requests.Session.get',
        return_value=get_request_response(200)
    )
    mocker.patch(
        'requests.Session.post',
        return_value=get_request_response(200)
    )
    mocker.patch(
        'json.loads',
        side_effect=json.decoder.JSONDecodeError(
            'error decoding',
            'dock error',
            0
        )
    )

    with pytest.raises(SystemExit):
        gmskube.request_dataload(
            base_domain="test.cluster.com",
            instance_name="test",
            config_overrides=get_config_overrides_path(),
            timeout=0.001
        )

    out, err = capsys.readouterr()
    assert 'Unable to convert response to json' in out
    assert 'Data load response status is unknown' in out


# ----- get_override_tar_file tests
def test_get_override_tar_file():
    result = gmskube.get_override_tar_file(
        config_dir=get_config_overrides_path()
    )

    assert result is not None
    assert 200 < len(result) < 350


def test_get_override_tar_file_path_not_exists():
    result = gmskube.get_override_tar_file(config_dir="/tmp")

    assert result is not None
    assert 20 < len(result) < 60


def test_get_override_tar_file_exception(capsys):
    result = gmskube.get_override_tar_file(config_dir="/doesnotexist")

    out, err = capsys.readouterr()
    expected = "No such file or directory: '/doesnotexist'"
    for word in expected.split():
        assert word in out
    assert result is None


# ----- get_ingress_port tests
def test_get_ingress_port(mocker):
    mocker.patch(
        'gmskube.gmskube.get_ingress_ports_config',
        return_value={
            'istio_port': '8443',
            'nginx_port': '443'
        }
    )

    result = gmskube.get_ingress_port(istio_enabled=False, override_port=None)

    assert result == "443"


def test_get_ingress_port_istio(mocker):
    mocker.patch(
        'gmskube.gmskube.get_ingress_ports_config',
        return_value={
            'istio_port': '8443',
            'nginx_port': '443'
        }
    )

    result = gmskube.get_ingress_port(istio_enabled=True, override_port=None)

    assert result == "8443"


def test_get_ingress_port_override(mocker):
    mocker.patch(
        'gmskube.gmskube.get_ingress_ports_config',
        return_value={
            'istio_port': '8443',
            'nginx_port': '443'
        }
    )

    result = gmskube.get_ingress_port(
        istio_enabled=False,
        override_port="9000"
    )

    assert result == "9000"


# ----- get_base_domain tests
def test_get_base_domain(mocker):
    mocker.patch(
        'gmskube.gmskube.get_ingress_ports_config',
        return_value={
            'base_domain': 'test.gms.domain.com',
            'istio_port': '8443',
            'nginx_port': '443'
        }
    )

    result = gmskube.get_base_domain()

    assert result == "test.gms.domain.com"


def test_get_base_domain_fail(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.get_ingress_ports_config',
        return_value={
            'istio_port': '8443',
            'nginx_port': '443'
        }
    )

    with pytest.raises(SystemExit):
        gmskube.get_base_domain()

    out, err = capsys.readouterr()
    expected = (
        "`base_domain` is not configured in the gms ingress-ports-config "
        "configmap"
    )
    for word in expected.split():
        assert word in out


# ----- get_ingress_ports_config tests
def test_get_ingress_ports_config(mocker):
    mocker.patch(
        'gmskube.gmskube.run_kubectl_get',
        return_value=(
            0,
            get_test_file_contents(
                'cmds/run_kubectl_get_configmap_ingress_ports_config.json'
            ),
            ""
        )
    )

    data = gmskube.get_ingress_ports_config()

    assert data['base_domain'] == "test.gms.domain.com"
    assert data['istio_port'] == "8443"
    assert data['nginx_port'] == "443"


def test_get_ingress_ports_config_kubectl_get_fail(mocker, capsys):
    mocker.patch('gmskube.gmskube.run_kubectl_get', return_value=(1, "", ""))

    with pytest.raises(SystemExit):
        gmskube.get_ingress_ports_config()

    out, err = capsys.readouterr()
    expected = "Unable to get gms configmap ingress-ports-config"
    for word in expected.split():
        assert word in out


# ----- is_instance_istio tests
def test_is_instance_istio_true(mocker):
    mocker.patch(
        'gmskube.gmskube.run_kubectl_get',
        return_value=(
            0,
            get_test_file_contents(
                'cmds/run_kubectl_get_namespace_istio.json'
            ),
            ""
        )
    )

    result = gmskube.is_instance_istio('test')

    assert result is True


def test_is_instance_istio_false(mocker):
    mocker.patch(
        'gmskube.gmskube.run_kubectl_get',
        return_value=(
            0,
            get_test_file_contents('cmds/run_kubectl_get_namespace.json'),
            ""
        )
    )

    result = gmskube.is_instance_istio('test')

    assert result is False


def test_is_instance_istio_kubectl_get_fail(mocker, capsys):
    mocker.patch('gmskube.gmskube.run_kubectl_get', return_value=(1, "", ""))

    with pytest.raises(SystemExit):
        gmskube.is_instance_istio('test')

    out, err = capsys.readouterr()
    assert 'Unable to get namespace details' in out


# ----- get_instance_labels tests
def test_get_instance_labels(mocker):
    mocker.patch(
        'gmskube.gmskube.run_kubectl_get',
        return_value=(
            0,
            get_test_file_contents('cmds/run_kubectl_get_configmap_gms.json'),
            ""
        )
    )

    result = gmskube.get_instance_labels(name="test")

    assert result['gms/cd11-connman-port'] == '8041'
    assert result['gms/cd11-dataman-port-end'] == '8449'
    assert result['gms/cd11-dataman-port-start'] == '8100'
    assert result['gms/cd11-live-data'] == 'false'
    assert result['gms/image-tag'] == 'test'
    assert result['gms/name'] == 'test'
    assert result['gms/namespace'] == 'test'
    assert result['gms/user'] == 'testuser'


def test_get_instance_labels_kubectl_get_fail(mocker, capsys):
    mocker.patch(
        'gmskube.gmskube.run_kubectl_get',
        return_value=(1,
                      "",
                      "test error")
    )

    result = gmskube.get_instance_labels(name="test")

    out, err = capsys.readouterr()
    assert len(result) == 0
    expected = "Unable to get gms configmap for test: test error"
    for word in expected.split():
        assert word in out


# ----- get_injector_set_args tests
def test_get_injector_set_args():
    result = gmskube.get_injector_set_args(
        injector=True,
        injector_dataset='test'
    )

    assert len(result) == 6
    assert result[1] == 'global.injector=True'
    assert result[3] == 'augmentation.cd11-injector.enabled=True'
    assert result[
        5] == 'augmentation.cd11-injector.env.CD11_INJECTOR_CONFIG_NAME=test'


def test_get_injector_set_args_false_none():
    result = gmskube.get_injector_set_args(
        injector=False,
        injector_dataset=None,
    )

    assert len(result) == 0


# ----- get_livedata_set_args tests
def test_get_livedata_set_args():
    result = gmskube.get_livedata_set_args(
        livedata=True,
        connman_port='8041',
        connman_data_manager_ip='192.168.1.1',
        connman_data_provider_ip='192.168.1.2',
        dataman_ports='9000-9099'
    )

    assert len(result) == 12
    assert result[1] == 'global.liveData=True'
    assert result[3] == 'da-connman.connPort=8041'
    assert result[
        5
    ] == 'da-connman.env.GMS_CONFIG_CONNMAN__DATA_MANAGER_IP_ADDRESS=192.168.1.1'
    assert result[
        7
    ] == 'da-connman.env.GMS_CONFIG_CONNMAN__DATA_PROVIDER_IP_ADDRESS=192.168.1.2'
    assert result[9] == 'da-dataman.dataPortStart=9000'
    assert result[11] == 'da-dataman.dataPortEnd=9099'


def test_get_livedata_set_args_false_none():
    result = gmskube.get_livedata_set_args(
        livedata=False,
        connman_port=None,
        connman_data_manager_ip=None,
        connman_data_provider_ip=None,
        dataman_ports=None
    )

    assert len(result) == 0


# ----- run_command tests
def test_run_command(mocker):
    mocker.patch('os.chdir', return_value=None)

    ret, out, err = gmskube.run_command('echo "test"', print_output=True)

    assert ret == 0
    assert out == 'test\n'
    assert err == ''


def test_run_command_no_print_output(mocker, capsys):
    mocker.patch('os.chdir', return_value=None)

    ret, out, err = gmskube.run_command('echo "test"', print_output=False)

    assert ret == 0
    assert out == 'test\n'
    assert err == ''

    stdout, stderr = capsys.readouterr()
    assert 'test\n' not in stdout
    assert 'test\n' not in stderr


def test_run_command_list(mocker, capsys):
    mocker.patch('os.chdir', return_value=None)

    ret, out, err = gmskube.run_command(shlex.split('echo "test"'), print_output=True)

    assert ret == 0
    assert out == 'test\n'
    assert err == ''

    stdout, stderr = capsys.readouterr()
    assert 'test' in stdout
    assert 'test' not in stderr


def test_run_command_print_output_error(mocker):
    mocker.patch('os.chdir', return_value=None)

    with pytest.raises(FileNotFoundError):
        gmskube.run_command(shlex.split('boguscmd'), print_output=True)


# ----- print_warning tests
def test_print_warning(capsys):
    gmskube.print_warning('test warning')

    out, err = capsys.readouterr()
    assert '[WARNING] test warning' in out


# ----- print_error tests
def test_print_error(capsys):
    gmskube.print_error('test error')

    out, err = capsys.readouterr()
    assert '[ERROR] test error' in out


# ----- is_valid_fqdn tests
def test_is_valid_fqdn():
    assert gmskube.is_valid_fqdn('test.cluster.local') is True
    assert gmskube.is_valid_fqdn('subdomain.gms.guru') is True
    assert gmskube.is_valid_fqdn(
        'a23456789-a23456789-a234567890.a23456789.a23456789.a23456789.a23456789.a23456789.a23456789.a23456789.a23456789.a23456789.a23456789.a23456789.a23456789.a23456789.a23456789.a23456789.a23456789.a23456789.a23456789.a23456789.a23456789.a23456789.a2345678.com'
    ) is True


def test_is_valid_fqdn_false():
    assert gmskube.is_valid_fqdn('test.192.168.1.1:6443') is False
    assert gmskube.is_valid_fqdn('xn--d1aacihrobi6i.xn--p1ai') is False
    assert gmskube.is_valid_fqdn('label.name.123') is False
    assert gmskube.is_valid_fqdn('so-me.na-me.567') is False
    assert gmskube.is_valid_fqdn('a..b') is False
    assert gmskube.is_valid_fqdn('a.b') is False


# ----- create_namespace tests
def test_create_namespace_no_rancher_project(mocker):
    mocker.patch('gmskube.gmskube.run_kubectl_create_namespace',
                 return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.run_kubectl_label_namespace',
                 return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.get_rancher_project_id',
                 return_value=(None))

    gmskube.create_namespace('test', 'config', False)


def test_create_namespace_rancher_project(mocker, capsys):
    mocker.patch('gmskube.gmskube.run_kubectl_create_namespace',
                 return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.run_kubectl_label_namespace',
                 return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.get_rancher_project_id',
                 return_value=('local:abc123'))
    mocker.patch('gmskube.gmskube.run_kubectl_annotate_namespace',
                 return_value=(0, "", ""))

    gmskube.create_namespace('test', 'config', False)

    out, err = capsys.readouterr()
    assert 'Adding rancher project annotation' in out


def test_create_namespace_istio(mocker, capsys):
    mocker.patch('gmskube.gmskube.run_kubectl_create_namespace',
                 return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.run_kubectl_label_namespace',
                 return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.get_rancher_project_id',
                 return_value=(None))

    gmskube.create_namespace('test', 'config', True)

    out, err = capsys.readouterr()
    assert 'Adding "istio-injection=enabled" label' in out


def test_create_namespace_kubectl_create_namespace_fail(mocker):
    mocker.patch('gmskube.gmskube.run_kubectl_create_namespace',
                 return_value=(1, "", ""))

    with pytest.raises(SystemExit):
        gmskube.create_namespace('test', 'config', False)


def test_create_namespace_kubectl_label_namespace_fail(mocker):
    mocker.patch('gmskube.gmskube.run_kubectl_create_namespace',
                 return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.run_kubectl_label_namespace',
                 return_value=(1, "", ""))

    with pytest.raises(SystemExit):
        gmskube.create_namespace('test', 'config', False)


def test_create_namespace_kubectl_annotate_namespace_fail(mocker):
    mocker.patch('gmskube.gmskube.run_kubectl_create_namespace',
                 return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.run_kubectl_label_namespace',
                 return_value=(0, "", ""))
    mocker.patch('gmskube.gmskube.get_rancher_project_id',
                 return_value=('local:abc123'))
    mocker.patch('gmskube.gmskube.run_kubectl_annotate_namespace',
                 return_value=(1, "", ""))

    with pytest.raises(SystemExit):
        gmskube.create_namespace('test', 'config', False)


# ----- get_rancher_project_id tests
def test_get_rancher_project_id(mocker):
    mocker.patch(
        'gmskube.gmskube.run_kubectl_get',
        return_value=(
            0,
            get_test_file_contents(
                'cmds/run_kubectl_get_configmap_rancher_project_config.json'
            ),
            ""
        )
    )

    rancher_project_id = gmskube.get_rancher_project_id()

    assert rancher_project_id == 'local:p-j5jfn'


def test_get_rancher_project_id_kubectl_get_fail(mocker, capsys):
    mocker.patch('gmskube.gmskube.run_kubectl_get', return_value=(1, "", ""))

    rancher_project_id = gmskube.get_rancher_project_id()

    assert rancher_project_id is None


# ----- delete_unmanaged_resources tests
def test_delete_unmanaged_resources(mocker):
    mocker.patch('gmskube.gmskube.run_kubectl_delete_unmanaged_resource',
                 return_value=(0, "", ""))

    gmskube.delete_unmanaged_resources('test')


def test_delete_unmanaged_resources_kubectl_delete_unmanaged_resource_fail(mocker):
    mocker.patch('gmskube.gmskube.run_kubectl_delete_unmanaged_resource',
                 return_value=(1, "test fail", ""))

    gmskube.delete_unmanaged_resources('test')
