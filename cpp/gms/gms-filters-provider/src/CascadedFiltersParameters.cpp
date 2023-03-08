#include "CascadedFiltersParameters.hh"

CascadedFiltersParameters::CascadedFiltersParameters(
    std::string comments,
    bool isCausal,
    double sampleRate,
    double sampleRateTolerance,
    double groupDelay) : comments(comments),
                         isCausal(isCausal),
                         sampleRate(sampleRate),
                         sampleRateTolerance(sampleRateTolerance),
                         groupDelay(groupDelay){};

CascadedFiltersParameters CascadedFiltersParameters::build(std::string comments, bool isCausal, double sampleRate, double sampleRateTolerance, double groupDelay)
{
    if (comments.length() > MAX_COMMENT_SIZE - 1)
    {
        auto errMsg = "Comment provided exceed maximum length: " + std::to_string(MAX_COMMENT_SIZE);
        throw std::invalid_argument(errMsg);
    }

    return CascadedFiltersParameters(comments, isCausal, sampleRate, sampleRateTolerance, groupDelay);
};

CASCADED_FILTERS_PARAMETERS CascadedFiltersParameters::to_cstruct(CascadedFiltersParameters *cfp)
{
    CASCADED_FILTERS_PARAMETERS defStruct;

    std::strcpy(defStruct.comments, cfp->comments.c_str());

    defStruct.group_delay = cfp->groupDelay;
    defStruct.is_causal = cfp->isCausal;
    defStruct.sample_rate = cfp->sampleRate;
    defStruct.sample_rate_tolerance = cfp->sampleRateTolerance;
    return defStruct;
};

CascadedFiltersParameters CascadedFiltersParameters::from_cstruct(CASCADED_FILTERS_PARAMETERS *cfp)
{
    return CascadedFiltersParameters(std::string(cfp->comments), cfp->is_causal, cfp->sample_rate, cfp->sample_rate_tolerance, cfp->group_delay);
};
