#ifndef CASCADED_FILTERS_PARAMETERS_H
#define CASCADED_FILTERS_PARAMETERS_H

#include <cstring>
#include <string>
#include <stdexcept>
#include <iostream>

extern "C"
{
#include "gms_filter.h"
}

class CascadedFiltersParameters
{

private:
    CascadedFiltersParameters(std::string comments, bool isCausal, double sampleRate, double sampleRateTolerance, double groupDelay);

public:
    std::string comments;
    bool isCausal;
    double sampleRate;
    double sampleRateTolerance;
    double groupDelay;

    CascadedFiltersParameters() = default;

    static CascadedFiltersParameters build(std::string comments, bool isCausal, double sampleRate, double sampleRateTolerance, double groupDelay);

    static CASCADED_FILTERS_PARAMETERS to_cstruct(CascadedFiltersParameters *cfp);

    static CascadedFiltersParameters from_cstruct(CASCADED_FILTERS_PARAMETERS *cfp);
};

#endif // CASCADED_FILTERS_PARAMETERS_H
