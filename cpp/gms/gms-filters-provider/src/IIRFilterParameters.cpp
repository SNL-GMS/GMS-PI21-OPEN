#include "IIRFilterParameters.hh"

IIRFilterParameters::IIRFilterParameters(std::vector<double> sosNumerator,
                                         std::vector<double> sosDenominator,
                                         std::vector<double> sosCoefficients,
                                         bool isDesigned,
                                         int numberOfSos,
                                         double groupDelay) : sosNumerator(sosNumerator),
                                                              sosDenominator(sosDenominator),
                                                              sosCoefficients(sosCoefficients),
                                                              isDesigned(isDesigned),
                                                              numberOfSos(numberOfSos),
                                                              groupDelay(groupDelay){};

IIRFilterParameters IIRFilterParameters::build(std::vector<double> sosNumerator,
                                               std::vector<double> sosDenominator,
                                               std::vector<double> sosCoefficients,
                                               bool isDesigned,
                                               int numberOfSos,
                                               double groupDelay)
{
  return IIRFilterParameters(sosNumerator, sosDenominator, sosCoefficients, isDesigned, numberOfSos, groupDelay);
};

#if (__EMSCRIPTEN__)
IIRFilterParameters IIRFilterParameters::build(emscripten::val sosNumerator,
                                               emscripten::val sosDenominator,
                                               emscripten::val sosCoefficients,
                                               bool isDesigned,
                                               int numberOfSos,
                                               double groupDelay)
{
  std::vector<double> sosNumeratorVector = emscripten::vecFromJSArray<double>(sosNumerator);
  std::vector<double> sosDenominatorVector = emscripten::vecFromJSArray<double>(sosDenominator);
  std::vector<double> sosCoefficientsVector = emscripten::vecFromJSArray<double>(sosCoefficients);
  return IIRFilterParameters(sosNumeratorVector, sosDenominatorVector, sosCoefficientsVector, isDesigned, numberOfSos, groupDelay);
};

emscripten::val IIRFilterParameters::getSosNumeratorAsTypedArray()
{
  return emscripten::val(emscripten::typed_memory_view(sosNumerator.size(), sosNumerator.data()));
}

emscripten::val IIRFilterParameters::getSosDenominatorAsTypedArray()
{
  return emscripten::val(emscripten::typed_memory_view(sosDenominator.size(), sosDenominator.data()));
}

emscripten::val IIRFilterParameters::getSosCoefficientsAsTypedArray()
{
  return emscripten::val(emscripten::typed_memory_view(sosCoefficients.size(), sosCoefficients.data()));
}

#endif

IIR_FILTER_PARAMETERS IIRFilterParameters::to_cstruct(IIRFilterParameters *ifp)
{
  IIR_FILTER_PARAMETERS defStruct;
  defStruct.group_delay = ifp->groupDelay;
  defStruct.is_designed = ifp->isDesigned;
  defStruct.num_sos = ifp->numberOfSos;

  // Must copy due to fixed width C array lengths
  std::memcpy(defStruct.sos_coefficients, ifp->sosCoefficients.data(), ifp->sosCoefficients.size());
  std::memcpy(defStruct.sos_denominator, ifp->sosDenominator.data(), ifp->sosDenominator.size());
  std::memcpy(defStruct.sos_numerator, ifp->sosNumerator.data(), ifp->sosNumerator.size());
  return defStruct;
};

IIRFilterParameters IIRFilterParameters::from_cstruct(IIR_FILTER_PARAMETERS *ifp)
{
  std::vector<double> sosNumerator(ifp->sos_numerator, ifp->sos_numerator + ifp->num_sos);
  std::vector<double> sosDenominator(ifp->sos_denominator, ifp->sos_denominator + ifp->num_sos);
  std::vector<double> sosCoefficients(ifp->sos_coefficients, ifp->sos_coefficients + ifp->num_sos);

  return IIRFilterParameters(sosNumerator, sosDenominator, sosCoefficients, ifp->is_designed, ifp->num_sos, ifp->group_delay);
};
