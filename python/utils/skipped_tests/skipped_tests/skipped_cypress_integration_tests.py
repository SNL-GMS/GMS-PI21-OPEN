#!/usr/bin/env python3

from pathlib import Path
from .skipped_tests import SkippedTests
import re
from typing import IO


class SkippedCypressIntegrationTests(SkippedTests):
    """
    An extension to :class:`SkippedTests` specific to Cypress
    integration tests.
    """

    def __init__(self, test_root: Path):
        super().__init__(test_type="Cypress integration", test_root=test_root,
                         test_glob="*.feature")

    def get_tests_from_file(self, file: IO) -> list[dict]:
        """
        Parse a Cypress integration test file and determine:

        * What test suites (``Feature`` blocks) are in the file.
        * What tests (``Scenario`` blocks) correspond to each test
          suite.
        * If any individual tests or test suites are skipped.

        Args:
            file:  An open file stream.

        Returns:
            A list of dictionaries, where each entry corresponds to a
            test suite.
        """
        regex = r"^\s*(WORD:\s*)(.+)$"
        result = []
        prior_line = ""
        skip_flag = "@skip"
        for line in file:
            suite_match = re.search(regex.replace("WORD", "Feature"), line)
            if suite_match:
                result.append({
                    "suite name": suite_match.group(2),
                    "tests": [],
                    "skipped": skip_flag in prior_line,
                    "skip reason": (re.search(fr"{skip_flag}:(\S+)",
                                              prior_line).group(1)
                                    if skip_flag in prior_line else "")
                })
            test_match = re.search(regex.replace("WORD", "Scenario"), line)
            if test_match:
                result[-1]["tests"].append({
                    "test name": test_match.group(2),
                    "skipped": skip_flag in prior_line,
                    "skip reason": (re.search(fr"{skip_flag}:(\S+)",
                                              prior_line).group(1)
                                    if skip_flag in prior_line else "")
                })
            prior_line = line
        return result
