import argparse

import pytest

from gmskube import gmskube_cli
from test.helpers import get_test_file_contents


def test_main(mocker, monkeypatch):
    mocker.patch('gmskube.gmskube_cli.get_args', return_value=argparse.Namespace(verbose='DEBUG', command=(lambda *args: None)))
    mocker.patch('logging.basicConfig', return_value=None)
    mocker.patch('builtins.open', new_callable=mocker.mock_open)
    mocker.patch('os.chmod', return_value=None)
    mocker.patch('gmskube.gmskube.check_kubernetes_connection', return_value=None)
    monkeypatch.setenv('KUBECTL_CONTEXT', 'test')
    monkeypatch.delenv('REQUEST_CA_BUNDLE', raising=False)
    gmskube_cli.main()


def test_main_no_command(mocker, monkeypatch):
    mocker.patch('gmskube.gmskube_cli.get_args', return_value=argparse.Namespace(verbose='DEBUG'))
    mocker.patch('logging.basicConfig', return_value=None)
    mocker.patch('builtins.open', new_callable=mocker.mock_open)
    mocker.patch('os.chmod', return_value=None)
    mocker.patch('gmskube.gmskube.check_kubernetes_connection', return_value=None)
    monkeypatch.setenv('KUBECTL_CONTEXT', 'test')
    monkeypatch.delenv('REQUEST_CA_BUNDLE', raising=False)
    gmskube_cli.main()


def test_get_args_name_pass(mocker):
    mocker.patch('argparse.ArgumentParser.parse_args', return_value=argparse.Namespace(name="test"))
    parser = gmskube_cli.get_parser()
    gmskube_cli.get_args(parser)


def test_get_args_name_fail(mocker):
    mocker.patch('argparse.ArgumentParser.parse_args', return_value=argparse.Namespace(name="Te$t"))
    parser = gmskube_cli.get_parser()

    with pytest.raises(argparse.ArgumentTypeError):
        gmskube_cli.get_args(parser)


def test_get_args_name_none(mocker):
    mocker.patch('argparse.ArgumentParser.parse_args', return_value=argparse.Namespace())
    parser = gmskube_cli.get_parser()
    gmskube_cli.get_args(parser)


def test_get_args_livedata_pass(mocker):
    mocker.patch('argparse.ArgumentParser.parse_args',
                 return_value=argparse.Namespace(livedata=True,
                                                 connman_port="123", connman_data_manager_ip="127.0.0.1",
                                                 connman_data_provider_ip="127.0.0.1", dataman_ports="123-456")
                 )
    parser = gmskube_cli.get_parser()
    gmskube_cli.get_args(parser)


def test_get_args_livedata_fail(mocker, capsys):
    mocker.patch('argparse.ArgumentParser.parse_args',
                 return_value=argparse.Namespace(dataman_ports="123-456")
                 )
    parser = gmskube_cli.get_parser()
    with pytest.raises(SystemExit):
        gmskube_cli.get_args(parser)

    out, err = capsys.readouterr()
    assert '--livedata must be specified' in err


def test_validate_instance_name_pass():
    gmskube_cli.validate_instance_name("test-1234")


def test_validate_instance_name_fail():
    with pytest.raises(argparse.ArgumentTypeError):
        gmskube_cli.validate_instance_name("awesome@_test")


def test_argparse_tag_name_type():
    ret = gmskube_cli.argparse_tag_name_type('-MyBranchName_1234567890123456789012345678901234567890')
    assert ret == 'mybranchname-1234567890123456789012345678901234567890'


def test_argparse_set_type():
    gmskube_cli.argparse_set_type('label=value')


def test_argparse_set_type_empty_value():
    gmskube_cli.argparse_set_type('label=')


def test_argparse_set_type_fail():
    with pytest.raises(argparse.ArgumentTypeError) as ex:
        gmskube_cli.argparse_set_type('=value')
    assert 'When specifying `--set`, you must supply helm chart name/value pair as: `Name=Value`' in ex.value.args[0]


def test_argparse_set_type_global_env():
    gmskube_cli.argparse_set_type('global.env.test=value')


def test_argparse_set_type_env_fail():
    with pytest.raises(argparse.ArgumentTypeError) as ex:
        gmskube_cli.argparse_set_type('env.test=value')
    assert 'The old top-level `env` has been replaced with `global.env`' in ex.value.args[0]


def test_argparse_augment_set_type():
    gmskube_cli.argparse_augment_set_type('env=value')


def test_argparse_ip_address_type_pass():
    gmskube_cli.argparse_ip_address_type('192.168.1.1')  # NOSONAR: ip address for testing


def test_argparse_ip_address_type_fail():
    with pytest.raises(argparse.ArgumentTypeError):
        gmskube_cli.argparse_ip_address_type('192.168.not.valid')


def test_argparse_dataman_ports_type_pass():
    gmskube_cli.argparse_dataman_ports_type('8080-8081')


def test_argparse_dataman_ports_type_fail():
    with pytest.raises(argparse.ArgumentTypeError):
        gmskube_cli.argparse_dataman_ports_type('9090')


def test_argparse_augmentation_type(mocker):
    mocker.patch('builtins.open', mocker.mock_open(read_data=get_test_file_contents("augmentation/test_values.yaml")))

    gmskube_cli.argparse_augmentation_type('aug1')


def test_argparse_augmentation_type_fail(mocker):
    mocker.patch('builtins.open', mocker.mock_open(read_data=get_test_file_contents("augmentation/test_values.yaml")))

    with pytest.raises(argparse.ArgumentTypeError):
        gmskube_cli.argparse_augmentation_type('does-not-exist')
