/*******************************************************************************
** IAN FILTER TEST
**
**
*/

#include "../src/ian_filter.c"

void ian_filter_cascade_test(FILTER_DEFINITION *filter_definition, double ian_data[], int num_data);

/********************
** Tests
*/
void ian_filter_test()
{
  FILTER_DEFINITION filter_definition;
  double *ian_data;
  int num_data;
  int sample_rate;
  int i;

  sample_rate = 40;
  num_data = 2 * 60 * 60 * sample_rate * 2;

  // generate an IAN data vector with random data samples
  ian_data = (double *)malloc(sizeof(double) * num_data);
  for (i = 0; i < num_data; ++i)
  {
    ian_data[i] = i;
    ++i;
    ian_data[i] = (((double)rand() / (RAND_MAX)) - 0.5) * 2 * 10;
  }

  // Set up FILTER_DEFINITION
  filter_definition.is_designed = 0;
  filter_definition.remove_group_delay = 0;
  filter_definition.num_filter_descriptions = 3;

  filter_definition.filter_description[0].filter_type = IIR;
  filter_definition.filter_description[0].linear_iir_filter_description.design_model = BUTTERWORTH;
  filter_definition.filter_description[0].linear_iir_filter_description.band_type = BAND_PASS;
  filter_definition.filter_description[0].linear_iir_filter_description.cutoff_frequency_low = 3;
  filter_definition.filter_description[0].linear_iir_filter_description.cutoff_frequency_high = 15;
  filter_definition.filter_description[0].linear_iir_filter_description.filter_order = 3;
  filter_definition.filter_description[0].linear_iir_filter_description.sample_rate = sample_rate;
  filter_definition.filter_description[0].linear_iir_filter_description.zero_phase = 1;
  filter_definition.filter_description[0].linear_iir_filter_description.taper = 20;
  filter_definition.filter_description[0].linear_iir_filter_description.iir_filter_parameters.is_designed = 0;

  filter_definition.filter_description[1].filter_type = IIR;
  filter_definition.filter_description[1].linear_iir_filter_description.design_model = BUTTERWORTH;
  filter_definition.filter_description[1].linear_iir_filter_description.band_type = BAND_REJECT;
  filter_definition.filter_description[1].linear_iir_filter_description.cutoff_frequency_low = 5;
  filter_definition.filter_description[1].linear_iir_filter_description.cutoff_frequency_high = 6;
  filter_definition.filter_description[1].linear_iir_filter_description.filter_order = 5;
  filter_definition.filter_description[1].linear_iir_filter_description.sample_rate = sample_rate;
  filter_definition.filter_description[1].linear_iir_filter_description.zero_phase = 0;
  filter_definition.filter_description[1].linear_iir_filter_description.taper = 0;
  filter_definition.filter_description[1].linear_iir_filter_description.iir_filter_parameters.is_designed = 0;

  filter_definition.filter_description[2].filter_type = IIR;
  filter_definition.filter_description[2].linear_iir_filter_description.design_model = BUTTERWORTH;
  filter_definition.filter_description[2].linear_iir_filter_description.band_type = BAND_REJECT;
  filter_definition.filter_description[2].linear_iir_filter_description.cutoff_frequency_low = 11;
  filter_definition.filter_description[2].linear_iir_filter_description.cutoff_frequency_high = 13;
  filter_definition.filter_description[2].linear_iir_filter_description.filter_order = 7;
  filter_definition.filter_description[2].linear_iir_filter_description.sample_rate = sample_rate;
  filter_definition.filter_description[2].linear_iir_filter_description.zero_phase = 0;
  filter_definition.filter_description[2].linear_iir_filter_description.taper = 0;
  filter_definition.filter_description[2].linear_iir_filter_description.iir_filter_parameters.is_designed = 0;

  ian_filter_cascade_test(&filter_definition, ian_data, num_data);
}

void ian_filter_cascade_test(FILTER_DEFINITION *filter_definition, double ian_data[], int num_data)
{
  int i, j;

  printf("RUNNING ian_filter_cascade_test - num_data = %d\n", num_data);

  /*****************************************
  **
  ** ian_filter_design
  **
  */
  for (i = 0; i < filter_definition->num_filter_descriptions; ++i)
  {
    printf("-------------------------\n");
    printf("design_model: %d\n", filter_definition->filter_description[i].linear_iir_filter_description.design_model);
    printf("band_type: %d\n", filter_definition->filter_description[i].linear_iir_filter_description.band_type);
    printf("low_freq: %f\n", filter_definition->filter_description[i].linear_iir_filter_description.cutoff_frequency_low);
    printf("high_freq: %f\n", filter_definition->filter_description[i].linear_iir_filter_description.cutoff_frequency_high);
    printf("filter_order: %d\n", filter_definition->filter_description[i].linear_iir_filter_description.filter_order);
    printf("samp_rate: %f\n", filter_definition->filter_description[i].linear_iir_filter_description.sample_rate);
    printf("zero_phase: %d\n", filter_definition->filter_description[i].linear_iir_filter_description.zero_phase);
    printf("taper: %d\n", filter_definition->filter_description[i].linear_iir_filter_description.taper);
  }
  printf("-------------------------\n");
  printf("CALLING ian_filter_design\n");

  ian_filter_design(filter_definition);

  for (i = 0; i < filter_definition->num_filter_descriptions; ++i)
  {
    printf("-------------------------\n");
    printf("is_designed: %d\n", filter_definition->filter_description[i].linear_iir_filter_description.iir_filter_parameters.is_designed);
    printf("group_delay: %f\n", filter_definition->filter_description[i].linear_iir_filter_description.iir_filter_parameters.group_delay);
    printf("num_coefficients: %d\n", filter_definition->filter_description[i].linear_iir_filter_description.iir_filter_parameters.num_sos);
    printf("sos_numerator:   ");
    for (j = 0; j < MAX_SOS; ++j)
      printf("%f ", filter_definition->filter_description[i].linear_iir_filter_description.iir_filter_parameters.sos_numerator[j]);
    printf("\n");
    printf("sos_denominator: ");
    for (j = 0; j < MAX_SOS; ++j)
      printf("%f ", filter_definition->filter_description[i].linear_iir_filter_description.iir_filter_parameters.sos_denominator[j]);
    printf("\n");
  }
  printf("-------------------------\n");
  printf("remove_group_delay: %d\n", filter_definition->remove_group_delay);
  printf("composite group_delay: %f\n", filter_definition->cascaded_filters_parameters.group_delay);
  printf("composite is_designed: %d\n", filter_definition->is_designed);

  /*****************************************
  **
  ** ian_filter_apply
  **
  */
  printf("CALLING ian_filter_apply\n");

  ian_filter_apply(filter_definition, ian_data, num_data);

  printf("-------------------------\n");

  return;
}

void gms_filter_cascade_test(FILTER_DEFINITION *filter_definition, double data[], int num_data)
{
  int i, j;

  printf("RUNNING gms_filter_cascade_test - num_data = %d\n", num_data);

  /*****************************************
  **
  ** filter_design
  **
  */
  for (i = 0; i < filter_definition->num_filter_descriptions; ++i)
  {
    printf("-------------------------\n");
    printf("design_model: %d\n", filter_definition->filter_description[i].linear_iir_filter_description.design_model);
    printf("band_type: %d\n", filter_definition->filter_description[i].linear_iir_filter_description.band_type);
    printf("low_freq: %f\n", filter_definition->filter_description[i].linear_iir_filter_description.cutoff_frequency_low);
    printf("high_freq: %f\n", filter_definition->filter_description[i].linear_iir_filter_description.cutoff_frequency_high);
    printf("filter_order: %d\n", filter_definition->filter_description[i].linear_iir_filter_description.filter_order);
    printf("samp_rate: %f\n", filter_definition->filter_description[i].linear_iir_filter_description.sample_rate);
    printf("zero_phase: %d\n", filter_definition->filter_description[i].linear_iir_filter_description.zero_phase);
    printf("taper: %d\n", filter_definition->filter_description[i].linear_iir_filter_description.taper);
  }
  printf("-------------------------\n");
  printf("CALLING filter_cascade_design\n");

  filter_cascade_design(filter_definition);

  for (i = 0; i < filter_definition->num_filter_descriptions; ++i)
  {
    printf("-------------------------\n");
    printf("is_designed: %d\n", filter_definition->filter_description[i].linear_iir_filter_description.iir_filter_parameters.is_designed);
    printf("group_delay: %f\n", filter_definition->filter_description[i].linear_iir_filter_description.iir_filter_parameters.group_delay);
    printf("num_coefficients: %d\n", filter_definition->filter_description[i].linear_iir_filter_description.iir_filter_parameters.num_sos);
    printf("sos_numerator:   ");
    for (j = 0; j < MAX_SOS; ++j)
      printf("%f ", filter_definition->filter_description[i].linear_iir_filter_description.iir_filter_parameters.sos_numerator[j]);
    printf("\n");
    printf("sos_denominator: ");
    for (j = 0; j < MAX_SOS; ++j)
      printf("%f ", filter_definition->filter_description[i].linear_iir_filter_description.iir_filter_parameters.sos_denominator[j]);
    printf("\n");
  }
  printf("-------------------------\n");
  printf("remove_group_delay: %d\n", filter_definition->remove_group_delay);
  printf("composite group_delay: %f\n", filter_definition->cascaded_filters_parameters.group_delay);
  printf("composite is_designed: %d\n", filter_definition->is_designed);

  /*****************************************
  **
  ** filter_apply
  **
  */
  printf("CALLING filter_cascade_apply\n");

  // index_offset=0, index_inc=1
  filter_cascade_apply(filter_definition, data, num_data, 0, 1);

  printf("-------------------------\n");

  return;
}
