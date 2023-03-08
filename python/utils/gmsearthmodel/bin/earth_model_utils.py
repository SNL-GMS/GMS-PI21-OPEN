# --------------------------------------------------------------------
#  earth_model_utils - module of utilities for earth model JSON serialization
# --------------------------------------------------------------------
import re
import sys
from termcolor import cprint


# Constants
FILE_ENDED_PREMATURELY = "stdin file ended prematurely."


def parse_float_list(num_floats: int | None = None, expected_depth: float | None = None, expected_units: str | None = None) -> list[float]:
    """
    This function reads the specified number of whitespace delimited floats from
    one of more input lines, and returns the floats as a list.
    If num_floats is None, then read all floats in the next line on stdin.  One
    line only.
    If expected_depth and expected_units are given (none or both, can not be given
    just one), then a commented line is expected before the list of floats from
    which the depth and units may be parsed.  A warning is issued if the read depth
    and/or units differ from what is expected.
    :param num_floats: number of floats to read
    :param expected_depth: depth value expected in the comment preceding the floats
    :param expected_units: units expected in the comment preceding the floats
    :return: list of num_floats floats
    """

    # If expected_depth and expected_units are not None, a line like:
    #   # Travel time at depth =   0.00 km.
    # is expected before the whitespace delimited floats, where km is the
    # float's units.

    if logical_xor(expected_depth is not None, expected_units):
        print_error('parse_float_list() expects neither or both optional args, '
                    'expected_depth and expected_units.')

    if expected_depth:
        line = get_line(skip_comments=False)
        if line is None:
            raise EOFError(FILE_ENDED_PREMATURELY)
        words = line.split()
        read_depth = float(words[-2])
        read_units = expand_units_abbreviation(words[-1].strip('.'))

        if read_depth != expected_depth:
            print_warning(f'Expected depth, {expected_depth}, '
                          f'differs from depth in input file, {read_depth}.')
        if read_units.casefold() != expected_units.casefold():
            print_warning(f'Expected units, {expected_units.casefold()}, '
                          f'differs from units in input file, {read_units.casefold()}.')

    floats = list()
    if num_floats is None:
        line = get_line()
        if line is None:
            raise EOFError(FILE_ENDED_PREMATURELY)

        floats = [float(number) for number in line.strip().split()]
    else:
        floats = read_num_floats(num_floats)

    return floats


def read_num_floats(num_floats: int) -> list[float]:
    """
    This function reads a specific number of floats from stdin.  It expects
    to find exactly the number given, and raises an exception if fewer are found.

    :param num_floats: number of floats to read
    :return: list of exactly num_floats floats
    """

    floats = []
    read_count = 0
    while read_count < num_floats:
        line = get_line()
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


def parse_int_list(num_ints: int, exception_on_failure: bool = True) -> list[int]:
    """
    This function reads the specified number of whitespace delimited ints from
    one of more input lines and returns the ints as a list.
    :param num_ints: number of ints to read
    :param exception_on_failure: if True, raises an exception on failure to read
    specified number of ints.  Otherwise, returns empty list on failure.
    :return: list of num_ints ints
    """

    ints = list()
    read_count = 0
    while read_count < num_ints:
        line = get_line()
        if line is None:
            if exception_on_failure:
                raise EOFError(FILE_ENDED_PREMATURELY)
            else:
                return []

        values = line.strip().split()
        for value in values:
            ints.append(int(value))
            read_count += 1
            if num_ints <= read_count:
                break

    return ints


def logical_xor(arg1: bool, arg2: bool) -> bool:
    """
    Returns the logical exclusive or of the two boolean arguments.
    :param arg1: first boolean argument
    :param arg2: second boolean argument
    :return: arg1 xor arg2
    """
    return (arg1 and not arg2) or (not arg1 and arg2)


def get_line(skip_comments: bool = True, skip_blank_lines: bool = True) -> str | None:
    """
    Reads next line (without trailing newline) from standard input, skipping # delimited
    comments unless requested, and skipping blank lines unless requested.
    :param skip_comments: do not return commented lines unless this parameter is False
    :param skip_blank_lines: do not return blank lines unless this parameter is False
    :return: Returns next non-comment / non-blank line from standard input without trailing newline.
             Returns next line (whether it is a comment or not) if skip_comments is False.
             Returns next line (whether it is blank or not) if skip_blank_lines is False.
             Returns None if end of file.
    """

    for line in sys.stdin:
        line = re.sub(r'^\s*#', '#', line).rstrip()
        if ((len(line) == 0 and skip_blank_lines) or
                (0 < len(line) and line[0] == '#' and skip_comments)):
            continue
        return line

    return None


def expand_units_abbreviation(units: str) -> str | None:
    if units == 'km':
        return 'kilometers'
    if units == 's':
        return 'seconds'
    if units == 'deg':
        return 'degrees'

    return None


def print_warning(message: str) -> None:
    """
    Print a warning message in bold red.
    :param message: Message string to print
    """
    cprint(f'[WARNING] {message}', color='red', attrs=['bold'])


def print_error(message: str) -> None:
    """
    Print an error message in bold red.
    :param message: Message string to print
    """
    cprint(f'[ERROR] {message}', file=sys.stderr, color='red', attrs=['bold'])
    sys.exit(1)


def stdin_exists() -> bool:
    """
    Returns True if there is data at standard input.  Returns False otherwise.
    :return: True if there is data at standard input.  Returns False otherwise.
    """
    return not sys.stdin.isatty()
