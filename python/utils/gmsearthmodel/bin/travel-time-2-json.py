#!/usr/bin/env python3

# --------------------------------------------------------------------
#  travel-time-2-json - Convert Travel Time Tables To JSON
#
#  The travel-time-2-json command-line program is used to serialize
#  travel time tables and format them in JSON
# --------------------------------------------------------------------
import argparse
import json
import re
import sys

from earth_model_utils import parse_float_list
from earth_model_utils import get_line
from earth_model_utils import print_error
from earth_model_utils import print_warning
from earth_model_utils import stdin_exists
from earth_model_utils import parse_int_list
from earth_model_utils import expand_units_abbreviation
from earth_model_utils import FILE_ENDED_PREMATURELY


def main() -> None:
    args = get_args()

    #
    # Read Travel Time Table
    #

    num_depth_samples = parse_sample_count(args.depth_units)
    depths = parse_float_list(num_depth_samples)

    num_distance_samples = parse_sample_count(args.distance_units)
    distances = parse_float_list(num_distance_samples)

    travel_times = list()
    for depth in depths:
        floats = parse_float_list(num_distance_samples, depth, args.depth_units)
        one_depth_list = [seconds_to_duration(x) for x in floats]
        travel_times.append(one_depth_list)

    #
    # Read Standard Deviation Table
    #

    int_list = parse_int_list(2, exception_on_failure=False)
    if 2 == len(int_list):
        [num_error_distance_samples, num_error_depth_samples] = int_list
    else:
        [num_error_distance_samples, num_error_depth_samples] = [0, 0]

    errors = list()
    if 0 == num_error_distance_samples and 0 == num_error_depth_samples:
        error_distances = []
        error_depths = []
        errors = [[]]
    elif 1 == num_error_distance_samples and 1 == num_error_depth_samples:
        error_distances = []
        error_depths = []
        floats = parse_float_list(1)
        one_depth_list = [seconds_to_duration(x) for x in floats]
        errors.append(one_depth_list)
    elif 1 < num_error_distance_samples and 1 == num_error_depth_samples:
        error_distances = parse_float_list(num_error_distance_samples)
        error_depths = []
        floats = parse_float_list(num_error_distance_samples)
        one_depth_list = [seconds_to_duration(x) for x in floats]
        errors.append(one_depth_list)
    else:
        error_distances = parse_float_list(num_error_distance_samples)
        error_depths = parse_float_list(num_error_depth_samples)
        for depth in error_depths:
            floats = parse_float_list(num_error_distance_samples)
            one_depth_list = [seconds_to_duration(x) for x in floats]
            errors.append(one_depth_list)

    output = {
        'model': args.model,
        'phase': args.phase,
        'depthUnits': args.depth_units,
        'distanceUnits': args.distance_units,
        'travelTimeUnits': args.travel_time_units,
        'depths': depths,
        'distances': distances,
        'travelTimes': travel_times,
        'modelingErrorDepths': error_depths,
        'modelingErrorDistances': error_distances,
        'modelingErrors': errors
    }

    print(json.dumps(output, indent=3))


def get_args() -> dict[str, str]:
    """
    This function returns a dictionary of command line arguments.  It also handles any
    errors in syntax and help requests.
    :return: dictionary of command line arguments
    """
    description = """
description:
  The travel-time-2-json command-line program is used to convert ASCII whitespace 
  delimited travel time tables to JSON format.

  A single ASCII travel time table for one model and one phase only is read from 
  standard input.  The resulting JSON table is written to standard output.
"""
    parser = argparse.ArgumentParser(prog='travel-time-2-json', description=description)

    parser.add_argument('-m', '--model',
                        choices=['iaspei', 'ak135'],
                        required=True)
    parser.add_argument('-p', '--phase',
                        required=True)
    parser.add_argument('--travel-time-units',
                        choices=['seconds'],
                        default='seconds')
    parser.add_argument('--depth-units',
                        choices=['kilometers'],
                        default='kilometers')
    parser.add_argument('--distance-units',
                        choices=['degrees'],
                        default='degrees')

    args = parser.parse_args()

    if not stdin_exists():
        print_error("One ASCII whitespace delimited travel time table must be provided in standard input.")
        sys.exit(1)
    return args


def parse_sample_count(expected_units: str) -> int:
    """
    The next line is expected to start with an integer number of samples and end
    with units in parentheses. This function parses the number of samples and units,
    returns the number of samples, and prints a warning if the units differs from
    the expected value.  If units are not given, expected_units is ignored and no
    warning is given.
    :param expected_units: expected units, not abbreviated
    :return: number of samples or -1 on failure
    """

    # Expecting a line like:
    #    14   Number of depth samples at the following depths (km):
    # where the number of samples is 14, and the depth units is kilometers.

    line = get_line()
    if line is None:
        print_error(FILE_ENDED_PREMATURELY)
        sys.exit(1)

    re_match_count_and_units = re.search(r'(\d+).*\(([a-zA-Z]+)\)', line)
    re_match_count_only = re.search(r'(\d+)', line)

    if re_match_count_and_units:
        count = int(re_match_count_and_units.group(1))
        units = expand_units_abbreviation(re_match_count_and_units.group(2))
    elif re_match_count_only:
        count = int(re_match_count_only.group(1))
        units = expected_units
    else:
        print_error('Failed to parse count and units from line:\n' + line)
        return -1

    if units.casefold() != expected_units.casefold():
        print_warning(f'Expected units, {expected_units.casefold()}, '
                      f'differs from units in input file, {units.casefold()}.')

    return count


def seconds_to_duration(x: float | None) -> str | None:
    """
    Given a floating point number in seconds, return the equivalent ISO 8601 duration string.
    If None is given, then None is returned.
    :param x: seconds as a float
    :return: ISO 8601 duration string
    """

    if x is not None:
        duration = "PT" + str(x) + "S"
    else:
        duration = None

    return duration


if __name__ == "__main__":
    main()
