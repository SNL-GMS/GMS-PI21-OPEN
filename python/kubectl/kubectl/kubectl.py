#!/usr/bin/env python3

import json
from pathlib import Path
import re
from shutil import get_terminal_size
import subprocess
import sys
from tenacity import (
    Retrying,
    stop_after_attempt,
    stop_after_delay,
    TryAgain,
    wait_fixed
)
from time import sleep


class KubeCtl:
    """
    This class bundles a number of convenience functions that are
    wrappers around calls to `kubectl` in the shell.

    Attributes:
        namespace:  The Kubernetes namespace.
    """

    def __init__(self, namespace: str | None = None):
        self.namespace = "default" if namespace is None else namespace
        self.validate_namespace()

    def validate_namespace(self) -> None:
        """
        Check that the namespace is a valid Kubernetes namespace name.

        Raises:
            ValueError:  If the name is invalid.
        """
        pattern = re.compile(r'^[a-z0-9]([-a-z0-9]{1,61}[a-z0-9])?$')
        if not pattern.match(self.namespace):
            raise ValueError(
                "The name of the namespace must (1) consist only of lowercase "
                "letters, numbers, and hyphens, (2) not start or end with a "
                "hyphen, and (3) be 63 characters or less."
            )

    @staticmethod
    def erase_line() -> None:
        """
        Erase the current line of text in the terminal by filling it
        with spaces.

        TODO:  Move this out into its own module.
        """
        print("\r" + " " * get_terminal_size().columns + "\r", end="")

    @staticmethod
    def type_text(message: str) -> None:
        """
        Print out some text one character at a time as if someone was
        typing it at the terminal.

        Args:
            message:  The message to be printed.

        TODO:  Move this out into its own module.
        """
        for character in message:
            sys.stdout.write(character)
            sys.stdout.flush()
            sleep(0.005)

    def all_pods_running_or_succeeded(self, retry_delay: float) -> bool:
        """
        Get all the pods in the namespace, and determine whether or not
        their current phases are either "Running" or "Succeeded".

        Args:
            retry_delay:  The number of seconds to wait before retrying.

        Raises:
            RuntimeError:  If a pod "Failed" or returned an unknown
                status.

        Returns:
            Whether or not all the pods in the given namespace are
            running.
        """
        pods = json.loads(
            subprocess.run(
                f"kubectl get pods --namespace {self.namespace} --output json",
                shell=True,
                capture_output=True,
                text=True
            ).stdout.strip()
        )
        if not pods["items"]:
            self.erase_line()
            self.type_text(
                f"Namespace '{self.namespace}' doesn't have any pods yet.  "
                f"Retrying in {retry_delay} seconds..."
            )
            return False
        for pod in pods["items"]:
            if pod["status"]["phase"] in ["Running", "Succeeded"]:
                continue
            elif pod["status"]["phase"] == "Pending":
                self.erase_line()
                self.type_text(
                    f"Pod '{pod['metadata']['name']}' is still 'Pending'.  "
                    f"Retrying in {retry_delay} seconds..."
                )
                return False
            elif pod["status"]["phase"] == "Failed":
                raise RuntimeError(
                    f"{pod['metadata']['name']} failed to start."
                )
            else:
                raise RuntimeError(
                    f"{pod['metadata']['name']} is in an unknown phase:  "
                    f"'{pod['status']['phase']}'"
                )
        return True

    def all_pods_running_or_succeeded_retry_wrapper(
        self,
        retry_delay: float
    ) -> None:
        """
        Wrap :func:`all_pods_running_or_succeeded` to make it suitable
        for retrying.

        Args:
            retry_delay:  The number of seconds to wait before retrying.

        Raises:
            RuntimeError:  If a pod "Failed" or returned an unknown
                status.
            TryAgain:  If not all the pods are "Running" or "Succeeded"
                yet.
        """
        if not self.all_pods_running_or_succeeded(retry_delay):
            raise TryAgain

    def get_pods(self) -> str:
        """
        Get the results of ``kubectl get pods``.

        Returns:
            The plain-text table of pods in the namespace.
        """
        return subprocess.run(
            f"kubectl get pods --namespace {self.namespace}",
            shell=True,
            capture_output=True,
            text=True
        ).stdout.strip()

    def get_logs_from_container(self, pod: str, container: str) -> str:
        """
        Retrieve the logs from the container in the given pod.

        Args:
            pod:  The name of the pod.
            container:  The name of the container within the pod.

        Returns:
            The logs from the container.
        """
        return subprocess.run(
            f"kubectl --namespace {self.namespace} logs {pod} {container}",
            shell=True,
            capture_output=True,
            text=True
        ).stdout

    def get_containers_in_pod(self, pod: str) -> list[str]:
        """
        Get the list of containers in the given pod.

        Args:
            pod:  The name of the pod.

        Returns:
            The list of containers.
        """
        return subprocess.run(
            f"kubectl --namespace {self.namespace} get pods {pod} --output "
            "jsonpath='{.spec.containers[*].name}'",
            shell=True,
            capture_output=True,
            text=True
        ).stdout.strip().split()

    RESOURCE_SELECTORS = {
        "deployment": "app",
        "statefulset": "app.kubernetes.io/component",
        "job": "job-name"
    }
    """
    A mapping from resource types to the corresponding selector arguments.
    """

    def get_pods_for_resource(self,
                              resource: str,
                              resource_type: str) -> list[str]:
        """
        Get the list of pods for the given resource.

        Args:
            resource:  The resource to get the pods from.
            resource_type:  The type of the resource.

        Returns:
            The list of pods.
        """
        return subprocess.run(
            f"kubectl --namespace {self.namespace} get pods "
            f"--selector={self.RESOURCE_SELECTORS[resource_type]}={resource} "
            "--output custom-columns=NAME:.metadata.name --no-headers",
            shell=True,
            capture_output=True,
            text=True
        ).stdout.strip().splitlines()

    def get_resources_for_namespace(self, resource_type: str) -> list[str]:
        """
        Get the list of resources available for the namespace.

        Args:
            resource_type:  The type of resource to get.

        Returns:
            The list of resources.
        """
        return subprocess.run(
            f"kubectl --namespace {self.namespace} get {resource_type} "
            "--output custom-columns=NAME:.metadata.name --no-headers",
            shell=True,
            capture_output=True,
            text=True
        ).stdout.strip().splitlines()

    def wait_for_all_pods_running_or_succeeded(
        self,
        *,
        timeout: int = 60,
        retry_delay: float = 0,
        max_attempts: int | None = None
    ) -> None:
        """
        Wait for all the pods in the given ``namespace`` to reach the
        phases "Running" or "Succeeded".

        Args:
            timeout:  The number of seconds to wait before giving up.
            retry_delay:  The number of seconds to wait before retrying.
            max_attempts:  If specified, the maximum number of times to
                try calling :func:`retry_all_pods_running_or_succeeded`.

        Raises:
            RetryError:  If the pods aren't all "Running" or "Succeeded"
                within the given retry specifications.
        """
        stop_condition = stop_after_delay(timeout)
        if max_attempts is not None:
            stop_condition = stop_condition | stop_after_attempt(max_attempts)
        retry = Retrying(stop=stop_condition, wait=wait_fixed(retry_delay))
        retry(self.all_pods_running_or_succeeded_retry_wrapper, retry_delay)

    def describe_pod(self, pod: str) -> str:
        """
        Run ``kubectl describe pod`` on the given ``pod``.

        Args:
            pod:  The name of the pod to describe.

        Returns:
            The plain-text results of that command.
        """
        return subprocess.run(
            f"kubectl describe pod {pod} --namespace {self.namespace}",
            shell=True,
            capture_output=True,
            text=True
        ).stdout.strip()

    def save_resource_logs(
        self,
        resource: str,
        resource_type: str,
        log_dir: Path
    ) -> None:
        """
        Save the logs from all the containers in all the pods for the
        given resource, along with the corresponding pod descriptions.

        Args:
            resource:  The name of the resource.
            resource_type:  The type of the resource.
            log_dir:  The directory in which to save the container logs.
        """
        log_dir.mkdir(parents=True, exist_ok=True)
        pods = self.get_pods_for_resource(resource, resource_type)
        for pod in pods:
            pod_description = self.describe_pod(pod)
            with open(log_dir / f"{pod}.describe.txt", "w") as f:
                f.write(pod_description)
            containers = self.get_containers_in_pod(pod)
            for container in containers:
                log = self.get_logs_from_container(pod, container)
                with open(log_dir / f"{pod}.{container}.log.txt", "w") as f:
                    f.write(log)

    def save_logs(self, log_dir: Path) -> None:
        """
        Save the logs for all the resources, along with the
        corresponding pod descriptions.

        Args:
            log_dir:  The directory in which to save the logs.
        """
        for resource_type in self.RESOURCE_SELECTORS:
            resources = self.get_resources_for_namespace(resource_type)
            for resource in resources:
                self.save_resource_logs(resource, resource_type, log_dir)

    @staticmethod
    def get_namespaces() -> list[str]:
        """
        Get the list of all namespaces in the currently configured
        Kubernetes cluster.

        Returns:
            The list of namespaces.
        """
        return subprocess.run(
            "kubectl get namespaces --output "
            "custom-columns=NAME:.metadata.name --no-headers",
            shell=True,
            capture_output=True,
            text=True
        ).stdout.strip().splitlines()

    def get_resource(
        self,
        resource_name: str,
        selectors: list[str] | None = None
    ) -> list[dict]:
        """
        Get a Kubernetes resource.

        Args:
            resource_name:  The name of the resource to get.
            selectors:  One or more selector strings to use for
                filtering the results.

        Raises:
            RuntimeError:  If something goes wrong with the ``kubectl
                get`` command in the shell.

        Returns:
            A list of dictionaries corresponding to the requested
            resource, or an empty list if the resource doesn't exist.
        """
        selector_argument = ""
        for s in selectors or []:
            selector_argument += f"--selector={s} "
        completed_process = subprocess.run(
            f"kubectl get {resource_name} --namespace {self.namespace} "
            f"--output json {selector_argument}",
            shell=True,
            capture_output=True,
            text=True
        )
        if completed_process.returncode != 0:
            if "NotFound" in completed_process.stderr:
                return []
            else:
                raise RuntimeError(
                    f"Failed to get resource '{resource_name}' from namespace "
                    f"'{self.namespace}'.\n{completed_process}"
                )
        else:
            results = json.loads(completed_process.stdout)
            if "items" in results:
                return results["items"]
            else:
                return [results]

    def get_endpoints(
        self,
        ingress_name: str
    ) -> tuple[str | None, str | None]:  # yapf: disable
        """
        Get the URL endpoints for the given ingress.

        Args:
            ingress_name:  The name of the ingress resource to get.

        Returns:
            The (host, path list) tuple, if present; ``None`` for both
            otherwise.
        """
        endpoint_list = []
        if self.istio_enabled():
            vs = self.get_resource(f"virtualservice/{ingress_name}")[0]
            host = vs["spec"]["hosts"][0]
            for http in vs["spec"]["http"]:
                service = http["route"][0]["destination"]["host"].split(".")[0]
                if service == ingress_name:
                    endpoint_list.append(http["match"][0]["uri"]["prefix"])
        else:
            ingress = self.get_resource(f"ingress/{ingress_name}")[0]
            rule = ingress["spec"]["rules"][0]
            host = rule["host"]
            for path in rule["http"]["paths"]:
                service = path["backend"]["service"]["name"]
                if service == ingress_name:
                    endpoint_list.append(path["path"])
        return (host, endpoint_list) if endpoint_list else (None, None)

    @staticmethod
    def _split_labels(labels: str) -> dict[str, str]:
        """
        Translate the labels from ``kubectl get ... --show-labels`` into
        a Python ``dict``.

        Args:
            labels:  A comma-separated string of ``key=value`` pairs.

        Returns:
            The corresponding dictionary representation.

        Raises:
            ValueError:  If an item in the comma-separated list isn't of
                the form ``key=value``.
        """
        result = {}
        if labels != "<none>":
            for item in labels.split(","):
                if "=" not in item:
                    raise ValueError(
                        f"The label `{item}` is not in the form `key=value`."
                    )
                key, value = item.split("=")
                result[key] = value
        return result

    def get_configmap_labels(self, configmap: str) -> dict[str, str] | None:
        """
        Get the labels for the namespace corresponding to the given
        ConfigMap.

        Args:
            configmap:  The name of the ConfigMap from which to grab the
                labels.

        Returns:
            The key-value pairs representing the labels, or ``None`` if
            the ConfigMap isn't found.
        """
        command = (
            f"kubectl get configmap --namespace {self.namespace} "
            f"--show-labels --no-headers {configmap}"
        )
        labels = subprocess.run(
            command,
            shell=True,
            capture_output=True,
            text=True
        ).stdout
        return self._split_labels(labels.split()[3]) if labels else None

    def get_namespace_labels(self) -> dict:
        """
        Get the labels for the namespace

        Returns:
            The key-value pairs representing the labels.
        """
        command = (
            f"kubectl get namespace {self.namespace} --show-labels "
            "--no-headers"
        )
        labels = subprocess.run(
            command,
            shell=True,
            capture_output=True,
            text=True
        ).stdout.split()[3]
        return self._split_labels(labels)

    def istio_enabled(self) -> bool:
        """
        Check if Istio is enabled.

        Returns:
            ``True`` if the object's namespace is labeled with
            ``istio-injection=enabled``; ``False`` otherwise.
        """
        labels = self.get_namespace_labels()
        try:
            return labels["istio-injection"] == "enabled"
        except KeyError:
            return False

    def get_resource_exit_code(self, resource_name: str) -> int:
        """
        Look up the exit code for a given resource.

        Args:
            resource_name:  The name of the resource to look up the exit
                code for.

        Raises:
            RuntimeError:  If the exit code is unable to be determined.

        Returns:
            The exit code from the completed resource.
        """
        if "/" in resource_name:
            _, resource = resource_name.split("/", 1)
        else:
            resource = resource_name

        # Define the selector to find the corresponding pod for the
        # given resource.
        if resource_name.startswith("jobs"):
            selector = f"--selector='job-name=={resource}'"
        else:
            selector = f"--selector='name=={resource}'"

        # `kubectl` will print the `exitCode` if present; otherwise it
        # prints nothing.
        completed_process = subprocess.run(
            f"kubectl get pods --namespace {self.namespace} {selector} "
            "--output jsonpath='{.items[*].status.containerStatuses[*].state."
            "terminated.exitCode}'",
            shell=True,
            capture_output=True,
            text=True
        )
        if (
            completed_process.returncode == 0
            and len(completed_process.stdout) > 0
        ):
            return max([int(_) for _ in completed_process.stdout.split()])
        else:
            raise RuntimeError(
                "Unable to determine the exit code for resource "
                f"'{resource_name}' in namespace '{self.namespace}'.\n"
                f"{completed_process}"
            )

    @staticmethod
    def _get_wait_flags(resource: str, condition: str) -> list[str]:
        """
        Get the flags to pass to ``kubectl wait``.

        Args:
            resource:  A resource from the augmentation to wait for.
            condition: The condition to wait for (e.g. 'ready',
                'delete').  If no condition is specified, a reasonable
                default condition will be chosen based on the resource
                type.

        Returns:
            A list of flags to wait for.
        """
        if condition != "":
            return [
                "--for=delete"
                if condition == "delete" else f"--for=condition={condition}"
            ]
        elif resource.startswith("jobs"):
            return ["--for=condition=complete", "--for=condition=failed"]
        elif resource.startswith("pods"):
            return ["--for=condition=ready"]
        else:
            return ["--for=condition=available"]

    def _get_wait_return_value(
        self,
        resource: str,
        flag: str,
        completed_process: subprocess.CompletedProcess
    ) -> int | None:
        """
        Determine the appropriate return value after executing the
        ``kubectl wait`` command.  If none can be found, that means the
        wait is not yet complete.

        Args:
            resource:  The resource from which to get the exit code.
            flag:  The ``--for=`` flag passed to ``kubectl wait``.
            completed_process:  The results of running the ``kubectl
                wait`` command in the shell.

        Returns:
            An integer indicating success (0) or failure (non-zero), or
            ``None`` if we're not done waiting yet.
        """
        if completed_process.returncode == 0:
            return (
                self.get_resource_exit_code(resource)
                if flag == "--for=condition=failed" else 0
            )
        elif completed_process.returncode > 0:

            # Ignore `NotFound` errors since the resource may not have
            # been created yet.
            if (
                "NotFound" in completed_process.stderr
                or "no matching" in completed_process.stderr
            ):

                # If we are waiting to delete and we can't find it, then
                # it is already deleted.
                if flag == "--for=delete":
                    return 0

            # Did the `kubectl` command fail with an error?
            elif "timed out" not in completed_process.stderr:
                return 1
        return None

    def wait(
        self,
        resource: str,
        condition: str = "",
        timeout: int = 1200
    ) -> int:
        """
        Wait for a condition to be met on a resource.

        If no ``condition`` is specified, a reasonable default condition
        will be chosen based on the ``resource`` type.  For example, for
        a 'job' resource (e.g., 'job/test-job') this will wait for the
        job to 'complete' or 'fail'.  The condition 'ready' will wait
        for a resource to be ready, and 'delete' will wait for resources
        to be deleted.

        Args:
            resource:  A resource from the augmentation to wait for.
            condition: The condition to wait for (e.g. 'ready',
                'delete').  If no condition is specified, a reasonable
                default condition will be chosen based on the resource
                type.
            timeout:  How long to wait (in seconds) for the condition to
                be met before giving up.

        Returns:
            The exit code of the job if the job failed; 0 if the wait
            was successful; 1 if the ``kubectl`` command failed for an
            unknown reason or the timeout expired.

        Todo:
            * Refactor the retry functionality to use ``tenacity``.
        """
        interval = 5
        remaining = timeout
        while True:
            for flag in self._get_wait_flags(resource, condition):
                command = (
                    f"kubectl wait --namespace {self.namespace} {flag} "
                    + resource
                )
                try:
                    completed_process = subprocess.run(
                        command,
                        shell=True,
                        capture_output=True,
                        text=True,
                        timeout=interval
                    )
                except subprocess.TimeoutExpired:
                    completed_process = subprocess.CompletedProcess(
                        args=command,
                        returncode=-1,
                        stdout="",
                        stderr=""
                    )
                return_value = self._get_wait_return_value(
                    resource,
                    flag,
                    completed_process
                )
                if return_value is not None:
                    return return_value
                remaining -= interval
                if remaining <= 0:
                    return 1

    def resource_failed_to_start(
        self,
        resource: str,
        timeout: int = 10
    ) -> int:
        """
        Check if a resource failed to even start.  Resources may fail
        due to things like formatting errors or missing requirements and
        never even reach a starting state.  This function checks the
        status of the resources and fails if it fails to have any status
        after some timeout period.

        Args:
            resource:  A resource to check for failures.
            timeout:  How long to wait (in seconds) for the resource to
                start before giving up.

        Returns:
            ``True`` if the resource failed to report any status in the
            timeout period; ``False`` otherwise.

        Todo:
            * Rewrite the retry loop using ``tenacity``.
        """
        remaining = timeout
        sleep_time = 1
        while remaining > 0:
            if self.get_resource(resource):
                return False
            sleep(sleep_time)
            remaining -= sleep_time
        return True
