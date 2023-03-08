#!/usr/bin/env python3

# --------------------------------------------------------------------
#  slowness-uncertainty-2-json - Convert Slowness Uncertainty Tables To JSON
#
#  The slowness-uncertainty-2-json command-line program is used to serialize
#  travel time tables and format them in JSON
# --------------------------------------------------------------------
import argparse
import json
import sys

from earth_model_utils import parse_int_list
from earth_model_utils import parse_float_list
from earth_model_utils import print_error
from earth_model_utils import stdin_exists


def main() -> None:
    args = get_args()

    [num_distance_samples, value_one] = parse_int_list(2)
    distances = parse_float_list(num_distance_samples)

    # value_one refers to the fact that there is only one list of corrections.
    # There should never be more than one since slowness uncertainty is only a
    # function of distance in this model.
    if value_one != 1:
        print_error(f'Unexpected number of sets of corrections.  Expected 1, saw {value_one}.')

    slowness_uncertainties = parse_float_list(num_distance_samples)

    output = {
        'model': args.model,
        'phase': args.phase,
        'slowness-uncertainty-units': args.slowness_uncertainty_units,
        'distance-units': args.distance_units,
        'travel-time-distances': distances,
        'slowness-uncertainties': slowness_uncertainties
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
  The slowness-uncertainty-2-json command-line program is used to convert ASCII whitespace 
  delimited slowness uncertainty tables to JSON format.

  A single ASCII slowness uncertainty table for one model and one phase only is read from 
  standard input.  The resulting JSON table is written to standard output.
"""
    parser = argparse.ArgumentParser(prog='slowness-uncertainty-2-json', description=description)

    parser.add_argument('-m', '--model',
                        choices=['iaspei', 'ak135'],
                        required=True)
    parser.add_argument('-p', '--phase',
                        required=True)
    parser.add_argument('--slowness-uncertainty-units',
                        choices=['seconds/kilometer'],
                        default='seconds/kilometer')
    parser.add_argument('--distance-units',
                        choices=['degrees'],
                        default='degrees')

    args = parser.parse_args()

    if not stdin_exists():
        print_error("One ASCII whitespace slowness uncertainty table must be provided in standard input.")
        sys.exit(1)

    return args


if __name__ == "__main__":
    main()
