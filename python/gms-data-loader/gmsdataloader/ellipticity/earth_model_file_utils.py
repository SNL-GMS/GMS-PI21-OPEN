# --------------------------------------------------------------------
#  earth_model_utils - module of utilities for earth model JSON serialization
# --------------------------------------------------------------------
import re
import sys
from typing import TextIO

FILE_ENDED_PREMATURELY = "stdin file ended prematurely."


def parse_float_list(input_file: TextIO, num_floats: int | None = None) -> list[float]:
    """
    This function reads the specified number of whitespace delimited floats from
    one of more input lines, and returns the floats as a list.
    If num_floats is None, then read all floats in the next line on stdin.  One
    line only.
    If expected_depth and expected_units are given (none or both, can not be given
    just one), then a commented line is expected before the list of floats from
    which the depth and units may be parsed.  A warning is issued if the read depth
    and/or units differ from what is expected.
    :param input_file: input to read table values from
    :param num_floats: number of floats to read
    :param expected_depth: depth value expected in the comment preceding the floats
    :param expected_units: units expected in the comment preceding the floats
    :return: list of num_floats floats
    """

    #todo: import orignal None checks if needed

    floats = read_num_floats(input_file, num_floats)

    return floats


def read_num_floats(input_file: TextIO, num_floats: int) -> list[float]:
    """
    This function reads a specific number of floats from stdin.  It expects
    to find exactly the number given, and raises an exception if fewer are found.

    :param input_file: input to read table values from
    :param num_floats: number of floats to read
    :return: list of exactly num_floats floats
    """

    floats = []
    read_count = 0
    while read_count < num_floats:
        line = get_line(input_file)
        if line is None:
            raise EOFError(FILE_ENDED_PREMATURELY)

        values = line.strip().split()
        for value in values:
            tmp = float(value)
            if tmp != -1.0:
                floats.append(tmp)
            else:
                floats.append(None)
            read_count += 1
            if num_floats <= read_count:
                break

    return floats


def get_line(input_file: TextIO, skip_comments: bool = True, skip_blank_lines: bool = True) -> str | None:
    """
    Reads next line (without trailing newline) from standard input, skipping # delimited
    comments unless requested, and skipping blank lines unless requested.
    :param input_file: input to read table values from
    :param skip_comments: do not return commented lines unless this parameter is False
    :param skip_blank_lines: do not return blank lines unless this parameter is False
    :return: Returns next non-comment / non-blank line from standard input without trailing newline.
             Returns next line (whether it is a comment or not) if skip_comments is False.
             Returns next line (whether it is blank or not) if skip_blank_lines is False.
             Returns None if end of file.
    """

    for line in input_file:
        line = re.sub(r'^\s*#', '#', line).rstrip()
        if ((len(line) == 0 and skip_blank_lines) or
                (0 < len(line) and line[0] == '#' and skip_comments)):
            continue
        return line

    return None

