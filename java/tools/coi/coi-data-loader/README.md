# coi-data-loader

## Purpose

To provide a general-purpose program to load data from the GMS COI into the OSD.

## Design and usage

The program has a command-line interface.  All of the arguments are optional.
Each specify a distinct piece (i.e. type) of COI data to load, typically as a JSON
file containing that data.  Waveforms work a little differently because of their size;
to load waveforms you must specify both `-waveformClaimCheck` (a file containing a SegmentClaimCheck[] as JSON)
and `-wfDir` (a directory containing binary waveform files, the names of which are referenced in
the SegmentClaimCheck's).

The program can store data directly to the databases (PostgreSQL and Cassandra) using
OSD repository implementations or by sending store requests to the OSD service.  The default behavior
is to use the database directly but providing flag `-useService` will use service communication instead.