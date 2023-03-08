#ifndef GMS_FILTER_PROVIDER_H
#define GMS_FILTER_PROVIDER_H

#if (__EMSCRIPTEN__)

#include <emscripten/emscripten.h>
#include <emscripten/bind.h>
#include <emscripten/em_macros.h>

#endif

#include "CascadedFiltersParameters.hh"
#include "FilterDefinition.hh"
#include "FilterDescription.hh"
#include "IIRFilterParameters.hh"
#include "LinearIIRFilterDescription.hh"
#include "complex.h"
#include <iostream>

extern "C"
{
#include "gms_filter.h"
}

class FilterProvider
{
public:
    FilterProvider() = default;

    static LinearIIRFilterDescription filterIIRDesign(LinearIIRFilterDescription linearIIRFilterDescription);

    static FilterDefinition filterCascadeDesign(FilterDefinition filterDefinition);

#if (__EMSCRIPTEN__)
    static emscripten::val filterIIRApply(emscripten::val data, int indexOffset, int indexInc, LinearIIRFilterDescription linearIIRFilterDescription);

    static emscripten::val filterCascadeApply(FilterDefinition filter_definition, emscripten::val data, int indexOffset, int indexInc);
#endif
};

extern "C"
{
    void cFilterIIRApply(double *data, unsigned long num_data, int index_offset, int index_inc, int taper, int zero_phase, double *sos_numerator, double *sos_denominator, int num_sos);
}

#endif // GMS_FILTER_PROVIDER_H

#if (__EMSCRIPTEN__)
EMSCRIPTEN_KEEPALIVE

EMSCRIPTEN_BINDINGS(FilterProvider)
{
    emscripten::register_vector<double>("VectorDouble");

    emscripten::constant("MAX_NAME_SIZE", MAX_NAME_SIZE);
    emscripten::constant("MAX_COMMENT_SIZE", MAX_COMMENT_SIZE);
    emscripten::constant("MAX_FILTER_ORDER", MAX_FILTER_ORDER);
    emscripten::constant("MAX_POLES", MAX_POLES);
    emscripten::constant("MAX_SOS", MAX_SOS);
    emscripten::constant("MAX_TRANSFER_FUNCTION", MAX_TRANSFER_FUNCTION);
    emscripten::constant("MAX_FILTER_DESCRIPTIONS", MAX_FILTER_DESCRIPTIONS);

    emscripten::enum_<FILTER_COMPUTATION_TYPE>("FilterComputationType")
        .value("FIR", FIR)
        .value("IIR", IIR)
        .value("AR", AR)
        .value("PM", PM);

    emscripten::enum_<FILTER_DESIGN_MODEL>("FilterDesignModel")
        .value("BUTTERWORTH", BUTTERWORTH)
        .value("CHEBYSHEV_I", CHEBYSHEV_I)
        .value("CHEBYSHEV_II", CHEBYSHEV_II)
        .value("ELLIPTIC", ELLIPTIC);

    emscripten::enum_<FILTER_BAND_TYPE>("FilterBandType")
        .value("LOW_PASS", LOW_PASS)
        .value("HIGH_PASS", HIGH_PASS)
        .value("BAND_PASS", BAND_PASS)
        .value("BAND_REJECT", BAND_REJECT);

    emscripten::class_<CascadedFiltersParameters>("CascadedFiltersParameters")
        .property("comments", &CascadedFiltersParameters::comments)
        .property("isCausal", &CascadedFiltersParameters::isCausal)
        .property("sampleRate", &CascadedFiltersParameters::sampleRate)
        .property("sampleRateTolerance", &CascadedFiltersParameters::sampleRateTolerance)
        .property("groupDelay", &CascadedFiltersParameters::groupDelay)
        .class_function("build", &CascadedFiltersParameters::build);

    emscripten::class_<FilterDefinition>("FilterDefinition")
        .property("cascadedFiltersParameters", &FilterDefinition::cascadedFiltersParameters)
        .property("filterDescriptions", &FilterDefinition::filterDescriptions)
        .property("name", &FilterDefinition::name)
        .property("comments", &FilterDefinition::comments)
        .property("isDesigned", &FilterDefinition::isDesigned)
        .property("removeGroupDelay", &FilterDefinition::removeGroupDelay)
        .property("numberOfFilterDescriptions", &FilterDefinition::numberOfFilterDescriptions)
        .class_function("build", &FilterDefinition::build);

    emscripten::class_<FilterDescription>("FilterDescription")
        .property("linearIIRFilterDescription", &FilterDescription::linearIIRFilterDescription)
        .property("filterComputationType", &FilterDescription::filterComputationType)
        .property("comments", &FilterDescription::comments)
        .property("isCausal", &FilterDescription::isCausal)
        .class_function("build", &FilterDescription::build);

    emscripten::class_<IIRFilterParameters>("IIRFilterParameters")
        .property("isDesigned", &IIRFilterParameters::isDesigned)
        .property("numberOfSos", &IIRFilterParameters::numberOfSos)
        .property("groupDelay", &IIRFilterParameters::groupDelay)
        .property("sosNumerator", &IIRFilterParameters::sosNumerator)
        .property("sosDenominator", &IIRFilterParameters::sosDenominator)
        .property("sosCoefficients", &IIRFilterParameters::sosCoefficients)
        .function("getSosNumeratorAsTypedArray", &IIRFilterParameters::getSosNumeratorAsTypedArray)
        .function("getSosDenominatorAsTypedArray", &IIRFilterParameters::getSosDenominatorAsTypedArray)
        .function("getSosCoefficientsAsTypedArray", &IIRFilterParameters::getSosCoefficientsAsTypedArray)
        .class_function("build", emscripten::select_overload<IIRFilterParameters(std::vector<double>, std::vector<double>, std::vector<double>, bool, int, double)>(&IIRFilterParameters::build), emscripten::allow_raw_pointers())
        .class_function("buildWithTypedArray", emscripten::select_overload<IIRFilterParameters(emscripten::val, emscripten::val, emscripten::val, bool, int, double)>(&IIRFilterParameters::build), emscripten::allow_raw_pointers());

    emscripten::class_<LinearIIRFilterDescription>("LinearIIRFilterDescription")
        .property("iirFilterParameters", &LinearIIRFilterDescription::iirFilterParameters)
        .property("filterDesignModel", &LinearIIRFilterDescription::filterDesignModel)
        .property("filterBandType", &LinearIIRFilterDescription::filterBandType)
        .property("cutoffLow", &LinearIIRFilterDescription::cutoffLow)
        .property("cutoffHigh", &LinearIIRFilterDescription::cutoffHigh)
        .property("filterOrder", &LinearIIRFilterDescription::filterOrder)
        .property("sampleRate", &LinearIIRFilterDescription::sampleRate)
        .property("sampleRateTolerance", &LinearIIRFilterDescription::sampleRateTolerance)
        .property("zeroPhase", &LinearIIRFilterDescription::zeroPhase)
        .property("taper", &LinearIIRFilterDescription::taper)
        .class_function("build", &LinearIIRFilterDescription::build);

    emscripten::class_<FilterProvider>("FilterProvider")
        .constructor()
        .class_function("filterIIRDesign", &FilterProvider::filterIIRDesign, emscripten::allow_raw_pointers())
        .class_function("filterIIRApply", &FilterProvider::filterIIRApply, emscripten::allow_raw_pointers())
        .class_function("filterCascadeDesign", &FilterProvider::filterCascadeDesign, emscripten::allow_raw_pointers())
        .class_function("filterCascadeApply", &FilterProvider::filterCascadeApply, emscripten::allow_raw_pointers());

    emscripten::register_vector<FilterDescription>("VectorFilterDescription");
}

#endif
