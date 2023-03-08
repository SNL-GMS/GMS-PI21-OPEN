/*******************************************************************************
** IIR Filtering Functions
**
** Low-level functions to design and run IIR filters.
*/

#include "filter_iir.h"

/*******************************************************************************
** filter_iir
**
**  Apply filter definition to a data sequence.
**  The filter is defined as second-order-sections (sn[], sd[]).
**  Filtering is in-place (input data vector is overwritten with output).
**  Zero-phase (forward and reverse) is an option.
**  Zero-phase filtering doubles the falloff rate outside of the pass band
**  number of poles is effectively doubled.
**
**  Allows for non-sequential data vector using index_offset as a starting offset
**  and index_inc as the increment value (rather than incrementing by 1).
*/
void filter_iir(double data[], int num_data, int index_offset, int index_inc, int reverse, double sn[], double sd[], int num_sos)
{
  double x0;
  double y0 = 0.;

  double x1[MAX_SOS];
  double x2[MAX_SOS];
  double y1[MAX_SOS];
  double y2[MAX_SOS];

  int i2;
  int j;
  int j2;

  int num_val;
  int index_end;

  // Compute end of data vector with offset and increment
  num_val = (num_data - index_offset) / index_inc;
  index_end = index_offset + num_val * index_inc + index_offset;

  /*************************************************
  ** Initialize filter state variables
  */
  for (j = 0; j < num_sos; j++)
  {
    x1[j] = x2[j] = y1[j] = y2[j] = 0.;
  }

  /*************************************************
  ** Loop over each input sample to compute each output sample
  */
  for (int i = index_offset; i < num_data; i += index_inc)
  {
    j2 = 0;

    // Compute next data sample index
    if (reverse) // Reverse data index
      i2 = index_end - i;
    else // Forward data index
      i2 = i;

    // Get input data sample
    x0 = data[i2];

    // Loop over each SOS
    for (j = 0; j < num_sos; j++)
    {
      // Filter one input sample with the difference equation for one SOS
      y0 = (sn[j2] * x0 + sn[j2 + 1] * x1[j] + sn[j2 + 2] * x2[j]) - (sd[j2 + 1] * y1[j] + sd[j2 + 2] * y2[j]);
      y2[j] = y1[j];
      y1[j] = y0;
      x2[j] = x1[j];
      x1[j] = x0;

      // Index to next SOS coefficients
      j2 = j2 + 3;

      // Next input sample is this output sample
      x0 = y0;
    }

    // Set output sample in output data vector, storing in place of input
    // note, if num_sos is zero, this will be set to the initial val of y0 which is 0
    data[i2] = y0;
  }
}

/*******************************************************************************
** filter_design_iir
**
**  Classical IIR filter design.
**    1. Create analog lowpass prototype filter of the desired design type.
**    2. Transform frequency to desired band type.
**    3. Discretized analog filter to digital filter with bilinear transform.
**
**  Digital filter coefficients are structured as second-order-sections.
**
**  ** Only butterworth design model supported for now **
**  ** Others will be added like Chebyshev and Elliptic **
**
*/
void filter_design_iir(enum FILTER_DESIGN_MODEL design_model, enum FILTER_BAND_TYPE band_type, double fl, double fh, double fs, int filter_order, double sn[], double sd[], int *num_sos)
{
  double flw;
  double fhw;
  complex double poles[MAX_POLES];
  char pole_type[MAX_POLES];
  int num_poles;
  int i;

  /*************************************************
  ** 1. Design analog lowpass prototype filter.
  ** Normalized cutoff frequency of 1 rad/sec.
  */
  switch (design_model)
  {
  case BUTTERWORTH:
    butterworth_analog_design(filter_order, poles, pole_type, &num_poles);
    break;

  default:
    // set up no-op filter
    // just default to butterworth for now
    butterworth_analog_design(filter_order, poles, pole_type, &num_poles);
    break;
  }

  /*************************************************
  ** 2. Frequency transformation.
  ** Transform lowpass to desired band type.
  ** Prewarp cutoff frequencies using normalized nyquist (0.5 Hz).
  */
  switch (band_type)
  {
  case HIGH_PASS:
    fl = fl / fs / 2.;
    flw = tangent_warp(fl, 0.5);
    lp_to_hp(poles, pole_type, num_poles, sn, sd, num_sos);
    cutoff_alter(sn, sd, *num_sos, flw);
    break;

  case BAND_PASS:
    fl = fl / fs / 2.;
    fh = fh / fs / 2.;
    flw = tangent_warp(fl, 0.5);
    fhw = tangent_warp(fh, 0.5);
    lp_to_bp(poles, pole_type, num_poles, flw, fhw, sn, sd, num_sos);
    break;

  case BAND_REJECT:
    fl = fl / fs / 2.;
    fh = fh / fs / 2.;
    flw = tangent_warp(fl, 0.5);
    fhw = tangent_warp(fh, 0.5);
    lp_to_br(poles, pole_type, num_poles, flw, fhw, sn, sd, num_sos);
    break;

  default:
    // Default to low_pass
    fh = fh / fs / 2.;
    fhw = tangent_warp(fh, 0.5);
    lp_to_lp(poles, pole_type, num_poles, sn, sd, num_sos);
    cutoff_alter(sn, sd, *num_sos, fhw);
    break;
  }

  /*************************************************
  ** 3. Convert analog filter to digital filter
  ** with bilinear transform.
  */
  bilinear(sn, sd, *num_sos);
}

/*******************************************************************************
** butterworth_analog_design
**
** Compute analog (s-plane) butterworth poles for normalized lowpass filter.
**
**  int order
**      desired number of poles
**  complex poles[]
**      complex array containing poles
**      contains a single real pole at s=-1 if order is odd
**      contains only one pole for each complex conjugate pair
**      the complex conjugate pole is implied
**  character pole_type[]
**      character array indicating pole type:
**        'S' -- single real
**        'C' -- complex conjugate pair
**  int num_poles
**      number of complex poles
**      this is different from filter order because a complex pole represents two poles
**      will be same as the number of second-order-sections
**
*/
static void butterworth_analog_design(int order, complex double poles[], char pole_type[], int *num_poles)
{
  double angle;
  int half;

  half = order / 2;
  *num_poles = 0;

  // If odd order, add single first-order pole at (-1,0).
  if (2 * half < order)
  {
    poles[0] = (-1 + 0 * I);
    pole_type[0] = 'S';
    *num_poles = 1;
  }

  // Compute butterworth complex poles.
  // Complex conjugate matching pole is not created here.
  for (int k = 0; k < half; k++)
  {
    angle = M_PI * (0.5 + (double)(2 * (k + 1) - 1) / (double)(2 * order));
    // Compute first pole.
    poles[*num_poles] = (cos(angle) + sin(angle) * I);
    pole_type[*num_poles] = 'C';
    *num_poles = *num_poles + 1;
  }
  return;
}

/*******************************************************************************
** lp_to_lp
**
**  Transform normalized analog all-pole lowpass filter to a lowpass filter
**  using the analog polynomial transformation.
**  The lowpass filter is described in terms of its poles (as input to this routine).
**  The output is structured as second-order-sections.
**
*/
static void lp_to_lp(const complex double poles[], const char pole_type[], int num_poles, double sn[], double sd[], int *ns)
{
  int iptr;
  *ns = 0;
  iptr = 0;

  for (int i = 0; i < num_poles; i++)
  {
    if (pole_type[i] == 'C')
    {
      sn[iptr] = 1.;
      sn[iptr + 1] = 0.;
      sn[iptr + 2] = 0.;

      sd[iptr] = creal(poles[i] * conj(poles[i]));
      sd[iptr + 1] = -2. * creal(poles[i]);
      sd[iptr + 2] = 1.;

      iptr = iptr + 3;
      *ns = *ns + 1;
    }
    else if (pole_type[i] == 'S')
    {
      sn[iptr] = 1.;
      sn[iptr + 1] = 0.;
      sn[iptr + 2] = 0.;

      sd[iptr] = -creal(poles[i]);
      sd[iptr + 1] = 1.;
      sd[iptr + 2] = 0.;

      iptr = iptr + 3;
      *ns = *ns + 1;
    }
  }
  return;
}

/*******************************************************************************
** lp_to_hp
**
**  Transform normalized analog all-pole lowpass filter to a highpass filter
**  using the analog polynomial transformation.
**  The lowpass filter is described in terms of its poles (as input to this routine).
**  The output is structured as second-order-sections.
**
*/
static void lp_to_hp(const complex double poles[], const char pole_type[], int num_poles, double sn[], double sd[], int *ns)
{
  int iptr;
  *ns = 0;
  iptr = 0;

  for (int i = 0; i < num_poles; i++)
  {
    if (pole_type[i] == 'C')
    {
      sn[iptr] = 0.;
      sn[iptr + 1] = 0.;
      sn[iptr + 2] = 1.;

      sd[iptr] = 1.;
      sd[iptr + 1] = -2. * creal(poles[i]);
      sd[iptr + 2] = creal(poles[i] * conj(poles[i]));

      iptr = iptr + 3;
      *ns = *ns + 1;
    }
    else if (pole_type[i] == 'S')
    {
      sn[iptr] = 0.;
      sn[iptr + 1] = 1.;
      sn[iptr + 2] = 0.;

      sd[iptr] = 1.;
      sd[iptr + 1] = -creal(poles[i]);
      sd[iptr + 2] = 0.;

      iptr = iptr + 3;
      *ns = *ns + 1;
    }
  }
  return;
}

/*******************************************************************************
** lp_to_bp
**
**  Transform normalized analog all-pole lowpass filter to a bandpass filter
**  using the analog polynomial transformation.
**  The lowpass filter is described in terms of its poles (as input to this routine).
**  The output is structured as second-order-sections.
**
*/
static void lp_to_bp(const complex double poles[], const char pole_type[], int num_poles, double fl, double fh, double sn[], double sd[], int *ns)
{
  complex double ctemp;
  complex double p1;
  complex double p2;
  double twopi;
  double a;
  double b;
  int iptr;

  twopi = 2. * M_PI;
  a = twopi * twopi * fl * fh;
  b = twopi * (fh - fl);
  *ns = 0;
  iptr = 0;

  for (int i = 0; i < num_poles; i++)
  {
    if (pole_type[i] == 'C')
    {
      ctemp = (b * poles[i]);
      ctemp = (ctemp * ctemp);
      ctemp = (ctemp - (4 * a + 0 * I));
      ctemp = csqrt(ctemp);
      p1 = (0.5 * ((b * poles[i]) + ctemp));
      p2 = (0.5 * ((b * poles[i]) - ctemp));

      sn[iptr] = 0.;
      sn[iptr + 1] = b;
      sn[iptr + 2] = 0.;

      sd[iptr] = creal(p1 * conj(p1));
      sd[iptr + 1] = -2. * creal(p1);
      sd[iptr + 2] = 1.;

      iptr = iptr + 3;

      sn[iptr] = 0.;
      sn[iptr + 1] = b;
      sn[iptr + 2] = 0.;

      sd[iptr] = creal(p2 * conj(p2));
      sd[iptr + 1] = -2. * creal(p2);
      sd[iptr + 2] = 1.;

      iptr = iptr + 3;
      *ns = *ns + 2;
    }
    else if (pole_type[i] == 'S')
    {
      sn[iptr] = 0.;
      sn[iptr + 1] = b;
      sn[iptr + 2] = 0.;

      sd[iptr] = a;
      sd[iptr + 1] = -b * creal(poles[i]);
      sd[iptr + 2] = 1.;

      iptr = iptr + 3;
      *ns = *ns + 1;
    }
  }
  return;
}

/*******************************************************************************
** lp_to_br
**
**  Transform normalized analog all-pole lowpass filter to a bandreject filter
**  using the analog polynomial transformation.
**  The lowpass filter is described in terms of its poles (as input to this routine).
**  The output is structured as second-order-sections.
**
*/
static void lp_to_br(const complex double poles[], const char pole_type[], int num_poles, double fl, double fh, double sn[], double sd[], int *ns)
{
  complex double pinv;
  complex double ctemp;
  complex double p1;
  complex double p2;
  double twopi;
  double a;
  double b;
  int iptr;

  twopi = 2. * M_PI;
  a = twopi * twopi * fl * fh;
  b = twopi * (fh - fl);
  *ns = 0;
  iptr = 0;

  for (int i = 0; i < num_poles; i++)
  {
    if (pole_type[i] == 'C')
    {
      pinv = ((1 + 0 * I) / poles[i]);
      ctemp = ((b * pinv) * (b * pinv));
      ctemp = (ctemp - (4 * a + 0 * I));
      ctemp = csqrt(ctemp);
      p1 = (0.5 * ((b * pinv) + ctemp));
      p2 = (0.5 * ((b * pinv) - ctemp));

      sn[iptr] = a;
      sn[iptr + 1] = 0.;
      sn[iptr + 2] = 1.;

      sd[iptr] = creal(p1 * conj(p1));
      sd[iptr + 1] = -2. * creal(p1);
      sd[iptr + 2] = 1.;

      iptr = iptr + 3;

      sn[iptr] = a;
      sn[iptr + 1] = 0.;
      sn[iptr + 2] = 1.;

      sd[iptr] = creal(p2 * conj(p2));
      sd[iptr + 1] = -2. * creal(p2);
      sd[iptr + 2] = 1.;

      iptr = iptr + 3;
      *ns = *ns + 2;
    }
    else if (pole_type[i] == 'S')
    {
      sn[iptr] = a;
      sn[iptr + 1] = 0.;
      sn[iptr + 2] = 1.;

      sd[iptr] = -a * creal(poles[i]);
      sd[iptr + 1] = b;
      sd[iptr + 2] = -creal(poles[i]);

      iptr = iptr + 3;
      *ns = *ns + 1;
    }
  }
  return;
}

/*******************************************************************************
** tangent_warp
**
**  Applies tangent frequency warping to compensate for bilinear analog to digital transformation.
**
*/
static double tangent_warp(double f, double fs)
{
  double twopi;
  double angle;
  double warp;
  double fac;

  twopi = 2. * M_PI;
  fac = f / fs / 2;
  angle = twopi * fac;
  warp = 2. * tan(angle) * fs;
  warp = warp / twopi;
  return warp;
}

/*******************************************************************************
** cutoff_alter
**
**  Alter the cutoff of a filter structured as second-order-sections.
**  Changes the cutoffs of normalized lowpass and highpass filters through
**  a simple polynomial transformation.
**
*/
static void cutoff_alter(double sn[], double sd[], int ns, double f)
{
  double scale;
  int j;

  scale = 2. * M_PI * f;
  j = 0;
  for (int i = 0; i < ns; i++)
  {
    sn[j + 1] = sn[j + 1] / scale;
    sn[j + 2] = sn[j + 2] / (scale * scale);

    sd[j + 1] = sd[j + 1] / scale;
    sd[j + 2] = sd[j + 2] / (scale * scale);

    j = j + 3;
  }
  return;
}

/*******************************************************************************
** bilinear
**
**  Transforms an analog filter to a digital filter using the bilinear transformation.
**  Both are stored as second-order-sections.
**  The transformation is done in-place.
**
**  double sn[]
**      Array containing numerator polynomial coefficients for second-order-sections.
**      Packed head-to-tail.
**  double sd[]
**      Array containing denominator polynomial coefficients for second-order-sections.
**      Packed head-to-tail.
**  integer ns
**      Number of second-order-sections.
**
*/
static void bilinear(double sn[], double sd[], int ns)
{
  double a0;
  double a1;
  double a2;
  double scale;
  int j;

  j = 0;

  for (int i = 0; i < ns; i++)
  {
    a0 = sd[j];
    a1 = sd[j + 1];
    a2 = sd[j + 2];
    scale = a2 + a1 + a0;

    sd[j] = 1.;
    sd[j + 1] = (2. * (a0 - a2)) / scale;
    sd[j + 2] = (a2 - a1 + a0) / scale;

    a0 = sn[j];
    a1 = sn[j + 1];
    a2 = sn[j + 2];

    sn[j] = (a2 + a1 + a0) / scale;
    sn[j + 1] = (2. * (a0 - a2)) / scale;
    sn[j + 2] = (a2 - a1 + a0) / scale;

    j += 3;
  }
  return;
}
