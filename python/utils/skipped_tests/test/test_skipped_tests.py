#!/usr/bin/env python3

from _pytest.capture import CaptureFixture
from pathlib import Path
import pytest
from python.utils.skipped_tests.skipped_tests.skipped_tests import SkippedTests


@pytest.fixture
def skipped_tests() -> SkippedTests:
    test_type = "Test type"
    test_root = Path(__file__).resolve().parent / "test_root"
    test_glob = "*.test.glob"
    return SkippedTests(test_type, test_root, test_glob)


def test_init(skipped_tests: SkippedTests) -> None:
    assert skipped_tests.test_type == "Test type"
    assert skipped_tests.test_root.name == "test_root"
    assert skipped_tests.test_glob == "*.test.glob"
    assert skipped_tests.test_catalogue == {}


def test_examine_tests(skipped_tests: SkippedTests) -> None:
    skipped_tests.test_root.mkdir(exist_ok=True)
    dummy_file = skipped_tests.test_root / "dummy.test.glob"
    dummy_file.touch()
    with pytest.raises(NotImplementedError):
        skipped_tests.examine_tests()
    dummy_file.unlink()
    skipped_tests.test_root.rmdir()


def test_get_tests_from_file(
        skipped_tests: SkippedTests,
        tmp_path: Path
) -> None:
    tmp_file = tmp_path / "tmp_file.txt"
    tmp_file.touch()
    with open(tmp_file, "r") as f:
        with pytest.raises(NotImplementedError) as exc:
            skipped_tests.get_tests_from_file(f)
    assert "must implement `get_tests_from_file()`" in exc.value.args[0]


@pytest.mark.parametrize("suite, result", [
    ({"skipped": True}, True),
    ({"skipped": False, "tests": [{"skipped": True}]}, True),
    ({"skipped": False, "tests": [{"skipped": False}]}, False)
], ids=["suite_skipped", "tests_skipped", "none_skipped"])
def test_any_tests_skipped(
        skipped_tests: SkippedTests,
        suite: dict,
        result: bool
) -> None:
    skipped_tests.test_catalogue = {"dummy_file": [suite]}
    assert skipped_tests.any_tests_skipped() == result


@pytest.mark.parametrize("suite, result", [
    ({"skipped": True, "tests": [{}, {}]}, 2),
    ({"skipped": False, "tests": [{"skipped": True}, {"skipped": False}]}, 1),
    ({"skipped": False, "tests": [{"skipped": False}, {"skipped": False}]}, 0)
], ids=["suite_skipped", "tests_skipped", "none_skipped"])
def test_get_num_skipped_in_suite(
        skipped_tests: SkippedTests,
        suite: dict,
        result: bool
) -> None:
    assert skipped_tests.get_num_skipped_in_suite(suite) == result


@pytest.mark.parametrize("name, reason", [
    ("test 1", "skip reason"),
    ("test 2", "")
], ids=["skip_reason", "no_reason"])
def test_print_test_name(
        skipped_tests: SkippedTests,
        capsys: CaptureFixture,
        name: str,
        reason: str
) -> None:
    skipped_tests.print_test_name(name, reason)
    out, _ = capsys.readouterr()
    assert name in out
    if reason != "":
        assert reason in out


@pytest.mark.parametrize("suite", [
    {"suite name": "Suite A",
     "skipped": True,
     "skip reason": "",
     "tests": [{"test name": "Test A1"}, {"test name": "Test A2"}]},
    {"suite name": "Suite B",
     "skipped": False,
     "tests": [{"test name": "Test B1", "skipped": True, "skip reason": ""},
               {"test name": "Test B2", "skipped": False}]}
], ids=["suite_skipped", "tests_skipped"])
def test_print_test_suite_report(
        skipped_tests: SkippedTests,
        capsys: CaptureFixture,
        suite: dict
) -> None:
    test_file = skipped_tests.test_root / "path/to/dummy_file.txt"
    skipped_tests.print_test_suite_report(test_file, suite)
    out, _ = capsys.readouterr()
    assert str(test_file.name) in out
    assert suite["suite name"] in out
    for test in suite["tests"]:
        if suite["skipped"] or test["skipped"]:
            assert test["test name"] in out


def test_print_skipped_test_report(
        skipped_tests: SkippedTests,
        capsys: CaptureFixture,
) -> None:
    skipped_tests.test_catalogue = {
        skipped_tests.test_root / "dummy_file.txt": [
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
                        "skip reason": ""}]},
        ]
    }
    num_skipped = 3
    total_tests = 4
    skipped_tests.print_skipped_test_report()
    out, _ = capsys.readouterr()
    assert skipped_tests.test_type.title() in out
    assert str(skipped_tests.test_root.name) in out
    assert f"{num_skipped}/{total_tests}" in out
