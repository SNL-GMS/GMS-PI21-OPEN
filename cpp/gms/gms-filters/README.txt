GMS IIR (Butterworth) Filter Library

These filtering functions include data indexing arguments to work with IAN time-data vectors.
Use index_offset=1 and index_inc=2 for IAN.
Use index_offset=0 and index_inc=1 for a standard waveform vector.
These function do no dynamic memory allocation.
Input data vectors are overwritten with filtered data.
C structures approximating COI FILTER_DEFINITION classes are used in higher
level functions to organize multiple filter descriptions for cascades.

ian_filter.c - high level wrappers using FILTER_DEFINITION with IAN data vector attributes set

gms_filter.c - cascade filter design and filtering using FILTER_DEFINITION, 
               and mid level functions using basic arguments

gms_filter.h - COI FILTER_DEFINITION structures

filter_iir.c - low level IIR filter design (Butterworth only for now) and filter execution functions

