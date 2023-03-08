/*******************************************************************************
** GMS Filter Functions
**
** Functions to design and run filters and filter cascades.
** Uses data structures following GMS COI Filter Definition classes for parameters.
**
** Function arguments include index_offset and index_inc that are passed through to support non-sequential data vectors.
**
*/

#include <stdlib.h>
#include <string.h>
#include "gms_filter.h"
#include "filter_iir.c"

/*******************************************************************************
** filter_cascade_design
**
**  Design a filter cascade.
**  Only designs the IIR filters.
**
*/
extern void filter_cascade_design(FILTER_DEFINITION *filter_definition)
{
  LINEAR_IIR_FILTER_DESCRIPTION *linear_iir_filter_description;
  double group_delay;

  group_delay = 0;

  // Design each filter.
  // Don't check is_designed, just force a new design operation.
  for (int i = 0; i < filter_definition->num_filter_descriptions; ++i)
  {
    linear_iir_filter_description = &filter_definition->filter_description[i].linear_iir_filter_description;

    // Design the filter.
    if (filter_definition->filter_description[i].filter_type == IIR)
    {
      gms_filter_design_iir(linear_iir_filter_description);
      group_delay += linear_iir_filter_description->iir_filter_parameters.group_delay;
    }
  }

  // Set composite group delay.
  filter_definition->cascaded_filters_parameters.group_delay = group_delay;

  filter_definition->is_designed = 1;
}

/*******************************************************************************
** filter_cascade_apply
**
**  Apply a filter cascade to a data vector.
**  Design filters if needed.
**
*/
void filter_cascade_apply(FILTER_DEFINITION *filter_definition, double data[], int num_data, int index_offset, int index_inc)
{


  LINEAR_IIR_FILTER_DESCRIPTION *linear_iir_filter_description;

  // If not already designed, design the filter.
  if (filter_definition->is_designed == 0)
  {
    filter_cascade_design(filter_definition);
  }

  // Process each filter.
  for (int i = 0; i < filter_definition->num_filter_descriptions; ++i)
  {
    linear_iir_filter_description = &filter_definition->filter_description[i].linear_iir_filter_description;

    // Filter data.
    gms_filter_apply(data, num_data, index_offset, index_inc, linear_iir_filter_description);
  }
}

/*******************************************************************************
** gms_filter_design_iir
**
**  Calls filter_design_iir() using LINEAR_IIR_FILTER_DESCRIPTION values.
**
*/
extern void gms_filter_design_iir(LINEAR_IIR_FILTER_DESCRIPTION *linear_iir_filter_description)
{
  filter_design_iir(
      linear_iir_filter_description->design_model,
      linear_iir_filter_description->band_type,
      linear_iir_filter_description->cutoff_frequency_low,
      linear_iir_filter_description->cutoff_frequency_high,
      linear_iir_filter_description->sample_rate,
      linear_iir_filter_description->filter_order,
      linear_iir_filter_description->iir_filter_parameters.sos_numerator,
      linear_iir_filter_description->iir_filter_parameters.sos_denominator,
      &linear_iir_filter_description->iir_filter_parameters.num_sos);

  // Set group delay for this filter.
  linear_iir_filter_description->iir_filter_parameters.group_delay = 0;

  linear_iir_filter_description->iir_filter_parameters.is_designed = 1;
}

/*******************************************************************************
** gms_filter_apply
**
**  Calls filter_apply() using LINEAR_IIR_FILTER_DESCRIPTION values.
**
*/
extern void gms_filter_apply(double data[], int num_data, int index_offset, int index_inc, LINEAR_IIR_FILTER_DESCRIPTION *linear_iir_filter_description)
{
  if (linear_iir_filter_description->iir_filter_parameters.is_designed == 1)
  {
    filter_apply(
        data, num_data, index_offset, index_inc,
        linear_iir_filter_description->taper,
        linear_iir_filter_description->zero_phase,
        linear_iir_filter_description->iir_filter_parameters.sos_numerator,
        linear_iir_filter_description->iir_filter_parameters.sos_denominator,
        linear_iir_filter_description->iir_filter_parameters.num_sos);
  }
}

/*******************************************************************************
** filter_apply
**
**  Filter a data vector.
**
**  Filtering is in-place (input data vector is overwritten with output).
**  Zero-phase filtering (forward and reverse) is an option.
**  Zero-phase filtering doubles the falloff rate outside of the pass band,
**  the number of poles is effectively doubled.
**
*/
extern void filter_apply(double data[], int num_data, int index_offset, int index_inc, int taper, int zero_phase, double sos_numerator[], double sos_denominator[], int num_sos)
{
  /*************************************************
  ** Filter input data in forward direction.
  */
  // Taper start of data before filtering.
  if (taper > 0)
    filter_taper(data, num_data, index_offset, index_inc, taper, 0);

  // Run filter in forward direction.
  filter_iir(data, num_data, index_offset, index_inc, 0, sos_numerator, sos_denominator, num_sos);

  /*************************************************
  ** If zero_phase, also filter output data
  ** again in reverse direction.
  */
  if (zero_phase)
  {
    // Taper end of data before filtering in reverse.
    if (taper > 0)
      filter_taper(data, num_data, index_offset, index_inc, taper, 1);

    // Run filter in reverse direction.
    filter_iir(data, num_data, index_offset, index_inc, 1, sos_numerator, sos_denominator, num_sos);
  }
}

/*******************************************************************************
** filter_taper
**
**  Apply a cosine taper to data vector.
**  If direction == 0, apply to start of data vector.
**  If direction == 1, apply to end of data vector.
**  If direction == 2, apply to both.
**
**  Allows for non-sequential data vector using index_offset as a starting offset
**  and index_inc as the increment value (rather than incrementing by 1).
*/
extern void filter_taper(double data[], int num_data, int index_offset, int index_inc, int taper_samples, int direction)
{
  int j;
  int j2;
  double taper_weight;
  int num_val;
  int index_end;

  // Compute end of data vector with offset and increment
  num_val = (num_data - index_offset) / index_inc;
  index_end = index_offset + num_val * index_inc + index_offset;

  if (taper_samples > num_val / 2)
    taper_samples = num_val / 2;

  // Simple cosine taper function
  for (int i = 0; i < taper_samples; i++)
  {
    taper_weight = 0.5 - 0.5 * cos(M_PI * i / taper_samples);

    j = index_offset + i * index_inc;

    if (direction == 0 || direction == 2)
    {
      data[j] = taper_weight * data[j];
    }

    if (direction == 1 || direction == 2)
    {
      j2 = index_end - j;
      data[j2] = taper_weight * data[j2];
    }
  }
}
