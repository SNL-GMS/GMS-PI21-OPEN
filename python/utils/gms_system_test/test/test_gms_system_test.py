#!/usr/bin/env python3
"""
Unit tests for the ``gms_system_test.py`` script.
"""

import os
import re
import shlex
from argparse import ArgumentTypeError
from datetime import datetime, timedelta
from pathlib import Path
from time import time
from unittest.mock import MagicMock, patch

import pytest
from minio import Minio
from python.utils.gms_system_test.gms_system_test.gms_system_test import \
    GMSSystemTest
from python.kubectl.kubectl.kubectl import KubeCtl
from rich.console import Console
from tenacity import Future, RetryError


@pytest.fixture()
def gst() -> GMSSystemTest:
    gms_system_test = GMSSystemTest()
    gms_system_test.console = Console(log_time=False, log_path=False)
    gms_system_test.minio["access_key"] = "MINIO_ACCESS_KEY"
    gms_system_test.minio["secret_key"] = "MINIO_SECRET_KEY"
    return gms_system_test


def test_dry_run_message(
    gst: GMSSystemTest,
    capsys: pytest.CaptureFixture
) -> None:
    message = "dry run message"
    expected = f"DRY-RUN MODE:  {message}"
    gst.dry_run_message(message)
    captured = capsys.readouterr()
    assert expected in captured.out


def test_ensure_commands_are_available(gst: GMSSystemTest) -> None:
    path = [_ for _ in os.environ["PATH"].split(":") if "gms-common" not in _]
    os.environ["PATH"] = ":".join(path)
    with pytest.raises(SystemExit) as e:
        gst.ensure_commands_are_available()
    msg = e.value.args[0]
    assert "The `gmskube` command is not available" in msg


def test_check_kubeconfig(gst: GMSSystemTest) -> None:
    if "KUBECONFIG" in os.environ:
        del os.environ["KUBECONFIG"]
    with pytest.raises(RuntimeError) as e:
        gst.check_kubeconfig()
    msg = e.value.args[0]
    assert "Your `KUBECONFIG` environment variable must be set" in msg
    os.environ["KUBECONFIG"] = "foo:bar:baz"
    with pytest.raises(RuntimeError) as e:
        gst.check_kubeconfig()
    msg = e.value.args[0]
    assert "It looks like your `KUBECONFIG` is set to a list of files" in msg
    config_file = Path("temp_test_check_kubeconfig_file").resolve()
    os.environ["KUBECONFIG"] = str(config_file)
    if config_file.exists():
        config_file.unlink()
    with pytest.raises(RuntimeError) as e:
        gst.check_kubeconfig()
    msg = e.value.args[0]
    assert "that file doesn't exist" in msg
    config_file.touch()
    gst.check_kubeconfig()
    config_file.unlink()


def test_create_unique_reports_directory(gst: GMSSystemTest) -> None:
    reports_dirs = []
    for _ in range(5):
        gst.create_unique_reports_directory()
        reports_dirs.append(gst.reports_dir)
    for reports_dir in reports_dirs:
        assert reports_dir.name.startswith("system-test-reports-")
        assert reports_dir.exists()
        log_dir = reports_dir / "container-logs"
        for child in reports_dir.iterdir():
            assert child == log_dir
        assert not any(log_dir.iterdir())
        log_dir.rmdir()
        reports_dir.rmdir()
    assert len(reports_dirs) == len(set(reports_dirs))


def test_create_unique_instance_name(gst: GMSSystemTest) -> None:
    names = [gst.create_unique_instance_name() for _ in range(5)]
    for name in names:
        assert name.startswith("gms-system-test-")
    assert len(names) == len(set(names))


def test_begin_stage(
    gst: GMSSystemTest,
    capsys: pytest.CaptureFixture
) -> None:
    stage_name = "test"
    message = "begin stage"
    gst.stage_start_time = None
    gst.begin_stage(stage_name, message)
    captured = capsys.readouterr()
    assert message in captured.out
    assert gst.current_stage == stage_name
    assert gst.stage_start_time is not None


def test_end_stage(gst: GMSSystemTest, capsys: pytest.CaptureFixture) -> None:
    stage_name = "test"
    gst.current_stage = stage_name
    gst.stage_start_time = datetime.now()
    gst.end_stage()
    captured = capsys.readouterr()
    assert stage_name in gst.durations
    assert "duration:" in captured.out
    assert str(gst.durations[stage_name]) in captured.out


def test_skip_stage(gst: GMSSystemTest, capsys: pytest.CaptureFixture) -> None:
    gst.skip_stage()
    captured = capsys.readouterr()
    assert "Skipping this stage." in captured.out


def test_install_instance_skip_stage(
    gst: GMSSystemTest,
    capsys: pytest.CaptureFixture
) -> None:
    gst.parse_args(shlex.split("--stage wait"))
    gst.install_instance()
    captured = capsys.readouterr()
    stage_name = "install"
    assert stage_name in gst.durations
    assert "Skipping this stage." in captured.out
    assert gst.durations[stage_name] < timedelta(seconds=1)


expected_commands = [
    (
        "ian",
        [
            "ian-sim-deploy --tag-auto sha1 instance development",
            "gmskube augment apply --tag sha1 --name minio-test-reports "
            "--set minioReportBucket=reports --set "
            "minioAccessKey=MINIO_ACCESS_KEY --set "
            "minioSecretKey=MINIO_SECRET_KEY instance"
        ]
    ),
    (
        "sb",
        [
            "gmskube install --tag sha1 --type sb instance",
            "gmskube augment apply --tag sha1 --name minio-test-reports "
            "--set minioReportBucket=reports --set "
            "minioAccessKey=MINIO_ACCESS_KEY --set "
            "minioSecretKey=MINIO_SECRET_KEY instance"
        ]
    ),
    (
        "soh",
        [
            "gmskube install --tag sha1 --type soh "
            "--set interactive-analysis-ui.env.NODE_ENV=development instance",
            "gmskube augment apply --tag sha1 --name minio-test-reports "
            "--set minioReportBucket=reports --set "
            "minioAccessKey=MINIO_ACCESS_KEY --set "
            "minioSecretKey=MINIO_SECRET_KEY instance"
        ]
    )
]


@pytest.mark.parametrize(
    "instance_type, expected",
    expected_commands,
    ids=[_[0] for _ in expected_commands]
)
def test_create_install_commands(
    instance_type: str,
    expected: str,
    gst: GMSSystemTest
) -> None:
    gst.parse_args(
        shlex.split(
            f"--stage install --instance instance --tag sha1 --type "
            f"{instance_type}"
        )
    )
    result = gst.create_install_commands()
    assert result == expected


@pytest.mark.parametrize(
    "command, expected",
    [("command --foo",
      "command \\\n    --foo"),
     ("command --foo bar baz",
      "command \\\n    --foo bar \\\n    baz"),
     ("command foo bar baz",
      "command \\\n    foo \\\n    bar \\\n    baz"),
     ("command --foo --bar baz",
      "command \\\n    --foo \\\n    --bar baz"),
     ("command foo bar baz",
      "command \\\n    foo \\\n    bar \\\n    baz"),
     ("command --foo 'bar baz'",
      "command \\\n    --foo 'bar baz'")]
)
def test_pretty_print_command(
    command: str,
    expected: str,
    gst: GMSSystemTest
) -> None:
    assert gst.pretty_print_command(command) == expected


@pytest.mark.parametrize(
    "instance_type, expected",
    expected_commands,
    ids=[_[0] for _ in expected_commands]
)
@patch("uuid.uuid4")
def test_install_instance(
    mock_uuid4: MagicMock,
    instance_type: str,
    expected: str,
    gst: GMSSystemTest,
    capsys: pytest.CaptureFixture
) -> None:
    mock_uuid4.return_value = "FOO"
    gst.parse_args(
        shlex.split(
            "--dry-run --instance instance --stage install --tag sha1 --type "
            f"{instance_type}"
        )
    )
    gst.install_instance()
    captured = capsys.readouterr()
    for command in expected:
        for word in re.split(" |.|=", command):
            assert word in captured.out


@patch(
    "python.kubectl.kubectl.kubectl.KubeCtl."
    "wait_for_all_pods_running_or_succeeded"
)
def test_check_all_pods_running_or_succeeded_success(
    mock_wait_for_all_pods_running_or_succeeded: MagicMock,
    gst: GMSSystemTest,
    capsys: pytest.CaptureFixture
) -> None:
    mock_wait_for_all_pods_running_or_succeeded.return_value = None
    gst.parse_args(shlex.split("--stage wait"))
    gst.kubectl = KubeCtl("namespace")
    gst.check_all_pods_running_or_succeeded()
    captured = capsys.readouterr()
    assert "ALL PODS RUNNING" in captured.out


@patch(
    "python.kubectl.kubectl.kubectl.KubeCtl."
    "wait_for_all_pods_running_or_succeeded"
)
def test_check_all_pods_running_or_succeeded_failed(
    mock_wait_for_all_pods_running_or_succeeded: MagicMock,
    gst: GMSSystemTest,
    capsys: pytest.CaptureFixture
) -> None:
    mock_wait_for_all_pods_running_or_succeeded.side_effect = (
        RetryError(Future(1))
    )
    gst.parse_args(shlex.split("--stage wait"))
    gst.kubectl = KubeCtl("namespace")
    gst.check_all_pods_running_or_succeeded()
    captured = capsys.readouterr()
    assert "NOT ALL PODS RUNNING" in captured.out


def test_sleep_after_pods_running(gst: GMSSystemTest) -> None:
    sleep_time = 1
    start_time = time()
    gst.parse_args(shlex.split(f"--stage sleep --sleep {sleep_time}"))
    gst.sleep_after_pods_running()
    assert time() - start_time > sleep_time


@patch("python.kubectl.kubectl.kubectl.KubeCtl.get_configmap_labels")
def test_ensure_instance_tag_set(
    mock_get_configmap_labels: MagicMock,
    gst: GMSSystemTest
) -> None:
    gst.kubectl = KubeCtl()
    instance_tag = "foo"
    mock_get_configmap_labels.return_value = {"gms/image-tag": instance_tag}
    gst.ensure_instance_tag_set()
    assert gst.instance_tag == instance_tag
    gst.instance_tag = None
    mock_get_configmap_labels.return_value = None
    with pytest.raises(RuntimeError) as e:
        gst.ensure_instance_tag_set()
    msg = e.value.args[0]
    assert "Unable to determine the instance tag" in msg


def test_apply_test_augmentation(
    gst: GMSSystemTest,
    capsys: pytest.CaptureFixture
) -> None:
    """
    TODO:  UPDATE AFTER AUTO-TAG DETECTION.
    """
    gst.parse_args(
        shlex.split(
            "--stage test --instance instance --tag sha1 --test test-name "
            "--env 'foo=echo hello world' --parallel 3 --dry-run"
        )
    )
    gst.apply_test_augmentation()
    captured = capsys.readouterr()
    expected = (
        "gmskube augment apply --tag sha1 --name test-name --set "
        "env.foo=\"echo hello world\" --set numIdenticalPods=3 instance"
    )
    for word in expected.split():
        assert word in captured.out


@pytest.mark.parametrize("istio_enabled", [True, False])
@patch("python.kubectl.kubectl.kubectl.KubeCtl.get_endpoints")
@patch("python.kubectl.kubectl.kubectl.KubeCtl.get_resource")
@patch("python.kubectl.kubectl.kubectl.KubeCtl.istio_enabled")
def test_get_minio_endpoint(
    mock_istio_enabled: MagicMock,
    mock_get_resource: MagicMock,
    mock_get_endpoints: MagicMock,
    istio_enabled: bool,
    gst: GMSSystemTest
) -> None:
    gst.kubectl = KubeCtl("namespace")
    gst.kubectl_gms = KubeCtl("other-namespace")
    mock_get_endpoints.return_value = "host.name", ["/"]
    mock_get_resource.return_value = [{
        "data": {
            "istio_port": "12345",
            "nginx_port": "54321"
        }
    }]
    mock_istio_enabled.return_value = istio_enabled
    endpoint = gst.get_minio_endpoint()
    if istio_enabled:
        assert endpoint == "host.name:12345/"
    else:
        assert endpoint == "host.name:54321/"


@patch("python.kubectl.kubectl.kubectl.KubeCtl.get_endpoints")
@patch("python.kubectl.kubectl.kubectl.KubeCtl.get_resource")
def test_get_minio_endpoint_raises(
    mock_get_resource: MagicMock,
    mock_get_endpoints: MagicMock,
    gst: GMSSystemTest
) -> None:
    gst.kubectl = KubeCtl("namespace")
    gst.kubectl_gms = KubeCtl("other-namespace")
    mock_get_endpoints.return_value = None, None
    with pytest.raises(RuntimeError) as e:
        gst.get_minio_endpoint()
    msg = e.value.args[0]
    assert "Failed to locate" in msg
    mock_get_endpoints.return_value = "host.name", ["/"]
    mock_get_resource.return_value = []
    with pytest.raises(RuntimeError) as e:
        gst.get_minio_endpoint()
    msg = e.value.args[0]
    assert "Failed to get the port" in msg


@patch(
    "python.utils.gms_system_test.gms_system_test.gms_system_test."
    "GMSSystemTest.get_minio_endpoint"
)
@patch.object(Minio, "__init__", return_value=None)
def test_get_minio_client(
    mock_Minio: MagicMock,
    mock_get_minio_endpoint: MagicMock
) -> None:
    """
    TODO:  FIGURE OUT HOW TO MOCK ``Minio()``.
    """
    # gst = GMSSystemTest()
    # mock_get_minio_endpoint.return_value = "host.name:12345/"
    # with pytest.raises(RuntimeError) as e:
    #     gst.get_minio_client()
    # msg = e.value.args[0]
    # assert "Failed to connect to MinIO endpoint" in msg
    assert True


def test_extract_minio_object() -> None:
    """
    TODO:  FIGURE OUT HOW TO UNIT TEST THIS.
    """
    assert True


def test_pod_succeeded() -> None:
    """
    TODO:  FIGURE OUT HOW TO UNIT TEST THIS.
    """
    assert True


def test_retrieve_test_results() -> None:
    """
    TODO:  FIGURE OUT HOW TO UNIT TEST THIS.
    """
    assert True


# @pytest.mark.parametrize("tests", ["", "test-sb-jest"])
# @pytest.mark.parametrize("env", [[""], ["foo=bar"]])
# def test_run_tests(tests, env, capsys):
def test_run_tests() -> None:
    """
    Todo:
        * Update this once :func:`run_test` has been updated for
          dry-run mode.
    """
    assert True
    # gst = GMSSystemTest(
    #     Namespace(
    #         instance="test-instance",
    #         dry_run=True,
    #         tests=tests,
    #         env=env
    #     )
    # )
    # gst.run_test()
    # captured = capsys.readouterr()
    # assert "gms_test_runner.py" in captured.out
    # assert "--force" in captured.out
    # assert "--reports" in captured.out
    # assert gst.instance_name in captured.out
    # if tests:
    #     assert tests in captured.out
    # if env:
    #     for value in env:
    #         assert value in captured.out


def test_uninstall_instance(
    gst: GMSSystemTest,
    capsys: pytest.CaptureFixture
) -> None:
    instance_name = "instance"
    command = f"gmskube uninstall {instance_name}"
    gst.parse_args(
        shlex.split(f"--stage uninstall --instance {instance_name} --dry-run")
    )
    gst.uninstall_instance()
    captured = capsys.readouterr()
    for word in command.split():
        assert word in captured.out


def test_get_timing_report(gst: GMSSystemTest) -> None:
    gst.durations = {"first": "0:12:34.56789", "second": "9:87:65.43210"}
    report = gst.get_timing_report()
    for stage in gst.durations:
        assert stage in report
    for duration in gst.durations.values():
        assert duration in report
    assert "Total" in report


@patch(
    "reverse_argparse.ReverseArgumentParser."
    "get_pretty_command_line_invocation"
)
def test_print_script_execution_summary(
    mock_get_pretty_command_line_invocation: MagicMock,
    gst: GMSSystemTest,
    capsys: pytest.CaptureFixture
) -> None:
    mock_get_pretty_command_line_invocation.return_value = (
        "command line invocation"
    )
    gst.parse_args(shlex.split("--stage wait sleep"))
    gst.durations = {"wait": "0:12:34.56789", "sleep": "9:87:65.43210"}
    gst.commands_executed = ["foo", "bar", "baz"]
    gst.reports_dir = Path("/path/to/test/reports")
    gst.print_script_execution_summary()
    captured = capsys.readouterr()
    headings = [
        "Ran the following",
        "Commands executed",
        "Timing results",
        "Test reports"
    ]
    details = ([mock_get_pretty_command_line_invocation.return_value]
               + gst.commands_executed + list(gst.durations.keys())
               + list(gst.durations.values()))
    for item in headings + details:
        assert item in captured.out


@patch("python.kubectl.kubectl.kubectl.KubeCtl.save_logs")
def test_keyboard_interrupt_handler(
    mock_save_logs: MagicMock,
    gst: GMSSystemTest,
    capsys: pytest.CaptureFixture
) -> None:
    instance_name = "instance"
    command = f"gmskube uninstall {instance_name}"
    gst.kubectl = KubeCtl(instance_name)
    gst.parse_args(
        shlex.split(
            f"--tag sha1 --type sb --instance {instance_name} --stage install "
            "wait sleep test uninstall --test TEST-NAME --dry-run"
        )
    )
    with pytest.raises(SystemExit) as exc:
        gst.keyboard_interrupt_handler(signal_number=1, stack_frame=None)
    assert exc.value.code == 1
    captured = capsys.readouterr()
    expected = ["Caught a keyboard interrupt signal", command]
    for line in expected:
        for word in line.split():
            assert word in captured.out


@pytest.mark.parametrize(
    "tag, expected", [
        ("no-changes", "no-changes"),
        ("TO-LOWER-CASE", "to-lower-case"),
        ("rep!ace-$pecial", "rep-ace--pecial"),
        ("--remove-leading-trailing-----", "remove-leading-trailing"),
        ("this-is-the-tag-that-never-ends-yes-it-goes-on-and-on-my-friend-"
         "some-people-started-typing-it-not-knowing-what-it-was-and-theyll-"
         "continue-typing-it-forever-just-because---",
         "this-is-the-tag-that-never-ends-yes-it-goes-on-and-on-my-friend")
    ]
)  # yapf: disable
def test_argparse_tag_name_type(
    tag: str,
    expected: str,
    gst: GMSSystemTest
) -> None:
    assert gst.argparse_tag_name_type(tag) == expected


@pytest.mark.parametrize("arg", ["foo=bar", "'var=quotes and spaces'"])
def test_argparse_set_env(arg: str, gst: GMSSystemTest) -> None:
    assert gst.argparse_set_env(arg) == arg


def test_argparse_set_env_raises(gst: GMSSystemTest) -> None:
    with pytest.raises(ArgumentTypeError):
        gst.argparse_set_env("no-equals-sign")


def test_raise_parser_error(
    gst: GMSSystemTest,
    capsys: pytest.CaptureFixture
) -> None:
    parser = gst.parser()
    error_message = (
        "This is a lengthy error message explaining what exactly went wrong, "
        "where, and why.  It's so long it should get wrapped over multiple "
        "lines."
    )
    with pytest.raises(SystemExit):
        gst.raise_parser_error(parser, error_message)
    captured = capsys.readouterr()
    expected = (
        error_message.split() + [
            "usage:",
            "Description",
            "Results",
            "Environment Variables",
            "Examples",
            "options:",
            "Retry options"
        ]
    )
    for term in expected:
        assert term in captured.out


@pytest.mark.parametrize(
    "env_args, parallel, expected",
    [(
        ["FOO=BAR"],
        1,
        ['--set env.FOO="BAR"']
    ), (
        ["spaces=echo hello world"],
        1,
        ['--set env.spaces="echo hello world"']
    ), (
        ["check=multiple", "args=okay"],
        1,
        ['--set env.check="multiple"', '--set env.args="okay"']
    ), (
        ["global=here", "test-name.foo=also here", "other-test.bar=missing"],
        1,
        ['--set env.global="here"', '--set env.foo="also here"']
    ), (
        ["FOO=BAR"],
        3,
        ['--set env.FOO="BAR"', '--set numIdenticalPods=3']
    ), (
        [],
        1,
        []
    )]
)  # yapf: disable
def test_create_set_args(
    env_args: str,
    parallel: int,
    expected: str,
    gst: GMSSystemTest
) -> None:
    gst.test_name = "test-name"
    gst.parallel = parallel
    assert gst.create_set_args(env_args) == expected


def test_parse_args(gst: GMSSystemTest) -> None:
    gst.parse_args(
        shlex.split(
            "--dry-run --env foo=bar --env baz=bif --instance instance --tag "
            "my-tag --type sb --parallel 5 --retry-attempts 42 --retry-delay "
            "600 --retry-timeout 1200 --sleep 123 --stage install wait sleep "
            "test --test MY-TEST --timeout 54321"
        )
    )
    assert gst.dry_run is True
    assert gst.instance_name == "instance"
    assert gst.instance_tag == "my-tag"
    assert gst.instance_type == "sb"
    assert gst.parallel == 5
    assert gst.retry_attempts == 42
    assert gst.retry_delay == 600
    assert gst.retry_timeout == 1200
    assert gst.set_args == [
        '--set env.foo="bar"',
        '--set env.baz="bif"',
        '--set numIdenticalPods=5'
    ]
    assert gst.sleep == 123
    assert gst.stages_to_run == ["install", "wait", "sleep", "test"]
    assert gst.test_name == "MY-TEST"
    assert gst.timeout == 54321


@pytest.mark.parametrize(
    "args, message", [(
        "--stage test",
        "`--test` flag augment catalog"
    ), (
        "--stage install",
        "specify (1) both `--tag` `--type` (2) `--instance <name>` omit"
    )]
)  # yapf: disable
def test_parse_args_raises(
    args: str,
    message: str,
    gst: GMSSystemTest,
    capsys: pytest.CaptureFixture
) -> None:
    with pytest.raises(SystemExit):
        gst.parse_args(shlex.split(args))
    captured = capsys.readouterr()
    for word in message:
        assert word in captured.out
