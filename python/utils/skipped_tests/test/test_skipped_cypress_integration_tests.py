#!/usr/bin/env python3

from _pytest.capture import CaptureFixture
from pathlib import Path
import pytest
from python.utils.skipped_tests.skipped_tests.\
    skipped_cypress_integration_tests import SkippedCypressIntegrationTests


@pytest.fixture
def skipped_tests() -> SkippedCypressIntegrationTests:
    test_root = Path(__file__).resolve().parent / "test_root"
    return SkippedCypressIntegrationTests(test_root)


@pytest.fixture
def sample_file(skipped_tests: SkippedCypressIntegrationTests) -> Path:
    contents = """
    @skip:wip
    Feature: Suite A
      Scenario: Test A1
      Scenario: Test A2
    Feature: Suite B
      @skip:failing
      Scenario: Test B1
      Scenario: Test B2
    """
    skipped_tests.test_root.mkdir()
    file_path = skipped_tests.test_root / "dummy.feature"
    with open(file_path, "w") as f:
        f.write(contents)
    yield file_path
    file_path.unlink()
    skipped_tests.test_root.rmdir()


def test_init(skipped_tests: SkippedCypressIntegrationTests) -> None:
    assert skipped_tests.test_type == "Cypress integration"
    assert skipped_tests.test_root.name == "test_root"
    assert skipped_tests.test_glob == "*.feature"
    assert skipped_tests.test_catalogue == {}


def test_examine_tests(
        skipped_tests: SkippedCypressIntegrationTests,
        sample_file: Path,
        capsys: CaptureFixture
) -> None:
    print(f"Dummy test file:  {sample_file}")
    skipped_tests.examine_tests()
    out, _ = capsys.readouterr()
    assert """
# Skipped Cypress Integration Tests

**Test directory:**  `python/utils/skipped_tests/test/test_root`
* `dummy.feature`
  * Suite A
    * Test A1 (wip)
    * Test A2 (wip)
* `dummy.feature`
  * Suite B
    * Test B1 (failing)

**Total tests skipped:**  3/4 (75%)""" in out


def test_get_tests_from_file(
        skipped_tests: SkippedCypressIntegrationTests,
        sample_file: Path
) -> None:
    with open(sample_file, "r") as f:
        result = skipped_tests.get_tests_from_file(f)
    assert result == [
        {"suite name": "Suite A",
         "skipped": True,
         "skip reason": "wip",
         "tests": [{"test name": "Test A1",
                    "skipped": False,
                    "skip reason": ""},
                   {"test name": "Test A2",
                    "skipped": False,
                    "skip reason": ""}]},
        {"suite name": "Suite B",
         "skipped": False,
         "skip reason": "",
         "tests": [{"test name": "Test B1",
                    "skipped": True,
                    "skip reason": "failing"},
                   {"test name": "Test B2",
                    "skipped": False,
                    "skip reason": ""}]}
    ]
