#!/usr/bin/env python3

# --------------------------------------------------------------------
#  dziewonski-gilbert-2-json - Convert Ellipticity Correction Tables To JSON
#
#  The dziewonski-gilbert-2-json command-line program is used to serialize
#  dziewonski-gilbert ellipticity correction tables and format them in JSON
# --------------------------------------------------------------------
import json
import os
from typing import List

from typing import IO, TextIO

from gmsdataloader.ellipticity.earth_model_file_utils import get_line, parse_float_list


def parse_for_model(model: str, directory: str) -> dict[str, str]:
    file_names = [os.path.join(directory, f) for f in os.listdir(directory) if f.startswith('el_' + model)]

    file_json_map = {}

    # each time through this while loop is one phase type
    for file_name in file_names:
        phase_tables = parse_phase_table(file_name)

        output = {
            'model': model,
            'phase': phase_tables['phase'],
            'depthUnits': "kilometers",
            'distanceUnits': "degrees",
            'depths': phase_tables['depths'],
            'distances': phase_tables['distances'],
            'tau0': phase_tables['tau0'],
            'tau1': phase_tables['tau1'],
            'tau2': phase_tables['tau2']
        }

        file_json_map[phase_tables['phase']] = json.dumps(output, indent=3)

    return file_json_map


def parse_phase_table(input_file_name: str) -> dict[str, str] | None:
    """
    This function parses standard input for the tau tables for the next phase type at
    standard input.  It reads the phase type, the list of depths, and a set of tau0, tau1,
    tau2 for each distance.  It returns a dictionary with the phase, list of depths, list of
    distances, and three tau tables: tau0, tau1, and tau2.  The tau tables are each a 2-D array
    (i.e., a list of lists).  The first index is depth, and the second index is distance.
    If standard input is at end of file, None is returned.
    :return: A dictionary of tau tables for one phase type.  Returns None if end of file.
    """

    with open(input_file_name) as input_file:
        depths = parse_samples(input_file)
        distances = parse_samples(input_file)

        if depths is None or distances is None:
            return None

        tau0 = [[] for _ in range(0, len(distances))]
        tau1 = [[] for _ in range(0, len(distances))]
        tau2 = [[] for _ in range(0, len(distances))]

        num_tau = 3
        for _ in range(0, len(depths)):
            for j in range(0, len(distances)):
                line = parse_float_list(input_file, num_tau)
                tau0[j].append(line[0])
                tau1[j].append(line[1])
                tau2[j].append(line[2])

    return {
        'phase': parse_phase(input_file_name.split('_', maxsplit=2)[-1]),
        'depths': depths,
        'distances': distances,
        'tau0': tau0,
        'tau1': tau1,
        'tau2': tau2
    }


def get_filename(prefix: str, phase: str) -> str:
    """
    Returns the filename for the given phase.  Filenames will be the prefix followed by the phase,
    and ".json".  The exception is for phases that start with a lower-case 's' or 'p'.  The names
    of those files will be the prefix followed by a tilda ('~'), followed by the phase, and ".json".
    This will make files distinguishable on case-insensitive operating systems.
    :param prefix: filename prefix
    :param phase: phase name
    :return: name of file for the given phase
    """

    if phase.startswith("s") or phase.startswith("p"):
        filename = prefix + '~' + phase + ".json"
    else:
        filename = prefix + phase + ".json"

    return filename


def parse_samples(input_file: TextIO) -> List[float] | None:
    line = get_line(input_file)
    if line is None:
        return None

    num_samples = int(line.split()[0])
    samples = []
    while len(samples) < num_samples:
        for sample in get_line(input_file).split():
            samples.append(float(sample))

    return samples


def parse_phase(phase: str) -> str:
    return replace_str_in_phase(replace_str_in_phase(phase, 'little'), 'big')


def replace_str_in_phase(phase: str, replace: str) -> str:
    new_phase = phase
    while replace in new_phase:
        replace_index = new_phase.find(replace)
        underscore_index = new_phase.find('_')
        if underscore_index != -1:
            new_phase = new_phase[:replace_index] + new_phase[replace_index + len(replace):underscore_index] + new_phase[underscore_index + 1:]
        else:
            new_phase = new_phase[:replace_index] + new_phase[replace_index + len(replace):]
    return new_phase
