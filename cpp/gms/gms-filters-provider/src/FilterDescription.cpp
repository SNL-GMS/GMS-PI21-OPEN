#include "FilterDescription.hh"

FilterDescription::FilterDescription(LinearIIRFilterDescription linearIIRFilterDescription,
                                     FILTER_COMPUTATION_TYPE filterComputationType,
                                     std::string comments,
                                     bool isCausal) : linearIIRFilterDescription(linearIIRFilterDescription),
                                                      filterComputationType(filterComputationType),
                                                      comments(comments),
                                                      isCausal(isCausal){};

FilterDescription FilterDescription::build(LinearIIRFilterDescription linearIIRFilterDescription,
                                           int filterComputationType,
                                           std::string comments,
                                           bool isCausal)
{
  if (comments.length() > MAX_COMMENT_SIZE - 1)
  {
    auto errMsg = "Comments provided exceed maximum length: " + std::to_string(MAX_COMMENT_SIZE);
    throw std::invalid_argument(errMsg);
  }

  return FilterDescription(linearIIRFilterDescription, (FILTER_COMPUTATION_TYPE)filterComputationType, comments, isCausal);
};

FILTER_DESCRIPTION FilterDescription::to_cstruct(FilterDescription *filterDescription)
{
  // Filter description info
  FILTER_DESCRIPTION defStruct;
  
  std::strcpy(defStruct.comments, filterDescription->comments.c_str());

  defStruct.is_causal = filterDescription->isCausal;
  defStruct.filter_type = filterDescription->filterComputationType;
  defStruct.linear_iir_filter_description = LinearIIRFilterDescription::to_cstruct(&filterDescription->linearIIRFilterDescription);
  return defStruct;
};

FilterDescription FilterDescription::from_cstruct(FILTER_DESCRIPTION *fd)
{
  return FilterDescription(LinearIIRFilterDescription::from_cstruct(&(fd->linear_iir_filter_description)),
                           fd->filter_type,
                           fd->comments,
                           fd->is_causal);
}