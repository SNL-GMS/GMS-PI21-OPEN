/*******************************************************************************
** Filter Functions to call from IAN
**
**
*/
#include "ian_filter.h"

/*******************************************************************************
** ian_filter_design
**
**  Design a digital filter based on filter_definition.
**
*/
void ian_filter_design(FILTER_DEFINITION *filter_definition)
{
  filter_cascade_design(filter_definition);
}

/*******************************************************************************
** ian_filter_apply
**
**  Filter data (design filter if needed, apply filter).
**
**  IAN data vector is sequence of [time, sample] pairs.
**  num_data is total length of ian_data; number of all the time and sample values
**
**  This function sets index_offset=1 to skip first value and index_inc=2 to skip alternating values.
**
*/
void ian_filter_apply(FILTER_DEFINITION *filter_definition, double ian_data[], int num_data)
{
  int index_offset;
  int index_inc;

  // Filter data
  index_offset = 1;
  index_inc = 2;
  filter_cascade_apply(filter_definition, ian_data, num_data, index_offset, index_inc);
}
