#!/usr/bin/env python3

from pathlib import Path
from .skipped_tests import SkippedTests
import re
from typing import IO


class SkippedJestIntegrationTests(SkippedTests):
    """
    An extension to :class:`SkippedTests` specific to Jest integration
    tests.
    """

    def __init__(self, test_root: Path):
        super().__init__(test_type="Jest integration", test_root=test_root,
                         test_glob="*.test.ts")

    def get_tests_from_file(self, file: IO) -> list[dict]:
        """
        Parse a Jest integration test file and determine:

        * What test suites (``describe`` blocks) are in the file.
        * What tests (``it`` blocks) correspond to each test suite.
        * If any individual tests or test suites are skipped.

        Args:
            file:  An open file stream.

        Returns:
            A list of dictionaries, where each entry corresponds to a
            test suite.
        """
        regex = r"^\s*(WORD\.?\w*\()'(.+)',"
        result = []
        for line in file:
            suite_match = re.search(regex.replace("WORD", "describe"), line)
            if suite_match:
                result.append({
                    "suite name": suite_match.group(2),
                    "tests": [],
                    "skipped": bool(re.search(
                        regex.replace("WORD", "describe.skip"),
                        line
                    )),
                    "skip reason": ""
                })
            test_match = re.search(regex.replace("WORD", "it"), line)
            if test_match:
                result[-1]["tests"].append({
                    "test name": test_match.group(2),
                    "skipped": bool(re.search(
                        regex.replace("WORD", "it.skip"),
                        line
                    )),
                    "skip reason": ""
                })
        return result
