#ifndef STRUCTS_H
#define STRUCTS_H

#include "constants.h"
#include "enums.h"

/*******************************************************************************
** IIR_FILTER_PARAMETERS
**
** Contains computed filter parameters.
*/
typedef struct
{
  // Flag if parameters have been computed
  int is_designed;

  // Second-order-section coefficients
  double sos_numerator[MAX_SOS];
  double sos_denominator[MAX_SOS];
  double sos_coefficients[MAX_SOS];
  int num_sos;

  // Group delay in seconds
  double group_delay;

} IIR_FILTER_PARAMETERS;

/*******************************************************************************
** FIR_FILTER_PARAMETERS
**
** Contains computed filter parameters.
*/
typedef struct
{
  // Flag if parameters have been computed
  int is_designed;

  // Transfer function coefficients
  double transfer_function_b[MAX_TRANSFER_FUNCTION];
  int num_transfer_function;

  // Group delay in seconds
  double group_delay;

} FIR_FILTER_PARAMETERS;

/*******************************************************************************
** LINEAR_IIR_FILTER_DESCRIPTION
**
** Contains a set of parameters to design a linear IIR filter
** and the resulting IIR_FILTER_PARAMETERS.
*/
typedef struct
{
  // Filter design parameters
  enum FILTER_DESIGN_MODEL design_model; // BUTTERWORTH, CHEBYSHEV_I, etc

  enum FILTER_BAND_TYPE band_type; // LOW_PASS, HIGH_PASS, BAND_PASS, BAND_REJECT
  double cutoff_frequency_low;
  double cutoff_frequency_high;

  int filter_order;
  double sample_rate;
  double sample_rate_tolerance;

  int zero_phase; // 1 if zero-phase (filter in forward and reverse direction)
  int taper;      // number of samples for cosine taper (0 if no taper)

  // Computed filter parameters
  IIR_FILTER_PARAMETERS iir_filter_parameters;

} LINEAR_IIR_FILTER_DESCRIPTION;

/*******************************************************************************
** LINEAR_FIR_FILTER_DESCRIPTION
**
** Contains a set of parameters to design a linear FIR filter
** and the resulting FIR_FILTER_PARAMETERS.
*/
typedef struct
{
  // Filter design parameters
  enum FILTER_DESIGN_MODEL design_model; // Window function

  enum FILTER_BAND_TYPE band_type; // LOWPASS, HIGHPASS, BANDPASS, BANDREJECT
  double cutoff_frequency_low;
  double cutoff_frequency_high;

  int filter_order;
  double sample_rate;
  double sample_rate_tolerance;

  int zero_phase; // 1 if zero-phase (filter in forward and reverse direction)
  int taper;      // number of samples for cosine taper (0 if no taper)

  // Computed filter coefficients parameters
  FIR_FILTER_PARAMETERS fir_filter_parameters;

} LINEAR_FIR_FILTER_DESCRIPTION;

/*******************************************************************************
** AUTOREGRESSIVE_FILTER_DEFINITION
**
** Contains a set of parameters for an autoregressive filter.
*/
typedef struct
{
  // Filter design parameters
  int tbd;

} AUTOREGRESSIVE_FILTER_DEFINITION;

/*******************************************************************************
** PHASE_MATCH_FILTER_DEFINITION
**
** Contains a set of parameters for a phase match filter.
*/
typedef struct
{
  // Filter design parameters
  int tbd;

} PHASE_MATCH_FILTER_DEFINITION;

/*******************************************************************************
** FILTER_DESCRIPTION
**
** Contains a set of parameters for a filter sequence (or cascade)
** and a set of LINEAR_FILTER_PARAMETERS for each filter in the sequence.
*/
typedef struct
{
  // Filter description info
  char comments[MAX_COMMENT_SIZE];
  int is_causal;

  // Filter computation type
  enum FILTER_COMPUTATION_TYPE filter_type; // IIR, FIR, AR, PM, etc.

  // Only one type of filter should be populated (indicated by filter_type)
  // Only IIR supported so far.
  LINEAR_IIR_FILTER_DESCRIPTION linear_iir_filter_description;

} FILTER_DESCRIPTION;

/*******************************************************************************
** CASCADED_FILTERS_PARAMETERS
**
** Contains FILTER_DESCRIPTION info for cascaded filters.
** Contains high-level parameters and a set of FILTER_DESCRIPTION structures.
** If more than one filter_description, this is a cascade filter.
** For a cascade, the filters will be executed by their order in the filter_description array.
**
*/
typedef struct
{
  // Filter description info
  char comments[MAX_COMMENT_SIZE];
  int is_causal;

  // Cascaded filters parameters
  double sample_rate;
  double sample_rate_tolerance;
  double group_delay;

} CASCADED_FILTERS_PARAMETERS;

/*******************************************************************************
** FILTER_DEFINITION
**
** Top level filter definition structure.
** Contains high-level parameters and a set of FILTER_DESCRIPTION structures.
** If more than one filter_description, this is a cascade filter.
** For a cascade, the filters will be executed by their order in the filter_description array.
**
*/
typedef struct
{
  // Filter definition info
  char name[MAX_NAME_SIZE];
  char comments[MAX_COMMENT_SIZE];

  // Flag if parameters have been computed
  int is_designed;

  //  Control parameters
  int remove_group_delay;

  // Composite info for a filter cascade
  // Contains summary info for a single filter
  CASCADED_FILTERS_PARAMETERS cascaded_filters_parameters;

  // Included filter descriptions (multiple if cascade)
  int num_filter_descriptions;
  FILTER_DESCRIPTION filter_description[MAX_FILTER_DESCRIPTIONS];

} FILTER_DEFINITION;

#endif // STRUCTS_H