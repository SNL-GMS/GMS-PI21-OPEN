#ifndef LINEAR_IIR_FILTER_DESCRIPTION_H
#define LINEAR_IIR_FILTER_DESCRIPTION_H

#include "IIRFilterParameters.hh"
#include <string>
#include <iostream>

extern "C"
{
#include "gms_filter.h"
}

class LinearIIRFilterDescription
{
private:
  LinearIIRFilterDescription(IIRFilterParameters iirFilterParameters,
                             FILTER_DESIGN_MODEL filterDesignModel,
                             FILTER_BAND_TYPE filterBandType,
                             double cutoffLow,
                             double cutoffHigh,
                             int filterOrder,
                             double sampleRate,
                             double sampleRateTolerance,
                             int zeroPhase,
                             int taper);

public:
  IIRFilterParameters iirFilterParameters;
  FILTER_DESIGN_MODEL filterDesignModel;
  FILTER_BAND_TYPE filterBandType;
  double cutoffLow;
  double cutoffHigh;
  int filterOrder;
  double sampleRate;
  double sampleRateTolerance;
  int zeroPhase;
  int taper;

  LinearIIRFilterDescription() = default;

  static LinearIIRFilterDescription build(IIRFilterParameters iirFilterParameters,
                                          int filterDesignModel,
                                          int filterBandType,
                                          double cutoffLow,
                                          double cutoffHigh,
                                          int filterOrder,
                                          double sampleRate,
                                          double sampleRateTolerance,
                                          int zeroPhase,
                                          int taper);

  static LINEAR_IIR_FILTER_DESCRIPTION to_cstruct(LinearIIRFilterDescription *lifd);

  static LinearIIRFilterDescription from_cstruct(LINEAR_IIR_FILTER_DESCRIPTION *lifd);
};

#endif // LINEAR_IIR_FILTER_DESCRIPTION
