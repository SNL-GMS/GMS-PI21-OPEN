#include "LinearIIRFilterDescription.hh"

LinearIIRFilterDescription::LinearIIRFilterDescription(IIRFilterParameters iirFilterParameters,
                                                       FILTER_DESIGN_MODEL filterDesignModel,
                                                       FILTER_BAND_TYPE filterBandType,
                                                       double cutoffLow,
                                                       double cutoffHigh,
                                                       int filterOrder,
                                                       double sampleRate,
                                                       double sampleRateTolerance,
                                                       int zeroPhase,
                                                       int taper) : iirFilterParameters(iirFilterParameters),
                                                                    filterDesignModel(filterDesignModel),
                                                                    filterBandType(filterBandType),
                                                                    cutoffLow(cutoffLow),
                                                                    cutoffHigh(cutoffHigh),
                                                                    filterOrder(filterOrder),
                                                                    sampleRate(sampleRate),
                                                                    sampleRateTolerance(sampleRateTolerance),
                                                                    zeroPhase(zeroPhase),
                                                                    taper(taper) {}

LinearIIRFilterDescription LinearIIRFilterDescription::build(IIRFilterParameters iirFilterParameters,
                                                             int filterDesignModel,
                                                             int filterBandType,
                                                             double cutoffLow,
                                                             double cutoffHigh,
                                                             int filterOrder,
                                                             double sampleRate,
                                                             double sampleRateTolerance,
                                                             int zeroPhase,
                                                             int taper)
{
  return LinearIIRFilterDescription(iirFilterParameters, (FILTER_DESIGN_MODEL)filterDesignModel, (FILTER_BAND_TYPE)filterBandType, cutoffLow, cutoffHigh, filterOrder, sampleRate, sampleRateTolerance, zeroPhase, taper);
};

LINEAR_IIR_FILTER_DESCRIPTION LinearIIRFilterDescription::to_cstruct(LinearIIRFilterDescription *lifd)
{
  LINEAR_IIR_FILTER_DESCRIPTION defStruct;

  defStruct.band_type = lifd->filterBandType;
  defStruct.cutoff_frequency_high = lifd->cutoffHigh;
  defStruct.cutoff_frequency_low = lifd->cutoffLow;
  defStruct.design_model = lifd->filterDesignModel;
  defStruct.filter_order = lifd->filterOrder;
  defStruct.iir_filter_parameters = IIRFilterParameters::to_cstruct(&lifd->iirFilterParameters);
  defStruct.sample_rate = lifd->sampleRate;
  defStruct.sample_rate_tolerance = lifd->sampleRateTolerance;
  defStruct.taper = lifd->taper;
  defStruct.zero_phase = lifd->zeroPhase;
  return defStruct;
};

LinearIIRFilterDescription LinearIIRFilterDescription::from_cstruct(LINEAR_IIR_FILTER_DESCRIPTION *lifd)
{
  return LinearIIRFilterDescription(IIRFilterParameters::from_cstruct(&(lifd->iir_filter_parameters)),
                                    lifd->design_model,
                                    lifd->band_type,
                                    lifd->cutoff_frequency_low,
                                    lifd->cutoff_frequency_high,
                                    lifd->filter_order,
                                    lifd->sample_rate,
                                    lifd->sample_rate_tolerance,
                                    lifd->zero_phase,
                                    lifd->taper);
};