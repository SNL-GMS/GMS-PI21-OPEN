# --------------------------------------------------------------------
#  test_earth_model_utils - tests for earth model JSON serialization
# --------------------------------------------------------------------
import filecmp
import io
import json
import os.path
import shutil
import subprocess
import sys
import tempfile
import unittest

from contextlib import redirect_stdout
from pathlib import Path
from python.utils.gmsearthmodel.bin.earth_model_utils import (
    parse_float_list,
    parse_int_list,
    logical_xor,
    get_line,
    expand_units_abbreviation
)

# Note that some of the following tests are performed through an operating system call
# rather than testing methods directly.  Consequently, the reported coverage is inaccurate.
# At some point, it would be good to look into emulating stdin so that the script does not
# need to be called indirectly.
# TODO - fix this so that tests are not called indirectly

class TestEarthModelUtils(unittest.TestCase):

    def test_parse_float_list(self):

        input_str = '1.0 2.0 3.0'
        expected = [1.0, 2.0, 3.0]
        sys.stdin = io.StringIO(input_str)
        result = parse_float_list()
        self.assertEqual(result, expected)

        input_str = '1.0 2.0 3.0   \n   4.0 5.0 6.0'
        expected = [1.0, 2.0, 3.0]
        sys.stdin = io.StringIO(input_str)
        result = parse_float_list()
        self.assertEqual(result, expected)

        input_str = '1.0 2.0 3.0'
        expected = [1.0, 2.0, 3.0]
        sys.stdin = io.StringIO(input_str)
        result = parse_float_list(num_floats=3)
        self.assertEqual(result, expected)

        input_str = '1.0 2.0 3.0   \n   4.0 5.0 6.0'
        expected = [1.0, 2.0, 3.0, 4.0, 5.0, 6.0]
        sys.stdin = io.StringIO(input_str)
        result = parse_float_list(num_floats=6)
        self.assertEqual(result, expected)

        input_str = '1.0 2.0 3.0   \n   4.0 5.0 6.0'
        sys.stdin = io.StringIO(input_str)
        with self.assertRaises(EOFError):
            parse_float_list(num_floats=100)

        input_str = ''
        sys.stdin = io.StringIO(input_str)
        with self.assertRaises(EOFError):
            parse_float_list()

        input_str = '1.0 2.0 3.0   \n   4.0 5.0 6.0'
        expected = [1.0]
        sys.stdin = io.StringIO(input_str)
        result = parse_float_list(num_floats=1)
        self.assertEqual(result, expected)

        input_str = '# Travel time at depth =   10.00 km.\n  1.0 2.0 3.0   \n   4.0 5.0 6.0'
        expected = [1.0, 2.0, 3.0, 4.0, 5.0, 6.0]
        sys.stdin = io.StringIO(input_str)
        result = parse_float_list(num_floats=6, expected_depth=10.00, expected_units='kilometers')
        self.assertEqual(result, expected)

        input_str = '# stuff and nonsense 5 s\n  1.0 2.0 3.0   \n   4.0 5.0 6.0'
        expected = [1.0, 2.0, 3.0, 4.0, 5.0, 6.0]
        sys.stdin = io.StringIO(input_str)
        result = parse_float_list(num_floats=6, expected_depth=5.0, expected_units='seconds')
        self.assertEqual(result, expected)

        input_str = '# Travel time at depth =   10.00 km.\n  1.0 2.0 3.0   \n   4.0 5.0 6.0'
        sys.stdin = io.StringIO(input_str)
        devnull = io.StringIO()
        with redirect_stdout(devnull):
            with self.assertRaises(SystemExit):
                parse_float_list(num_floats=6, expected_depth=10.00)

        input_str = '# Travel time at depth =   10.00 km.\n  1.0 2.0 3.0   \n   4.0 5.0 6.0'
        sys.stdin = io.StringIO(input_str)
        devnull = io.StringIO()
        with redirect_stdout(devnull):
            with self.assertRaises(SystemExit):
                parse_float_list(num_floats=6, expected_units='kilometers')


    def test_parse_int_list(self):

        input_str = '1 2 3'
        expected = [1, 2, 3]
        sys.stdin = io.StringIO(input_str)
        result = parse_int_list(3)
        self.assertEqual(result, expected)

        input_str = '1 2 3   \n   4 5 6'
        expected = [1, 2, 3, 4, 5]
        sys.stdin = io.StringIO(input_str)
        result = parse_int_list(5)
        self.assertEqual(result, expected)

        input_str = '1 2 3   \n   4 5 6'
        sys.stdin = io.StringIO(input_str)
        with self.assertRaises(EOFError):
            parse_int_list(100)

        input_str = '1 2 3   \n   4 5 6'
        expected = [1]
        sys.stdin = io.StringIO(input_str)
        result = parse_int_list(1)
        self.assertEqual(result, expected)


    def test_logical_xor(self):
        self.assertEqual(logical_xor(True, True), False)
        self.assertEqual(logical_xor(False, False), False)
        self.assertEqual(logical_xor(False, True), True)
        self.assertEqual(logical_xor(True, False), True)


    def test_get_line(self):

        input_str = 'line 1\nline 2\nline 3'
        sys.stdin = io.StringIO(input_str)
        self.assertEqual(get_line(), 'line 1')
        self.assertEqual(get_line(), 'line 2')
        self.assertEqual(get_line(), 'line 3')

        input_str = '\nline 1\nline 2\nline 3'
        sys.stdin = io.StringIO(input_str)
        self.assertEqual(get_line(), 'line 1')

        input_str = 'line 1 # more line 1\nline 2\nline 3'
        sys.stdin = io.StringIO(input_str)
        self.assertEqual(get_line(), 'line 1 # more line 1')

        input_str = '  # line 1\nline 2\nline 3'
        sys.stdin = io.StringIO(input_str)
        self.assertEqual(get_line(), 'line 2')

        input_str = '  # line 1\nline 2\nline 3'
        sys.stdin = io.StringIO(input_str)
        self.assertEqual(get_line(skip_comments=False), '# line 1')

        input_str = '\nline 1\nline 2\nline 3'
        sys.stdin = io.StringIO(input_str)
        self.assertEqual(get_line(skip_blank_lines=False), '')

    def test_expand_units_abbreviation(self):
        self.assertEqual(expand_units_abbreviation('km'), 'kilometers')
        self.assertEqual(expand_units_abbreviation('s'), 'seconds')
        self.assertEqual(expand_units_abbreviation('deg'), 'degrees')
        self.assertEqual(expand_units_abbreviation('garbage'), None)

    def json_file_matches_json_str(self, file_path, json_str):

        with open(file_path) as f:
            file_dict = json.load(f)

        str_dict = json.loads(json_str)

        return sorted(file_dict.items()) == sorted(str_dict.items())

    def test_travel_time_with_stddev_depth_and_distance_data(self):
        input_file = (Path(__file__).resolve().parent /
                      "resources/travel_time_P")
        script = (Path(__file__).resolve().parents[1] /
                  "bin/travel-time-2-json.py")
        result = subprocess.getoutput(
            str(script)
            + " -m ak135 -p P < "
            + str(input_file.with_suffix(".txt"))
        )
        self.assertEqual(
            self.json_file_matches_json_str(input_file.with_suffix(".json"),
                                            result),
            True
        )

    def test_travel_time_with_stddev_distance_without_depth_data(self):
        input_file = (Path(__file__).resolve().parent /
                      "resources/travel_time_S")
        script = (Path(__file__).resolve().parents[1] /
                  "bin/travel-time-2-json.py")
        result = subprocess.getoutput(
            str(script)
            + " -m ak135 -p S < "
            + str(input_file.with_suffix(".txt"))
        )
        self.assertEqual(
            self.json_file_matches_json_str(input_file.with_suffix(".json"),
                                            result),
            True
        )

    def test_travel_time_with_constant_stddev_data(self):
        input_file = (Path(__file__).resolve().parent /
                      "resources/travel_time_pPKiKP")
        script = (Path(__file__).resolve().parents[1] /
                  "bin/travel-time-2-json.py")
        result = subprocess.getoutput(
            str(script)
            + " -m ak135 -p pPKiKP < "
            + str(input_file.with_suffix(".txt"))
        )
        self.assertEqual(
            self.json_file_matches_json_str(input_file.with_suffix(".json"),
                                            result),
            True
        )

    def test_travel_time_without_stddev_data(self):
        input_file = (Path(__file__).resolve().parent /
                      "resources/travel_time_SKKSac_B")
        script = (Path(__file__).resolve().parents[1] /
                  "bin/travel-time-2-json.py")
        result = subprocess.getoutput(
            str(script)
            + " -m ak135 -p SKKSac_B < "
            + str(input_file.with_suffix(".txt"))
        )
        self.assertEqual(
            self.json_file_matches_json_str(input_file.with_suffix(".json"),
                                            result),
            True
        )

    def test_slowness_uncertainty(self):
        input_file = (Path(__file__).resolve().parent /
                      "resources/slowness_uncertainty_ak135_P")
        script = (Path(__file__).resolve().parents[1] /
                  "bin/slowness-uncertainty-2-json.py")
        result = subprocess.getoutput(
            str(script)
            + " -m ak135 -p P < "
            + str(input_file.with_suffix(".txt"))
        )
        self.assertEqual(
            self.json_file_matches_json_str(input_file.with_suffix(".json"),
                                            result),
            True
        )


if __name__ == '__main__':
    unittest.main()
