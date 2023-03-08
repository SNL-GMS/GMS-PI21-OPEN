# GMS Earth Model Scripts

This directory contains scripts for converting whitespace delimited ascii earth model files to JSON files. 
The software design review wiki page (*Solution Intent -> Design ->Software Design Reviews -> Interactive Analysis
Services -> Feature Prediction Service -> Earth Model Components*) includes descriptions of the files and their use.  
This document describes how the conversion scripts are run.

# bin directory

This directory contains the three Python conversion scripts:

### travel-time-2-json.py

Run with "--help" for a description of the command line arguments.  Ex.:
```commandline
./bin/travel-time-2-json.py -m ak135 -p P < travel_time_file.txt > travel_time_file.json
```
For a description of the output JSON, go the GMS Wiki.  Then go to *Solution Intent -> Design -> 
Software Design Reviews -> Interactive Analysis Services -> Feature Prediction Service -> Earth Model Components*.
On that page, go to *Structural Design -> Ak135 and IASPEI Travel Time*.

Each input file is expected to contain the travel time tables for a 
single model (i.e., ak135 or iaspei) and a single phase type (e.g., P, S, Pup, Pdiff, etc.).  Some integrity checks
are performed, and the JSON is printed to standard output.

### slowness-uncertainty-2-json.py
Run with "--help" for a descri ption of the command line arguments.  Ex.:
```commandline
./bin/slowness-uncertainty-2-json -m ak135 -p P < slowness-uncertainty_file.txt > slowness-uncertainty_file.json
```
For a description of the output JSON, go the GMS Wiki.  Then go to *Solution Intent -> Design -> 
Software Design Reviews -> Interactive Analysis Services -> Feature Prediction Service -> Earth Model Components*.
On that page, go to *Structural Design -> Ak135 Slowness Uncertainty*.

Each input file is expected to contain the slowness uncertainty tables for a 
single model (i.e., ak135 or iaspei) and a single phase type (e.g., P, S, Pup, Pdiff, etc.).  Some integrity checks
are performed, and the JSON is printed to standard output.

### dzeiwonski-gilbert-2-json.py
Run with "--help" for a description of the command line arguments.  Ex.:
```commandline
./bin/dzeiwonski-gilbert-2-json.py -m ak135 -p dzeiwonski-gilbert- path/to/dziewonski-gilbert/directory/
```
For a description of the output JSON, go the GMS Wiki.  Then go to *Solution Intent -> Design -> 
Software Design Reviews -> Interactive Analysis Services -> Feature Prediction Service -> Earth Model Components*.
On that page, go to *Structural Design -> Ak135 and IASPEI Dziewonski-Gilbert Ellipticity Correction*.

The input files in the provided directory each define zeiwonski-Gilbert ellipticity correction tau tables for a single phase.
The script reads any file in the provided directory that begins with `el_<model-name>`.
Other files in the directory are ignored.

Output files are named after the phase type contained therein.  The phase is prepended with the file name prefix 
given on the command line, and appended with ".json".  An exception is made for depth phases (i.e., phases whose name 
starts with a lowercase 'p' or 's').  In order to avoid name collisions on case-insensitive operating systems, the
leading 'p' or 's' is prepended with a tilda to indicate that it is lowercase.  E.g., if the prefix is "my-prefix-",
then the pP phase data will be output to "my-prefix-~pP.json" making it distinguishable from the PP phase data which
would be output to "my-prefix-PP.json".

# test directory
A single file, `test_earth_model_utils.py`, contains unit tests for individual functions, as well as a regression test
for each of the three conversion scripts.  The tests are hooked into the GMS build system, but to run them from the 
command line, execute:
```commandline
pytest python/gmsearthmodel
```
from the `gms-common` directory.
