/*******************************************************************************
** Filter Functions to call from IAN
**
**
*/

#ifndef GMS_IAN_FILTER_H
#define GMS_IAN_FILTER_H

#include "gms_filter.c"

// Filter cascade functions using all filters in FILTER_DEFINITION
void ian_filter_design(FILTER_DEFINITION *filter_definition);
void ian_filter_apply(FILTER_DEFINITION *filter_definition, double ian_data[], int num_data);

#endif // GMS_IAN_FILTER_H