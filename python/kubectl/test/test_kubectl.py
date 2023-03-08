#!/usr/bin/env python3
"""
Unit tests for the ``kubectl.py`` script.
"""

from pathlib import Path
from python.kubectl.kubectl.kubectl import KubeCtl
import pytest
from subprocess import CompletedProcess, TimeoutExpired
from tenacity import RetryError, TryAgain
from time import sleep, time
from unittest.mock import patch

error_params = [("Failed",
                 "failed to start"),
                ("Unknown",
                 "in an unknown phase")]


@pytest.mark.parametrize(
    "invalid",
    ["CAPS",
     "-start-hyphen",
     "end-hyphen-",
     "$p3c!al chars",
     "too-long-" * 8]
)
def test_validate_namespace_name_invalid(invalid):
    with pytest.raises(ValueError):
        KubeCtl(invalid).validate_namespace()


def test_validate_namespace_name_valid():
    KubeCtl("valid-name").validate_namespace()


@pytest.mark.parametrize(
    "phase, expected",
    error_params,
    ids=[p[0] for p in error_params]
)
@patch("subprocess.run")
def test_all_pods_running_or_succeeded_error(mock_run, phase, expected):
    mock_run.return_value = CompletedProcess(
        args="testing",
        returncode=0,
        stdout='{"items": [{"metadata": {"name": "pod_name"}, "status": '
        '{"phase": "' + phase + '"}}]}'
    )
    with pytest.raises(RuntimeError) as e:
        KubeCtl().all_pods_running_or_succeeded(60)
    msg = e.value.args[0]
    assert expected in msg


@pytest.mark.parametrize(
    "items",
    [
        '[]',
        '''[{"status": {"phase": "Pending"},
             "metadata": {"name": "pod-name"}}]'''
    ],
    ids=["no_pods",
         "Pending"]
)
@patch("subprocess.run")
def test_all_pods_running_or_succeeded_retry(mock_run, items):
    mock_run.return_value = CompletedProcess(
        args="testing",
        returncode=0,
        stdout='{"items": ' + items + '}'
    )
    assert not KubeCtl().all_pods_running_or_succeeded(60)


@patch("subprocess.run")
def test_all_pods_running_or_succeeded_success(mock_run):
    mock_run.return_value = CompletedProcess(
        args="testing",
        returncode=0,
        stdout="""{"items": [{"status": {"phase": "Running"}},
                             {"status": {"phase": "Succeeded"}}]}"""
    )
    assert KubeCtl().all_pods_running_or_succeeded(60)


@patch("subprocess.run")
def test_get_pods(mock_run):
    expected = "some output"
    mock_run.return_value = CompletedProcess(
        args="testing",
        returncode=0,
        stdout=expected
    )
    results = KubeCtl().get_pods()
    assert expected in results


@patch("subprocess.run")
def test_get_logs_from_container(mock_run):
    expected = "sample\nlog\noutput"
    mock_run.return_value = CompletedProcess(
        args="testing",
        returncode=0,
        stdout=expected
    )
    result = KubeCtl().get_logs_from_container("pod", "container")
    assert result == expected


@patch("subprocess.run")
def test_get_containers_in_pod(mock_run):
    expected = ["foo", "bar", "baz"]
    mock_run.return_value = CompletedProcess(
        args="testing",
        returncode=0,
        stdout=" ".join(expected)
    )
    result = KubeCtl().get_containers_in_pod("pod")
    assert result == expected


@patch("subprocess.run")
def test_get_pods_for_resource(mock_run):
    expected = ["foo", "bar", "baz"]
    mock_run.return_value = CompletedProcess(
        args="testing",
        returncode=0,
        stdout="\n".join(expected)
    )
    result = KubeCtl().get_pods_for_resource("resource", resource_type="job")
    assert result == expected


@patch("subprocess.run")
def test_get_resources_for_namespace(mock_run):
    expected = ["foo", "bar", "baz"]
    mock_run.return_value = CompletedProcess(
        args="testing",
        returncode=0,
        stdout="\n".join(expected)
    )
    result = KubeCtl().get_resources_for_namespace("resource-type")
    assert result == expected


@pytest.mark.parametrize(
    "items",
    [
        '[]',
        '''[{"status": {"phase": "Pending"},
             "metadata": {"name": "pod-name"}}]'''
    ],
    ids=["no_pods",
         "Pending"]
)
@patch("subprocess.run")
def test_all_pods_running_or_succeeded_retry_wrapper_retry(mock_run, items):
    mock_run.return_value = CompletedProcess(
        args="testing",
        returncode=0,
        stdout='{"items": ' + items + '}'
    )
    with pytest.raises(TryAgain):
        KubeCtl().all_pods_running_or_succeeded_retry_wrapper(60)


@patch("subprocess.run")
def test_all_pods_running_or_succeeded_retry_wrapper_success(mock_run):
    mock_run.return_value = CompletedProcess(
        args="testing",
        returncode=0,
        stdout="""{"items": [{"status": {"phase": "Running"}},
                             {"status": {"phase": "Succeeded"}}]}"""
    )
    assert KubeCtl().all_pods_running_or_succeeded_retry_wrapper(60) is None


@pytest.mark.parametrize(
    "phase, expected",
    error_params,
    ids=[p[0] for p in error_params]
)
@patch("subprocess.run")
def test_all_pods_running_or_succeeded_retry_wrapper_error(
    mock_run,
    phase,
    expected
):
    mock_run.return_value = CompletedProcess(
        args="testing",
        returncode=0,
        stdout='{"items": [{"metadata": {"name": "pod_name"}, "status": '
        '{"phase": "' + phase + '"}}]}'
    )
    with pytest.raises(RuntimeError) as e:
        KubeCtl().all_pods_running_or_succeeded_retry_wrapper(60)
    msg = e.value.args[0]
    assert expected in msg


@patch(
    "python.kubectl.kubectl.kubectl.KubeCtl."
    "all_pods_running_or_succeeded_retry_wrapper"
)
def test_wait_for_all_pods_running_or_succeeded_timeout(
    mock_all_pods_running_or_succeeded_retry_wrapper
):
    mock_all_pods_running_or_succeeded_retry_wrapper.side_effect = TryAgain
    timeout = 2
    start_time = time()
    with pytest.raises(RetryError):
        KubeCtl().wait_for_all_pods_running_or_succeeded(timeout=timeout)
    assert time() - start_time > timeout


@patch(
    "python.kubectl.kubectl.kubectl.KubeCtl."
    "all_pods_running_or_succeeded_retry_wrapper"
)
def test_wait_for_all_pods_running_or_succeeded_retry_delay_max_attempts(
    mock_all_pods_running_or_succeeded_wrapper
):
    mock_all_pods_running_or_succeeded_wrapper.side_effect = TryAgain
    retry_delay = 2
    max_attempts = 2
    start_time = time()
    with pytest.raises(RetryError):
        KubeCtl().wait_for_all_pods_running_or_succeeded(
            retry_delay=retry_delay,
            max_attempts=max_attempts
        )
    assert time() - start_time > retry_delay * (max_attempts-1)


@patch(
    "python.kubectl.kubectl.kubectl.KubeCtl."
    "all_pods_running_or_succeeded_retry_wrapper"
)
def test_wait_for_all_pods_running_or_succeeded_no_retry(
    mock_all_pods_running_or_succeeded_wrapper
):
    mock_all_pods_running_or_succeeded_wrapper.return_value = None
    start_time = time()
    KubeCtl().wait_for_all_pods_running_or_succeeded()
    assert time() - start_time < 1


@patch("subprocess.run")
def test_describe_pod(mock_run):
    expected = "some output"
    mock_run.return_value = CompletedProcess(
        args="testing",
        returncode=0,
        stdout=expected
    )
    results = KubeCtl().describe_pod("pod")
    assert expected in results


def ensure_logs_exist(
    log_dir: Path,
    pods: list[str],
    containers: list[str],
    description: str,
    log: str
) -> None:
    assert log_dir.exists()
    for pod in pods:
        describe_file = log_dir / f"{pod}.describe.txt"
        assert describe_file.exists()
        with open(describe_file, "r") as f:
            assert f.read() == description
        for container in containers:
            log_file = log_dir / f"{pod}.{container}.log.txt"
            assert log_file.exists()
            with open(log_file, "r") as f:
                assert f.read() == log


@patch("python.kubectl.kubectl.kubectl.KubeCtl.get_pods_for_resource")
@patch("python.kubectl.kubectl.kubectl.KubeCtl.describe_pod")
@patch("python.kubectl.kubectl.kubectl.KubeCtl.get_containers_in_pod")
@patch("python.kubectl.kubectl.kubectl.KubeCtl.get_logs_from_container")
def test_save_resource_logs(
    mock_get_logs_from_container,
    mock_get_containers_in_pod,
    mock_describe_pod,
    mock_get_pods_for_resource,
    tmp_path
):
    pods = ["pod-1", "pod-2", "pod-3"]
    containers = ["container-1", "container-2"]
    description = "sample\npod\ndescription"
    log = "sample\nlog\noutput"
    mock_get_pods_for_resource.return_value = pods
    mock_describe_pod.return_value = description
    mock_get_containers_in_pod.return_value = containers
    mock_get_logs_from_container.return_value = log
    log_dir = tmp_path / "container-logs"
    KubeCtl().save_resource_logs("resource", "job", log_dir)
    ensure_logs_exist(log_dir, pods, containers, description, log)


@patch("python.kubectl.kubectl.kubectl.KubeCtl.get_resources_for_namespace")
@patch("python.kubectl.kubectl.kubectl.KubeCtl.get_pods_for_resource")
@patch("python.kubectl.kubectl.kubectl.KubeCtl.describe_pod")
@patch("python.kubectl.kubectl.kubectl.KubeCtl.get_containers_in_pod")
@patch("python.kubectl.kubectl.kubectl.KubeCtl.get_logs_from_container")
def test_save_logs(
    mock_get_logs_from_container,
    mock_get_containers_in_pod,
    mock_describe_pod,
    mock_get_pods_for_resource,
    mock_get_resources_for_namespace,
    tmp_path
):
    resources = ["resource-1", "resource-2"]
    pods = ["pod-1", "pod-2", "pod-3"]
    containers = ["container-1", "container-2"]
    description = "sample\npod\ndescription"
    log = "sample\nlog\noutput"
    mock_get_resources_for_namespace.return_value = resources
    mock_get_pods_for_resource.return_value = pods
    mock_describe_pod.return_value = description
    mock_get_containers_in_pod.return_value = containers
    mock_get_logs_from_container.return_value = log
    log_dir = tmp_path / "container-logs"
    KubeCtl().save_logs(log_dir)
    for _ in resources:
        ensure_logs_exist(log_dir, pods, containers, description, log)


@patch("subprocess.run")
def test_get_namespaces(mock_run):
    expected = ["foo", "bar", "baz"]
    mock_run.return_value = CompletedProcess(
        args="testing",
        returncode=0,
        stdout="\n".join(expected)
    )
    result = KubeCtl().get_namespaces()
    assert result == expected


@patch("subprocess.run")
def test_get_resource_not_found(mock_run):
    mock_run.return_value = CompletedProcess(
        args="",
        returncode=1,
        stdout="",
        stderr="NotFound"
    )
    assert KubeCtl().get_resource("resource-name") == []


def test_get_resource_raises():
    with pytest.raises(RuntimeError) as e:
        KubeCtl("my-namespace").get_resource(
            "resource-name",
            selectors=["foo", "bar"]
        )  # yapf: disable
    msg = e.value.args[0]
    assert "Failed to get resource" in msg
    command = (
        "kubectl get resource-name --namespace my-namespace --output json "
        "--selector=foo --selector=bar"
    )
    assert command in msg


@patch("subprocess.run")
def test_get_resource_items(mock_run):
    mock_run.return_value = CompletedProcess(
        args="",
        returncode=0,
        stdout="""{
    "apiVersion": "v1",
    "items": [
        {
            "apiVersion": "batch/v1",
            "kind": "Job",
            "metadata": {
                "foo": "bar"
            }
        },
        {
            "metadata": {
                "baz": "bif"
            }
        }
    ],
    "kind": "List"
}""",
        stderr=""
    )
    result = KubeCtl().get_resource("resource-name")
    assert len(result) == 2
    assert result[0]["apiVersion"] == "batch/v1"
    assert result[0]["kind"] == "Job"
    assert result[0]["metadata"]["foo"] == "bar"
    assert result[1]["metadata"]["baz"] == "bif"


@patch("subprocess.run")
def test_get_resource_no_items(mock_run):
    mock_run.return_value = CompletedProcess(
        args="",
        returncode=0,
        stdout="""{
    "apiVersion": "apps/v1",
    "kind": "Deployment",
    "metadata": {
        "foo": "bar"
    }
}""",
        stderr=""
    )
    result = KubeCtl().get_resource("resource-name")
    assert len(result) == 1
    assert result[0]["apiVersion"] == "apps/v1"
    assert result[0]["kind"] == "Deployment"
    assert result[0]["metadata"]["foo"] == "bar"


@pytest.mark.parametrize(
    "service",
    ["interactive-analysis-api-gateway",
     "bogus-service-name"]
)
@patch("python.kubectl.kubectl.kubectl.KubeCtl.istio_enabled")
@patch("python.kubectl.kubectl.kubectl.KubeCtl.get_resource")
def test_get_endpoints_istio_multiple(
    mock_get_resource,
    mock_istio_enabled,
    service
):
    mock_get_resource.return_value = [{
        "spec": {
            "hosts": ["namespace.domain"],
            "http": [{
                "match": [{
                    "uri": {
                        "prefix":
                        "/interactive-analysis-api-gateway/subscriptions"
                    }
                }],
                "route": [{
                    "destination": {
                        "host": f"{service}.namespace.svc.cluster.local",
                        "port": {
                            "number": 8080
                        }
                    }
                }]
            },
                     {
                         "match": [{
                             "uri": {
                                 "prefix": "/interactive-analysis-api-gateway"
                             }
                         }],
                         "route": [{
                             "destination": {
                                 "host":
                                 f"{service}.namespace.svc.cluster.local",
                                 "port": {
                                     "number": 8080
                                 }
                             }
                         }]
                     }]
        },
    }]
    mock_istio_enabled.return_value = True
    result = KubeCtl().get_endpoints("interactive-analysis-api-gateway")
    if "bogus" in service:
        assert result == (None, None)
    else:
        assert result == (
            "namespace.domain",
            [
                "/interactive-analysis-api-gateway/subscriptions",
                "/interactive-analysis-api-gateway"
            ]
        )


@pytest.mark.parametrize(
    "service",
    ["interactive-analysis-api-gateway",
     "bogus-service-name"]
)
@patch("python.kubectl.kubectl.kubectl.KubeCtl.istio_enabled")
@patch("python.kubectl.kubectl.kubectl.KubeCtl.get_resource")
def test_get_endpoints_no_istio_multiple(
    mock_get_resource,
    mock_istio_enabled,
    service
):
    mock_get_resource.return_value = [{
        "spec": {
            "rules": [{
                "host": "namespace.domain",
                "http": {
                    "paths": [{
                        "backend": {
                            "service": {
                                "name": service,
                                "port": {
                                    "number": 8080
                                }
                            }
                        },
                        "path":
                        "/interactive-analysis-api-gateway/subscriptions",
                        "pathType": "Prefix"
                    },
                              {
                                  "backend": {
                                      "service": {
                                          "name": service,
                                          "port": {
                                              "number": 8080
                                          }
                                      }
                                  },
                                  "path": "/interactive-analysis-api-gateway",
                                  "pathType": "Prefix"
                              }]
                }
            }],
        },
    }]
    mock_istio_enabled.return_value = False
    result = KubeCtl().get_endpoints("interactive-analysis-api-gateway")
    if "bogus" in service:
        assert result == (None, None)
    else:
        assert result == (
            "namespace.domain",
            [
                "/interactive-analysis-api-gateway/subscriptions",
                "/interactive-analysis-api-gateway"
            ]
        )


@pytest.mark.parametrize(
    "service",
    ["interactive-analysis-ui",
     "bogus-service-name"]
)
@patch("python.kubectl.kubectl.kubectl.KubeCtl.istio_enabled")
@patch("python.kubectl.kubectl.kubectl.KubeCtl.get_resource")
def test_get_endpoints_istio_single(
    mock_get_resource,
    mock_istio_enabled,
    service
):
    mock_get_resource.return_value = [{
        "spec": {
            "hosts": ["namespace.domain"],
            "http": [{
                "match": [{
                    "uri": {
                        "prefix": "/interactive-analysis-ui"
                    }
                }],
                "route": [{
                    "destination": {
                        "host": f"{service}.namespace.svc.cluster.local",
                        "port": {
                            "number": 8080
                        }
                    }
                }]
            }]
        },
    }]
    mock_istio_enabled.return_value = True
    result = KubeCtl().get_endpoints("interactive-analysis-ui")
    if "bogus" in service:
        assert result == (None, None)
    else:
        assert result == ("namespace.domain", ["/interactive-analysis-ui"])


@pytest.mark.parametrize(
    "service",
    ["interactive-analysis-ui",
     "bogus-service-name"]
)
@patch("python.kubectl.kubectl.kubectl.KubeCtl.istio_enabled")
@patch("python.kubectl.kubectl.kubectl.KubeCtl.get_resource")
def test_get_endpoints_no_istio_single(
    mock_get_resource,
    mock_istio_enabled,
    service
):
    mock_get_resource.return_value = [{
        "spec": {
            "rules": [{
                "host": "namespace.domain",
                "http": {
                    "paths": [{
                        "backend": {
                            "service": {
                                "name": service,
                                "port": {
                                    "number": 8080
                                }
                            }
                        },
                        "path": "/interactive-analysis-ui",
                        "pathType": "Prefix"
                    }]
                }
            }],
        },
    }]
    mock_istio_enabled.return_value = False
    result = KubeCtl().get_endpoints("interactive-analysis-ui")
    if "bogus" in service:
        assert result == (None, None)
    else:
        assert result == ("namespace.domain", ["/interactive-analysis-ui"])


@pytest.mark.parametrize(
    "stdout",
    [
        "foo   0     7m6s   app.kubernetes.io/managed-by=Helm,"
        "foo/image-tag=develop,"
        "foo/name=my-namespace,"
        "foo/namespace=my-namespace,"
        "foo/type=baz,"
        "foo/update-time=2022-04-14T190732Z,"
        "foo/user=me",
        ""
    ],
    ids=["labels",
         "no-labels"]
)
@patch("subprocess.run")
def test_get_configmap_labels(mock_run, stdout):
    mock_run.return_value = CompletedProcess(
        args="",
        returncode=0,
        stdout=stdout,
        stderr=""
    )
    result = KubeCtl().get_configmap_labels("foo")
    if stdout:
        assert result["app.kubernetes.io/managed-by"] == "Helm"
        assert result["foo/image-tag"] == "develop"
        assert result["foo/name"] == "my-namespace"
        assert result["foo/namespace"] == "my-namespace"
        assert result["foo/type"] == "baz"
        assert result["foo/update-time"] == "2022-04-14T190732Z"
        assert result["foo/user"] == "me"
    else:
        assert result is None


@patch("subprocess.run")
def test_get_namespace_labels(mock_run):
    mock_run.return_value = CompletedProcess(
        args="",
        returncode=0,
        stdout="foo bar baz key1=value1,key2=value2",
        stderr=""
    )
    labels = KubeCtl().get_namespace_labels()
    assert labels["key1"] == "value1"
    assert labels["key2"] == "value2"


@pytest.mark.parametrize(
    "stdout, expected",
    [("foo bar baz istio-injection=enabled",
      True),
     ("foo bar baz key1=value1,key2=value2",
      False),
     ("foo bar baz <none>",
      False)]
)
@patch("subprocess.run")
def test_istio_enabled(mock_run, stdout, expected):
    mock_run.return_value = CompletedProcess(
        args="",
        returncode=0,
        stdout=stdout,
        stderr=""
    )
    assert KubeCtl().istio_enabled() == expected


@patch("subprocess.run")
def test_istio_enabled_raises(mock_run):
    mock_run.return_value = CompletedProcess(
        args="",
        returncode=0,
        stdout="foo bar baz bif",
        stderr=""
    )
    with pytest.raises(ValueError):
        KubeCtl().istio_enabled()


@pytest.mark.parametrize("stdout, expected", [("42", 42), ("0 1 2", 2)])
@patch("subprocess.run")
def test_get_resource_exit_code(mock_run, stdout, expected):
    mock_run.return_value = CompletedProcess(
        args="",
        returncode=0,
        stdout=stdout,
        stderr=""
    )
    assert KubeCtl().get_resource_exit_code("type/resource") == expected


@patch("subprocess.run")
def test_get_resource_exit_code_raises(mock_run):
    mock_run.return_value = CompletedProcess(
        args="",
        returncode=0,
        stdout="",
        stderr=""
    )
    with pytest.raises(RuntimeError):
        KubeCtl().get_resource_exit_code("type/resource")
    mock_run.return_value = CompletedProcess(
        args="",
        returncode=1,
        stdout="foo",
        stderr="bar"
    )
    with pytest.raises(RuntimeError):
        KubeCtl().get_resource_exit_code("type/resource")


@pytest.mark.parametrize(
    "resource, condition, expected",
    [
        ("foo",
         "delete",
         ["--for=delete"]),
        ("foo",
         "bar",
         ["--for=condition=bar"]),
        (
            "jobs/foo",
            "",
            ["--for=condition=complete",
             "--for=condition=failed"]
        ),
        ("pods/foo",
         "",
         ["--for=condition=ready"]),
        ("foo",
         "",
         ["--for=condition=available"]),
    ]
)
def test__get_wait_flags(resource, condition, expected):
    assert KubeCtl()._get_wait_flags(resource, condition) == expected


@pytest.mark.parametrize(
    "flag, returncode, stderr, expected",
    [
        ("--for=delete", 0, "", 0),
        ("--for=condition=failed", 0, "", 42),
        ("--for=delete", 1, "NotFound", 0),
        ("--for=delete", 1, "no matching", 0),
        ("foo", 1, "NotFound", None),
        ("foo", 1, "no matching", None),
        ("foo", 1, "timed out", None),
        ("foo", 1, "bar", 1),
    ]
)  # yapf: disable
@patch("python.kubectl.kubectl.kubectl.KubeCtl.get_resource_exit_code")
def test__get_wait_return_value(
    mock_get_resource_exit_code,
    flag,
    returncode,
    stderr,
    expected
):
    mock_get_resource_exit_code.return_value = expected
    assert KubeCtl()._get_wait_return_value(
        "resource",
        flag,
        CompletedProcess(
            args="",
            returncode=returncode,
            stdout="",
            stderr=stderr
        )
    ) == expected


@patch("subprocess.run")
def test_wait(mock_run):
    mock_run.return_value = CompletedProcess(
        args="",
        returncode=0,
        stdout="",
        stderr=""
    )
    assert KubeCtl().wait("foo") == 0
    mock_run.return_value = CompletedProcess(
        args="",
        returncode=1,
        stdout="",
        stderr="no matching"
    )
    assert KubeCtl().wait("foo", condition="delete") == 0
    mock_run.return_value = CompletedProcess(
        args="",
        returncode=1,
        stdout="",
        stderr=""
    )
    assert KubeCtl().wait("foo") == 1
    timeout = 6
    start_time = time()
    mock_run.return_value = CompletedProcess(
        args="",
        returncode=1,
        stdout="",
        stderr="timed out"
    )
    mock_run.side_effect = sleep(5)
    assert KubeCtl().wait("foo", timeout=timeout) == 1
    assert time() - start_time > timeout - 5
    mock_run.side_effect = TimeoutExpired("foo", 5)
    assert KubeCtl().wait("foo") == 1


@pytest.mark.parametrize("expected", [True, False])
@patch("python.kubectl.kubectl.kubectl.KubeCtl.get_resource")
def test_resource_failed_to_start(mock_get_resource, expected):
    mock_get_resource.return_value = [] if expected else [{"foo": "bar"}]
    timeout = 1
    start_time = time()
    assert KubeCtl().resource_failed_to_start("baz", timeout) == expected
    duration = time() - start_time
    if expected:
        assert duration > timeout
    else:
        assert duration < timeout
