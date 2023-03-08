#include "constants.h"
#include "enums.h"
#include "structs.h"

// Filter cascade functions using all filters in FILTER_DEFINITION
extern void filter_cascade_design(FILTER_DEFINITION *filter_definition);
extern void filter_cascade_apply(FILTER_DEFINITION *filter_definition, double data[], int num_data, int index_offset, int index_inc);

// Single IIR filter functions using LINEAR_IIR_FILTER_DESCRIPTION
extern void gms_filter_design_iir(LINEAR_IIR_FILTER_DESCRIPTION *linear_iir_filter_description);
extern void gms_filter_apply(double data[], int num_data, int index_offset, int index_inc, LINEAR_IIR_FILTER_DESCRIPTION *linear_iir_filter_description);

// Lower level filter functions using basic arguments
extern void filter_apply(double data[], int num_data, int index_offset, int index_inc, int taper, int zero_phase, double sos_numerator[], double sos_denominator[], int num_sos);
extern void filter_taper(double data[], int num_data, int index_offset, int index_inc, int taper_samples, int direction);