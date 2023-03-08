#!/usr/bin/env python3

from _pytest.capture import CaptureFixture
from pathlib import Path
import pytest
from python.utils.skipped_tests.skipped_tests.skipped_jest_integration_tests \
    import SkippedJestIntegrationTests


@pytest.fixture
def skipped_tests() -> SkippedJestIntegrationTests:
    test_root = Path(__file__).resolve().parent / "test_root"
    return SkippedJestIntegrationTests(test_root)


@pytest.fixture
def sample_file(skipped_tests: SkippedJestIntegrationTests) -> Path:
    contents = """
    describe.skip('Suite A', () => {
      it('Test A1', () => {
      it('Test A2', () => {
    describe('Suite B', () => {
      it.skip('Test B1', () => {
      it('Test B2', () => {
    """
    skipped_tests.test_root.mkdir()
    file_path = skipped_tests.test_root / "dummy.test.ts"
    with open(file_path, "w") as f:
        f.write(contents)
    yield file_path
    file_path.unlink()
    skipped_tests.test_root.rmdir()


def test_init(skipped_tests: SkippedJestIntegrationTests) -> None:
    assert skipped_tests.test_type == "Jest integration"
    assert skipped_tests.test_root.name == "test_root"
    assert skipped_tests.test_glob == "*.test.ts"
    assert skipped_tests.test_catalogue == {}


def test_examine_tests(
        skipped_tests: SkippedJestIntegrationTests,
        sample_file: Path,
        capsys: CaptureFixture
) -> None:
    print(f"Dummy test file:  {sample_file}")
    skipped_tests.examine_tests()
    out, _ = capsys.readouterr()
    assert """
# Skipped Jest Integration Tests

**Test directory:**  `python/utils/skipped_tests/test/test_root`
* `dummy.test.ts`
  * Suite A
    * Test A1
    * Test A2
* `dummy.test.ts`
  * Suite B
    * Test B1""" in out


def test_get_tests_from_file(
        skipped_tests: SkippedJestIntegrationTests,
        sample_file: Path
) -> None:
    with open(sample_file, "r") as f:
        result = skipped_tests.get_tests_from_file(f)
    assert result == [
        {"suite name": "Suite A",
         "skipped": True,
         "skip reason": "",
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
                    "skip reason": ""},
                   {"test name": "Test B2",
                    "skipped": False,
                    "skip reason": ""}]}
    ]
