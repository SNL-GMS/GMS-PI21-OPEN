#!/usr/bin/env python3

from abc import abstractmethod
from pathlib import Path
from typing import IO


class SkippedTests:
    """
    An abstract base class to examine a directory tree containing files
    pertaining to a certain type of test and produce a report of which
    tests are skipped.

    Note:
        The motivation is to ensure you don't skip a test and then
        forget about it.  Using :class:`SkippedTests` (or rather one of
        its extensions) regularly (e.g., via ``cron`` or something
        similar) helps you stay on top of your skipped tests, such that
        you can make sure your team is prioritizing getting them either
        fixed or removed.

    Attributes:
        test_type (str):  What kind of tests these are.
        test_root (Path):  The root directory containing all the tests
            that will be examined.
        test_glob (str):  What to glob for in the ``test_root`` to find
            all the test files.
        test_catalogue (dict):  A mapping from files to lists of test
            suites.  The structure of a test suite is the following::

                {
                    "suite name": str,
                    "skipped": bool,
                    "skip reason": str,
                    "tests": [
                        {
                            "test name": str,
                            "skipped": bool,
                            "skip reason": str
                        },
                        ...
                    ]
                }
    """

    def __init__(self, test_type: str, test_root: Path, test_glob: str):
        self.test_type = test_type
        self.test_root = test_root
        self.test_glob = test_glob
        self.test_catalogue = {}

    def examine_tests(self) -> None:
        """
        Examine all the tests in the ``test_root`` and generate a report
        of all the skipped tests.
        """
        test_files = self.test_root.rglob(self.test_glob)
        for test_file in test_files:
            with open(test_file, "r") as f:
                self.test_catalogue[test_file] = self.get_tests_from_file(f)
        if self.any_tests_skipped():
            self.print_skipped_test_report()
        else:
            print(f"No {self.test_type} tests are skipped at this time.")

    @abstractmethod
    def get_tests_from_file(self, file: IO) -> list[dict]:
        """
        Parse a test file and determine:

        * What test suites are in the file.
        * What tests correspond to each test suite.
        * If any individual tests or test suites are skipped.

        Args:
            file:  An open file stream.

        Returns:
            A list of dictionaries, where each entry corresponds to a
            test suite (see the :class:`SkippedTest` definition for the
            structure).
        """
        raise NotImplementedError("This class must implement "
                                  "`get_tests_from_file()`.")

    def any_tests_skipped(self) -> bool:
        """
        Check to see if any tests were skipped.

        Returns:
            ``True`` if any tests in the catalogue were skipped;
            ``False`` otherwise.
        """
        for file, tests in self.test_catalogue.items():
            if (any(suite["skipped"] for suite in tests) or
                    any(test["skipped"] for suite in tests for test in
                        suite["tests"])):
                return True
        return False

    @staticmethod
    def get_num_skipped_in_suite(suite: dict) -> int:
        """
        Determine the number of tests in the suite that are skipped.

        Args:
            suite:  The information about the test suite (see the
                :class:`SkippedTests` definition for the structure).

        Returns:
            The number of tests in the suite that are skipped.
        """
        return (len(suite["tests"]) if suite["skipped"] else
                sum(test["skipped"] for test in suite["tests"]))

    @staticmethod
    def print_test_name(name: str, skip_reason: str) -> None:
        """
        Print a line of the test suite report that contains the test
        name and optionally the reason it was skipped.

        Args:
            name:  The name of the test.
            skip_reason:  Why it was skipped.
        """
        print(f"    * {name}", end="")
        if skip_reason != "":
            print(f" ({skip_reason})")
        else:
            print()

    def print_test_suite_report(self, file: Path, suite: dict) -> None:
        """
        Print a report of the tests skipped in the given suite.

        Args:
            file:  The file in which the test suite resides.
            suite:  The information about the test suite (see the
                :class:`SkippedTests` definition for the structure).
        """
        print(f"* `{file.relative_to(self.test_root)}`")
        print(f"  * {suite['suite name']}")
        if suite["skipped"]:
            for test in suite["tests"]:
                self.print_test_name(test["test name"], suite["skip reason"])
        elif any(test["skipped"] for test in suite["tests"]):
            for test in suite["tests"]:
                if test["skipped"]:
                    self.print_test_name(test["test name"],
                                         test["skip reason"])

    def print_skipped_test_report(self) -> None:
        """
        Print a report of all the tests that are currently skipped.
        """
        print(f"\n# Skipped {self.test_type.title()} Tests\n")
        test_root = self.test_root.relative_to(Path(__file__).parents[4])
        print(f"**Test directory:**  `{test_root}`")
        skip_count = test_count = 0
        for file, tests in self.test_catalogue.items():
            for suite in tests:
                test_count += len(suite["tests"])
                if suite["skipped"] or any(test["skipped"] for test in
                                           suite["tests"]):
                    self.print_test_suite_report(file, suite)
                    skip_count += self.get_num_skipped_in_suite(suite)
        print(f"\n**Total tests skipped:**  {skip_count}/{test_count} "
              f"({skip_count/test_count*100:.0f}%)")
