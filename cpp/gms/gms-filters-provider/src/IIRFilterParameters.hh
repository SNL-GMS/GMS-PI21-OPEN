#ifndef IIR_FILTER_PARAMETERS_H
#define IIR_FILTER_PARAMETERS_H

#include <array>
#include <vector>
#include <cstring>
#include <iostream>

#if (__EMSCRIPTEN__)

#include <emscripten/emscripten.h>
#include <emscripten/bind.h>
#include <emscripten/em_macros.h>

#endif

extern "C"
{
#include "gms_filter.h"
}

class IIRFilterParameters
{

private:
  IIRFilterParameters(std::vector<double> sosNumerator,
                      std::vector<double> sosDenominator,
                      std::vector<double> sosCoefficients,
                      bool isDesigned,
                      int numberOfSos,
                      double groupDelay);

public:
  IIRFilterParameters() = default;

  std::vector<double> sosNumerator;
  std::vector<double> sosDenominator;
  std::vector<double> sosCoefficients;
  bool isDesigned;
  int numberOfSos;
  double groupDelay;

#if (__EMSCRIPTEN__)
  emscripten::val getSosNumeratorAsTypedArray();
  emscripten::val getSosDenominatorAsTypedArray();
  emscripten::val getSosCoefficientsAsTypedArray();
#endif

  static IIRFilterParameters build(std::vector<double> sosNumerator,
                                   std::vector<double> sosDenominator,
                                   std::vector<double> sosCoefficients,
                                   bool isDesigned,
                                   int numberOfSos,
                                   double groupDelay);
#if (__EMSCRIPTEN__)
  static IIRFilterParameters build(emscripten::val sosNumerator,
                                   emscripten::val sosDenominator,
                                   emscripten::val sosCoefficients,
                                   bool isDesigned,
                                   int numberOfSos,
                                   double groupDelay);
#endif

  static IIR_FILTER_PARAMETERS to_cstruct(IIRFilterParameters *ifp);

  static IIRFilterParameters from_cstruct(IIR_FILTER_PARAMETERS *ifp);
};

#endif
