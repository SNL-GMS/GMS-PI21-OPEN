#!/usr/bin/env python3

import functools
import os
import re
import secrets
import shlex
import string
import subprocess
import sys
import tarfile
import textwrap
import uuid
from argparse import (
    ArgumentParser,
    ArgumentTypeError,
    RawDescriptionHelpFormatter
)
from collections import defaultdict
from datetime import datetime
from minio import Minio
from pathlib import Path
from shutil import which
from signal import SIGINT, signal
from time import sleep
from typing import Any, Callable

import rich.traceback
from rich.console import Console
from rich.padding import Padding
from rich.panel import Panel
from rich.progress import track
from tenacity import RetryError

sys.path.append(str(Path(__file__).resolve().parents[4] / "python"))
from kubectl import KubeCtl  # noqa: E402
from reverse_argparse import ReverseArgumentParser  # noqa: E402

rich.traceback.install()


class GMSSystemTest:
    """
    This class is designed to handle our automated testing against
    running instances of the system.  It contains the following stages:

    Install
        Install an instance of the system for testing purposes.
    Wait
        Wait for all the pods to reach a "Running" or "Succeeded" state.
    Sleep
        Sleep an additional amount of time to wait for the applications
        in all the pods to be ready to process data.
    Test
        Apply a test augmentation to the running instance of the system
        and collect the results.
    Uninstall
        Uninstall the instance now that testing is complete.

    Attributes:
        args (Namespace):  The parsed command line arguments for the
            script.
        commands_executed (List[str]):  The commands that were executed
            in the shell.
        console (Console):  Used to print rich text to the console.
        current_stage (str):  The name of the stage being run.
        dry_run (bool):  If ``True``, don't actually run the command
            that would be executed in the shell; instead just print it
            out.
        durations (dict):  A mapping from stage names to how long it
            took for each to run.
        instance_name (str):  The name of the instance that will be
            used for testing purposes.
        instance_ready (bool):  Whether all the pods are in either a
            "Running" or "Completed" state.
        instance_tag (str):  The name of the Docker image tag.
        instance_type (str):  The name of the Docker image tag.
        kubectl (KubeCtl):  A :class:`KubeCtl` instance initialized with
            ``instance_name`` as the namespace.
        kubectl_gms (KubeCtl):  A :class:`KubeCtl` instance for the
            ``gms`` namespace.
        log_dir (Path):  The directory for collecting container logs.
        minio (dict):  The information needed to communicate with the
            MinIO test reporting service.
        parallel (int):  How many identical test augmentation pods to
            launch in parallel.
        reports_dir (Path):  The directory for collecting test reports.
        retry_attempts (Optional[int]):  How many times to check to see
            if all the pods are running.  ``None`` means don't restrict
            the number of attempts.
        retry_delay (float):  How long to wait between retries when
            checking if all the pods are up.
        retry_timeout (int):  How long to wait (in seconds) for all the
            pods in the instance to spin up.
        set_args (list[str]):  The ``--set`` arguments to pass to
            ``gmskube`` when applying the test augmentation.
        sleep (int):  How many seconds to sleep while waiting for the
            system to be ready.
        stage_start_time (datetime):  The time at which a stage began.
        stages_to_run (list[str]):  Which stages to run.
        start_time (datetime):  The time at which this object was
            initialized.
        test_name (str):  The name of the test to run.
        test_success (bool):  Whether the tests passed successfully.
        timeout (int):  How long (in seconds) to wait for a test
            augmentation to complete.
    """

    def __init__(self):
        self.args = None
        self.commands_executed = []
        self.current_stage = None
        self.dry_run = None
        self.durations = {}
        self.instance_name = None
        self.instance_ready = False
        self.instance_tag = None
        self.instance_type = None
        self.kubectl = None
        self.kubectl_gms = KubeCtl("gms")
        self.log_dir = None
        self.minio = {
            "access_key": str(uuid.uuid4()),
            "augmentation_name": "minio-test-reports",
            "report_bucket": "reports",
            "secret_key": str(uuid.uuid4())
        }
        self.parallel = None
        self.reports_dir = None
        self.retry_attempts = None
        self.retry_delay = None
        self.retry_timeout = None
        self.set_args = None
        self.sleep = None
        self.stage_start_time = None
        self.stages_to_run = None
        self.start_time = datetime.now()
        self.test_name = None
        self.test_success = False
        self.timeout = None
        console_kwargs = {"log_path": False}
        if os.getenv("CI"):
            console_kwargs["force_terminal"] = True
        if os.getenv("RICH_LOG_PATH"):
            console_kwargs["log_path"] = True
        self.console = Console(**console_kwargs)

    def heading(self, message: str, *, color: str = "cyan") -> None:
        """
        Print a heading to indicate at a high level what the script is
        doing.

        Args:
            message:  The message to print.
            color:  What color to print the message in.
        """
        self.console.log(Panel(f"[bold]{message}", style=color))

    def dry_run_message(self, message: str) -> None:
        """
        Print a message indicating that something is happening due to
        the script running in dry-run mode.

        Args:
            message:  The message to print.
        """
        self.console.log(
            Padding(
                Panel(f"DRY-RUN MODE:  {message}", style="yellow"),
                (0, 0, 0, 2)
            )
        )  # yapf: disable

    @staticmethod
    def ensure_commands_are_available() -> None:
        """
        Ensure the necessary commands are available such that we can
        execute them in the shell.

        Raises:
            SystemExit:  If one of the commands is unavailable.
        """
        if which("gmskube") is None:
            raise SystemExit(
                "The `gmskube` command is not available in your current "
                "shell.  Ensure you've sourced `gms-common/.bash_env` in your "
                "`.bashrc`."
            )
        if which("kubectl") is None:
            raise SystemExit(
                "The `kubectl` command is needed to run this script.  Please "
                "ensure it's installed."
            )

    @staticmethod
    def check_kubeconfig() -> None:
        """
        Ensure the ``KUBECONFIG`` environment variable is defined
        appropriately.

        Raises:
            RuntimeError:  If the environment variable is not set, if it
                points to a list of files, or if the file it points to
                doesn't exist.
        """
        if "KUBECONFIG" not in os.environ:
            raise RuntimeError(
                "Your `KUBECONFIG` environment variable must be set before "
                "running this script."
            )
        if ":" in os.environ["KUBECONFIG"]:
            raise RuntimeError(
                "It looks like your `KUBECONFIG` is set to a list of files.  "
                "Run `kubeconfig <cluster>` and then re-run this script."
            )
        config_file = Path(os.environ["KUBECONFIG"]).resolve()
        if not config_file.exists():
            raise RuntimeError(
                f"`KUBECONFIG` == `{config_file}`, but that file doesn't "
                "exist."
            )

    def create_unique_reports_directory(self) -> None:
        """
        Create a uniquely-named reports directory in the current working
        directory.
        """
        alphabet = string.ascii_lowercase + string.digits
        random_string = "".join(secrets.choice(alphabet) for _ in range(5))
        unique_name = (
            f"system-test-reports-{datetime.now():%Y%m%dT%H%M%S}-"
            + random_string
        )
        self.reports_dir = Path.cwd() / unique_name
        self.log_dir = self.reports_dir / "container-logs"
        self.log_dir.mkdir(parents=True)

    def begin_stage(self, stage_name: str, heading: str) -> None:
        """
        Execute a series of commands at the beginning of every stage.

        Args:
            stage_name:  The name of the stage.
            heading:  A heading message to print indicating what will
                happen in the stage.
        """
        self.stage_start_time = datetime.now()
        self.current_stage = stage_name
        self.heading(heading)

    def end_stage(self) -> None:
        """
        Execute a series of commands at the end of every stage.
        """
        self.durations[self.current_stage
                       ] = datetime.now() - self.stage_start_time
        self.console.log(
            f"`{self.current_stage}` stage duration:  "
            f"{str(self.durations[self.current_stage])}"
        )

    def skip_stage(self) -> None:
        """
        Execute a series of commands when skipping a stage.
        """
        self.console.log("Skipping this stage.")

    def stage(
        stage_name: str,
        heading: str,
        skip_result: Any = True
    ) -> Callable:
        """
        A decorator to automatically run a series of commands before and
        after every stage.

        Args:
            stage_name:  The name of the stage.
            heading:  A heading message to print indicating what will
                happen in the stage.
            skip_result:  The result to be returned if the stage is
                skipped.
        """

        def decorator(func: Callable) -> Callable:

            @functools.wraps(func)
            def wrapper(self, *args, **kwargs) -> Any:
                self.begin_stage(stage_name, heading)
                try:
                    if stage_name in self.stages_to_run:
                        result = func(self, *args, **kwargs)
                    else:
                        self.skip_stage()
                        result = skip_result
                    self.end_stage()
                    return result
                except Exception as e:
                    self.end_stage()
                    raise e

            return wrapper

        return decorator

    def create_install_commands(self) -> list[str]:
        """
        Create the commands to install the testing instance.

        Returns:
            The command(s) to execute.
        """
        minio_command = (
            f"gmskube augment apply --tag {self.instance_tag} "
            f"--name {self.minio['augmentation_name']} "
            f"--set minioReportBucket={self.minio['report_bucket']} "
            f"--set minioAccessKey={self.minio['access_key']} "
            f"--set minioSecretKey={self.minio['secret_key']} "
            f"{self.instance_name}"
        )
        node_env = "development"
        if self.instance_type == "ian":
            return [
                f"ian-sim-deploy --tag-auto {self.instance_tag} "
                f"{self.instance_name} {node_env}",
                minio_command
            ]
        elif self.instance_type == "sb":
            return [
                f"gmskube install --tag {self.instance_tag} --type "
                f"{self.instance_type} {self.instance_name}",
                minio_command
            ]
        elif self.instance_type == "soh":
            return [
                f"gmskube install --tag {self.instance_tag} "
                f"--type {self.instance_type} --set "
                f"interactive-analysis-ui.env.NODE_ENV={node_env} "
                f"{self.instance_name}",
                minio_command
            ]

    @staticmethod
    def quote_arg(arg: str) -> str:
        """
        If a command line argument has any spaces in it, surround it in
        single quotes.  If no quotes are necessary, don't change the
        argument.

        Args:
            arg:  The command line argument.

        Returns:
            The (possibly) quoted argument.
        """
        needs_quotes_regex = re.compile(r"(.*\s.*)")
        if needs_quotes_regex.search(arg):
            return needs_quotes_regex.sub(r"'\1'", arg)
        else:
            return arg

    def pretty_print_command(self, command: str, indent: int = 4) -> str:
        """
        Take a command executed in the shell and pretty-print it by
        inserting newlines where appropriate.

        Args:
            command:  The input command.
            indent:  How many spaces to indent each subsequent line.

        Returns:
            The reformatted command.
        """
        args = shlex.split(command)
        lines = [args.pop(0)]
        while args:
            if args[0][:2] == "--":
                if len(args) > 1:
                    if args[1][:2] == "--":
                        lines.append(args.pop(0))
                    else:
                        lines.append(
                            f"{args.pop(0)} {self.quote_arg(args.pop(0))}"
                        )
                else:
                    lines.append(args.pop(0))
            else:
                lines.append(args.pop(0))
        return (" \\\n" + " "*indent).join(lines)

    def create_unique_instance_name(self) -> str:
        """
        Create a unique name for this instance of the system.

        Returns:
            The unique instance name, which is of the form
            ``gms-system-test-<random-string>``.
        """
        prefix = "gms-system-test"
        alphabet = string.ascii_lowercase + string.digits
        random_string = "".join(secrets.choice(alphabet) for _ in range(10))
        instance_name = f"{prefix}-{random_string}"
        self.console.log(
            f"`GMSSystemTest` will create an instance named `{instance_name}` "
            "for testing purposes."
        )
        return instance_name

    @stage("install", "Installing an instance for testing purposes...")
    def install_instance(self) -> None:
        """
        Stand up an instance of the system for the sake of running a
        test augmentation against it.
        """
        if self.instance_name is None:
            self.instance_name = self.create_unique_instance_name()
        self.kubectl = KubeCtl(self.instance_name)
        for command in self.create_install_commands():
            if self.dry_run:
                self.dry_run_message(
                    f"The command executed would be:  `{command}`"
                )
            else:
                self.console.log(f"Executing:  {command}")
                return_code = subprocess.run(command, shell=True).returncode
                self.commands_executed.append(
                    self.pretty_print_command(command)
                )
                if return_code != 0:
                    raise RuntimeError(
                        "Instance failed to install.  Error code:  "
                        f"{return_code}."
                    )

    def print_pod_results(self, message: str, color: str) -> None:
        """
        Display a summary of the pod results, along with an overall
        status message.

        Args:
            message:  The overall status message to display.
            color:  The color for the status message.
        """
        print()
        self.console.log(self.kubectl.get_pods())
        self.console.log(f"[{color}]{message}")

    @stage(
        "wait",
        "Checking to see if all pods are running...",
        skip_result=True
    )
    def check_all_pods_running_or_succeeded(self) -> bool:
        """
        Check to see if all the pods for the testing instance are either
        ``Running`` or ``Succeeded``, such that the system is ready for
        running a test augmentation against it.  This will wait a
        reasonable amount of time for the pods to come up, as it usually
        takes a little time after a ``gmskube install`` completes.

        Returns:
            Whether or not all the pods are ready for us to run testing.
        """
        if self.dry_run:
            self.dry_run_message("Skipping this step.")
            return True
        try:
            self.commands_executed.append(
                "# Wait for all pods to reach the 'Running' or "
                "'Succeeded' state."
            )
            self.kubectl.wait_for_all_pods_running_or_succeeded(
                timeout=self.retry_timeout,
                retry_delay=self.retry_delay,
                max_attempts=self.retry_attempts
            )
            self.print_pod_results("ALL PODS RUNNING!", "green")
            return True
        except RuntimeError as e:
            self.print_pod_results(str(e), "red")
            return False
        except RetryError:
            self.print_pod_results("NOT ALL PODS RUNNING!", "red")
            return False

    @stage("sleep", "Sleeping to allow the application to be ready...")
    def sleep_after_pods_running(self) -> None:
        """
        This is intended to be used after
        :func:`check_all_pods_running_or_succeeded` such that we can
        wait for the applications within each pod to be ready to process
        data.
        """
        if self.dry_run:
            self.dry_run_message("Skipping this step.")
        else:
            time_to_sleep = track(
                range(self.sleep),
                description=(" " * 11),
                console=self.console
            )
            for _ in time_to_sleep:
                sleep(1)
            self.commands_executed.append(f"sleep {self.sleep}")

    def ensure_instance_tag_set(self) -> None:
        """
        Ensure the :attr:`instance_tag` is set, as future stages will
        require it.

        Raises:
            RuntimeError:  If the tag isn't set and we're not able to
                determine it.
        """
        if self.instance_tag is None:
            if labels := self.kubectl.get_configmap_labels("gms"):
                self.instance_tag = labels["gms/image-tag"]
            else:
                raise RuntimeError(
                    "Unable to determine the instance tag, which is needed "
                    "for future stages."
                )

    def apply_test_augmentation(self) -> None:
        """
        Apply the test augmentation to the system.
        """
        command = (
            f"gmskube augment apply --tag {self.instance_tag} --name "
            f"{self.test_name} {' '.join(self.set_args)} "
            f"{self.instance_name}"
        )
        if self.dry_run:
            self.dry_run_message(
                f"The command executed would be:  `{command}`"
            )
            return
        self.console.log(f"Executing:  {command}")
        subprocess.run(command, shell=True)
        self.commands_executed.append(self.pretty_print_command(command))

    def get_minio_endpoint(self) -> str:
        """
        Determine the ingress endpoint for the ``minio-test-reports``
        augmentation.

        Raises:
            RuntimeError:  If it's not possible to determine the host,
                port, or path.

        Returns:
            The host, port, and path.
        """
        host, paths = self.kubectl.get_endpoints(
            self.minio["augmentation_name"]
        )
        if host is None or paths is None:
            raise RuntimeError(
                f"Failed to locate the `{self.minio['augmentation_name']}` "
                "endpoint."
            )
        ports = self.kubectl_gms.get_resource("configmap/ingress-ports-config")
        if not ports:
            raise RuntimeError(
                "Failed to get the port for the "
                f"`{self.minio['augmentation_name']}` endpoint."
            )
        ports = ports[0]["data"]
        path = paths[0]
        if self.kubectl.istio_enabled():
            port = ports["istio_port"]
        else:
            port = ports["nginx_port"]
        return f"{host}:{port}{path}"

    def get_minio_client(self) -> Minio:
        """
        Create a :class:`Minio` client for the ``minio-test-reports``
        augmentation.

        Raises:
            RuntimeError:  If the client can't be created, or if the
                report bucket can't be found.

        Returns:
            The MinIO client.
        """
        endpoint = self.get_minio_endpoint()
        client = Minio(
            endpoint,
            access_key=self.minio["access_key"],
            secret_key=self.minio["secret_key"]
        )
        if client is None:
            raise RuntimeError(
                f"Failed to connect to MinIO endpoint '{endpoint}'.  No "
                "results can be retrieved."
            )
        if not client.bucket_exists(self.minio["report_bucket"]):
            raise RuntimeError(
                f"Unable to locate the '{self.minio['report_bucket']}' "
                f"container in the MinIO endpoint '{endpoint}'.  No results "
                "can be retrieved."
            )
        return client

    def extract_minio_object(
        self,
        client: Minio,
        file_name: str,
        local_filepath: Path,
        results_dir: Path
    ) -> None:
        """
        Retrieve a zipped tar file from the Minio report bucket and
        unzip it to the results directory.

        Args:
            client:  The client for the ``minio-test-reports``
                augmentation.
            file_name:  The name of the file to retrieve.
            local_filepath:  Where to store it locally on disk.
            results_dir:  Where to extract the results to.
        """
        client.fget_object(
            self.minio["report_bucket"],
            file_name,
            str(local_filepath)
        )
        tar = tarfile.open(local_filepath)
        tar.extractall(results_dir)
        tar.close()
        local_filepath.unlink()

    def pod_succeeded(self, output_file: Path) -> bool:
        """
        Determine the success or failure of the test augmentation pod by
        parsing its output.

        Args:
            output_file:  The file to parse.

        Raises:
            RuntimeError:  If the appropriate result string cannot be
                found in the file.

        Returns:
            ``True`` if the pod succeeded; ``False`` if not.
        """
        with open(output_file) as f:
            output = f.read()
            if "TEST AUGMENTATION POD RESULT:  SUCCESS" in output:
                return True
            elif "TEST AUGMENTATION POD RESULT:  FAILURE" in output:
                return False
            else:
                raise RuntimeError(
                    "Didn't find 'TEST AUGMENTATION POD RESULT' in "
                    f"'{output_file}'."
                )

    def retrieve_test_results(self) -> bool:
        """
        Retrieve one or more compressed tar files containing test
        results from the MinIO test reporting service.

        Returns:
            Whether or not testing passed.
        """
        client = self.get_minio_client()
        results_dir = self.reports_dir / self.test_name
        results_dir.mkdir()
        results = list(client.list_objects(self.minio["report_bucket"]))
        num_results = 0
        pod_success = []
        for result in results:
            file_name = result.object_name
            if self.test_name not in file_name:
                continue
            num_results += 1
            local_filepath = results_dir / file_name
            self.extract_minio_object(
                client,
                file_name,
                local_filepath,
                results_dir
            )
            output_file = results_dir / local_filepath.stem / "testrun.txt"
            pod_success.append(self.pod_succeeded(output_file))
        if num_results < self.parallel:
            raise RuntimeError(
                f"Expecting {self.parallel} results objects in the "
                f"'{self.minio['report_bucket']}' MinIO bucket; only found "
                f"{num_results}."
            )
        return all(pod_success or [False])

    @stage("test", "Running the specified test...")
    def run_test(self) -> None:
        """
        Apply the test augmentation to the system, wait for it to
        complete, retrieve the test results, and save the logs.
        """
        start_time = datetime.now()
        self.apply_test_augmentation()
        if self.dry_run:
            return
        if self.kubectl.resource_failed_to_start(f"jobs/{self.test_name}"):
            raise RuntimeError(
                f"'{self.test_name}' failed to start; aborting."
            )
        self.kubectl.wait(f"jobs/{self.test_name}", timeout=self.timeout)
        duration = datetime.now() - start_time
        self.console.log(f"'{self.test_name}' duration:  {str(duration)}")
        self.console.log(f"Collecting results from '{self.test_name}'")
        self.test_success = self.retrieve_test_results()
        self.console.log(
            f"Grabbing the '{self.test_name}' augmentation pod logs."
        )
        self.kubectl.save_resource_logs(self.test_name, "job", self.log_dir)
        if self.test_success:
            self.console.log(f"[bold green]{self.test_name} PASSED")
        else:
            self.console.log(f"[bold red]{self.test_name} FAILED")

    @stage(
        "uninstall",
        "Uninstalling the instance now that testing is complete..."
    )
    def uninstall_instance(self) -> None:
        """
        Now that testing is complete, tear down the instance of the
        system.
        """
        command = f"gmskube uninstall {self.instance_name}"
        if self.dry_run:
            self.dry_run_message(
                f"The command executed would be:  `{command}`"
            )
        else:
            self.console.log(f"Executing:  {command}")
            subprocess.run(command, shell=True)
            self.commands_executed.append(self.pretty_print_command(command))

    def get_timing_report(self) -> str:  # sourcery skip: use-join
        """
        Create a report of the durations of all the stages.

        Returns:
            The report.
        """
        total_duration = datetime.now() - self.start_time
        total_label = "Total"
        stage_width = max([len(stage)
                           for stage in self.durations] + [len(total_label)])
        report = ""
        for stage, duration in self.durations.items():
            report += (
                f"{stage}:  " + " " *
                (stage_width - len(stage)) + str(duration) + "\n"
            )
        report += (
            f"{total_label}:  " + " " *
            (stage_width - len(total_label)) + str(total_duration)
        )
        return report

    def print_script_execution_summary(self) -> None:
        """
        Print a summary of everything that was done by the script.
        """
        unparser = ReverseArgumentParser(self.parser(), self.args)
        sections = {
            "Ran the following":
            unparser.get_pretty_command_line_invocation(indent=2),
            "Commands executed":
            "\n".join(self.commands_executed),
            "Timing results":
            self.get_timing_report(),
            "Test reports":
            str(self.reports_dir)
        }
        width = 79
        summary = "[ Script Execution Summary ]".center(width, "-")
        summary = f"\n{summary}\n"
        for section, details in sections.items():
            summary += f"\n+ {section}:\n"
            summary += "\n" + textwrap.indent(details, "    ") + "\n"
        summary += "\n" + "[ End Script Execution Summary ]".center(width, "-")
        self.console.log(summary)

    def keyboard_interrupt_handler(self, signal_number, stack_frame):
        """
        Clean-up operations for when the user hits Ctrl+C in the midst
        of a run.

        Args:
            signal_number:  Not used.
            stack_frame:  Not used.

        Raises:
            SystemExit:  To indicate that the script completed with an
                error.
        """
        self.console.log(
            "[yellow]Caught a keyboard interrupt signal.  Attempting to tear "
            "down the testing instance so we don't leave it lying around.  If "
            "you hit CTRL+C again, you'll need to manually delete this "
            "instance."
        )
        if self.kubectl is not None:
            self.kubectl.save_logs(self.log_dir)
        self.uninstall_instance()
        self.print_script_execution_summary()
        raise SystemExit(1)

    def get_test_result(self) -> bool:
        """
        Determine the overall result of testing, and print a message to
        that effect.

        Returns:
            Whether testing completed successfully.
        """
        if self.instance_ready:
            if self.test_success:
                self.heading(
                    "Testing completed successfully" +
                    (" (in dry-run mode)" if self.dry_run else "") + ".",
                    color="green"
                )
                return True
            else:
                self.heading("Testing failed.", color="red")
                return False
        else:
            self.heading("Testing was unable to run.", color="yellow")
            return False

    def uninstall_on_exception(self, e: Exception) -> None:
        """
        In the event of an exception, try not to leave the testing
        instance lying around.

        Args:
            e:  The exception that was caught.

        Raises:
            e:  If the ``uninstall`` stage was not to be run, the same
                exception is re-raised.
        """
        if "uninstall" not in self.stages_to_run:
            raise e
        msg = textwrap.fill(
            "The following exception occurred, so we're going to attempt "
            "to tear down the testing instance so we don't leave it "
            "lying around.",
            width=79
        )
        msg += "\n" + "-"*79 + "\n" + str(e) + "\n" + "-"*79
        self.console.log(f"[bold yellow]{msg}")
        self.console.print_exception()
        if self.kubectl is not None:
            self.kubectl.save_logs(self.log_dir)
        self.uninstall_instance()

    def parser(self) -> ArgumentParser:
        """
        Create an ``ArgumentParser`` that contains all the necessary
        arguments for this script.

        Note:
            This exists in its own function separate from :func:`main`
            because the ``sphinx-argparse`` extension requires the
            parser to be returned from a function without running the
            :func:`parse_args` function on the parser.

        Returns:
            The argument parser for this script.

        Todo:
            * This ``ArgumentParser`` contains pieces from
              ``gmskube.py``.  The common arguments should be pulled out
              into a common location and then included in each file.
            * The list of supported types comes from ``gmskube.py``.
              Need to deal with the code duplication.
        """
        description = """
Description
===========

This script:
* stands up a temporary instance of the GMS system,
* waits for all the pods to be up and running,
* sleeps a given amount of time to wait for the application to be ready,
* runs a test augmentation against it, and
* tears down the temporary instance after testing completes.

The test must be specified by a GMS augmentation with a type of
``test``.  These augmentations are built into the ``gmskube`` container.
Running ``gmskube augment catalog --tag <tag>`` will show a list of all
available augmentations.  Which test is run can be specified via
``--test``.

Results
=======

Test augmentations copy their test results to a MinIO test reporting
service so that they can be gathered back to the machine on which this
script was executed.  Final reports will be gathered in a
``system-test-reports-{timestamp}-{unique-str}`` directory under the
current working directory.  Under this top-level directory there will be
(1) reports for the test itself, and (2) logs from all the containers
run as part of the testing in a ``container-logs`` directory.

Environment Variables
=====================

Additional environment variables can be provided to tests from the
command line via the ``--env`` argument.  Each ``env`` argument should
specify a value of the form ``variable=value``.

Examples
========

Run the ``test-sb-jest`` test against a ``sb`` instance deployed from
the ``develop`` branch::

    gms_system_test.py --type sb --tag develop --test test-sb-jest
"""
        ap = ArgumentParser(
            description=description,
            formatter_class=RawDescriptionHelpFormatter
        )
        stages = ["install", "wait", "sleep", "test", "uninstall"]
        ap.add_argument(
            "--stage",
            choices=stages,
            nargs="+",
            default=stages,
            help="Which stages to run.  Defaults to running all of them."
        )
        ap.add_argument(
            "--tag",
            default=None,
            type=self.argparse_tag_name_type,
            help="The tag name, which corresponds to the Docker tag of the "
            "images.  The value entered will automatically be transformed "
            "according to the definition of the GitLab ``CI_COMMIT_REF_SLUG`` "
            "variable definition (lowercase, shortened to 63 characters, and "
            "with everything except ``0-9`` and ``a-z`` replaced with ``-``, "
            "no leading / trailing ``-``)."
        )
        ap.add_argument(
            "--type",
            default=None,
            choices=["ian",
                     "sb",
                     "soh"],
            help="The type of instance to stand up."
        )
        ap.add_argument(
            "--test",
            help="The name of a test to run (see ``gmskube augment catalog "
            "--tag <reference>``)."
        )
        ap.add_argument(
            "--env",
            type=self.argparse_set_env,
            action="append",
            help="Set environment variables in the test environment.  This "
            "argument can be specified multiple times to specify multiple "
            "values.  Examples:  ``--env FOO=bar`` will set ``FOO=bar`` for "
            "the test."
        )
        retry_group = ap.add_argument_group(
            "Retry options when waiting for the pods to spin up"
        )
        retry_group.add_argument(
            "--retry-timeout",
            default=60,
            type=int,
            help="How long to wait (in seconds) for all the pods in the "
            "temporary instance to spin up."
        )
        retry_group.add_argument(
            "--retry-delay",
            default=0,
            type=float,
            help="How long to wait between retries when checking if all the "
            "pods are up."
        )
        retry_group.add_argument(
            "--retry-attempts",
            default=None,
            type=int,
            help="How many times to check to see if all the pods are "
            "running.  Omitting this option means don't restrict the number "
            "of attempts."
        )
        ap.add_argument(
            "--sleep",
            default=0,
            type=int,
            help="How long to wait between the pods reaching a 'Running' "
            "state and starting the test."
        )
        ap.add_argument(
            "--dry-run",
            action="store_true",
            help="If specified, don't actually stand up an instance, test "
            "against it, and tear it down; instead print the commands that "
            "would have been executed to do so."
        )
        ap.add_argument(
            "--instance",
            default=None,
            help="If specified, use the given instance name rather than "
            "automatically generating one."
        )
        ap.add_argument(
            "--timeout",
            default=1200,
            type=int,
            help="How long (in seconds) to wait for a test augmentation to "
            "complete."
        )
        ap.add_argument(
            "--parallel",
            default=1,
            type=int,
            choices=range(1, 11),
            help="How many identical test augmentation pods to launch in "
            "parallel."
        )  # yapf: disable
        return ap

    @staticmethod
    def argparse_tag_name_type(s: str) -> str:
        """
        Transform the tag name into the ``CI_COMMIT_REF_SLUG`` as
        defined by GitLab:  lower-cased, shortened to 63 bytes, and with
        everything except ``0-9`` and ``a-z`` replaced with ``-``.  No
        leading/trailing ``-``.

        Todo:
            * This function is a copy of the one in ``gmskube.py``. It
              should be pulled out into a common location and then
              included in each file.
        """
        # `s.lower()` changes to lower case.
        # `re.sub` replaces anything other than `a-z` or `0-9` with `-`.
        # `strip('-')` removes any leading or trailing `-` after `re.sub`.
        # `[:63]` truncates to 63 characters.
        return re.sub(r'[^a-z0-9]', '-', s.lower()).strip('-')[:63]

    @staticmethod
    def argparse_set_env(s: str) -> str:
        """
        Ensure the input is of the form ``VARIABLE=VALUE``.

        Args:
            s:  The input string.

        Raises:
            ArgumentTypeError:  If the string doesn't match the pattern.

        Returns:
            The input string.
        """
        if s and "=" not in s:
            raise ArgumentTypeError(
                "When specifying `--env`, you must supply the name/value pair "
                "as `Name=Value`."
            )
        return s

    def raise_parser_error(self, parser, message):
        """
        Exit the script with a message indicating what went wrong when
        parsing the command line arguments.

        Args:
            parser:  The argument parser.
            message:  What went wrong when parsing the arguments.

        Raises:
            SystemExit:  To indicate the problem and stop script
                execution.
        """
        parser.print_help()
        msg = textwrap.fill(message, width=79)
        self.console.log(f"[yellow]\n{msg}")
        raise SystemExit(1)

    def create_set_args(
        self,
        env_args: list[str] | None
    ) -> list[str]:  # yapf: disable
        """
        Create a list of ``--set`` arguments to pass to ``gmskube`` when
        applying the test augmentation.  This translates any ``--env``
        arguments passed to this script into the appropriate
        ``--set env.foo=bar`` form, and also sets the number of
        identical pods to launch if ``parallel`` is greater than 1.

        Args:
            env:  The (potentially empty or nonexistent) list of all the
                environment variables to be set for the test.

        Returns:
            The list of ``--set`` arguments.
        """
        set_args = defaultdict(list)
        for item in env_args or []:
            name, value = item.split("=", 1)
            if "." in name:
                test_name, name = name.split(".", 1)
            else:
                test_name = "global"
            set_args[test_name].append(f'--set env.{name}="{value}"')
        result = set_args["global"] + set_args[self.test_name]
        if self.parallel > 1:
            result.append(f"--set numIdenticalPods={self.parallel}")
        return result

    def parse_args(self, argv: list[str]) -> None:
        """
        Parse the command line arguments, and handle any special cases.

        Args:
            argv:  The command line arguments used when running this
                file as a script.

        Raises:
            SystemExit:  If the user doesn't specify a test to run, or
                if they don't provide the right combination of flags.
        """
        arg_parser = self.parser()
        args = arg_parser.parse_args(argv)
        if args.env == [""]:
            args.env = []
        if "test" in args.stage and not args.test:
            self.raise_parser_error(
                arg_parser,
                "You must specify which test to run via the `--test` flag.  "
                "Run `gmskube augment catalog --tag <reference>` to see the "
                "available tests."
            )
        if "install" in args.stage and (args.tag is None or args.type is None):
            self.raise_parser_error(
                arg_parser,
                "You must either specify (1) both `--tag` and `--type` to "
                "stand up a temporary instance, or (2) `--instance <name>` "
                "and omit `install` from the `--stage`s to run to test "
                "against an existing one."
            )
        self.args = args
        self.dry_run = args.dry_run
        self.instance_name = args.instance
        self.instance_tag = args.tag
        self.instance_type = args.type
        self.parallel = args.parallel
        self.retry_attempts = args.retry_attempts
        self.retry_delay = args.retry_delay
        self.retry_timeout = args.retry_timeout
        self.set_args = self.create_set_args(args.env)
        self.sleep = args.sleep
        self.stages_to_run = args.stage
        self.test_name = args.test
        self.timeout = args.timeout

    def main(self, argv: list[str]) -> None:
        """
        This method handles
        * standing up an instance of the GMS system,
        * waiting for all the pods to come up,
        * waiting for the application to be ready,
        * running a test augmentation against the system, and
        * tearing down the instance.

        Args:
            argv:  The command line arguments used when running this
                file as a script.

        Raises:
            RuntimeError:  If the user forgot to pass in the instance
                name.
            SystemExit(0):  If everything passes successfully.
            SystemExit(1):  If any test fails, or if anything else goes
                wrong.
        """
        self.parse_args(argv)
        signal(SIGINT, self.keyboard_interrupt_handler)
        script_success = True
        self.ensure_commands_are_available()
        self.check_kubeconfig()
        self.create_unique_reports_directory()
        try:
            self.install_instance()
            if self.instance_name is None:
                raise RuntimeError(
                    "The instance name is necessary for future stages.  "
                    "Specify `--instance` on the command line."
                )
            if self.kubectl is None:
                self.kubectl = KubeCtl(self.instance_name)
            self.instance_ready = self.check_all_pods_running_or_succeeded()
            if self.instance_ready:
                self.sleep_after_pods_running()
                self.ensure_instance_tag_set()
                self.run_test()
            self.kubectl.save_logs(self.log_dir)
            self.uninstall_instance()
            if "test" in self.stages_to_run:
                script_success = self.get_test_result()
        except Exception as e:
            self.uninstall_on_exception(e)
            script_success = False
        self.print_script_execution_summary()
        if not script_success:
            raise SystemExit(1)


if __name__ == "__main__":
    gst = GMSSystemTest()
    gst.main(sys.argv[1:])
