#include "FilterDefinition.hh"

FilterDefinition::FilterDefinition(CascadedFiltersParameters cascadedFiltersParameters,
                                   std::vector<FilterDescription> filterDescriptions,
                                   std::string name,
                                   std::string comments,
                                   bool isDesigned,
                                   bool removeGroupDelay,
                                   int numberOfFilterDescriptions) : cascadedFiltersParameters(cascadedFiltersParameters),
                                                                     filterDescriptions(filterDescriptions),
                                                                     name(name),
                                                                     comments(comments),
                                                                     isDesigned(isDesigned),
                                                                     removeGroupDelay(removeGroupDelay),
                                                                     numberOfFilterDescriptions(numberOfFilterDescriptions){};

FilterDefinition FilterDefinition::build(CascadedFiltersParameters cascadedFiltersParameters,
                                         std::vector<FilterDescription> filterDescriptions,
                                         std::string name,
                                         std::string comments,
                                         bool isDesigned,
                                         bool removeGroupDelay,
                                         int numberOfFilterDescriptions)
{
  if (comments.length() > MAX_COMMENT_SIZE - 1)
  {
    auto errMsg = "Comment provided exceed maximum length: " + std::to_string(MAX_COMMENT_SIZE);
    throw std::invalid_argument(errMsg);
  }

  if (name.length() > MAX_NAME_SIZE - 1)
  {
    auto errMsg = "Name provided exceed maximum length: " + std::to_string(MAX_NAME_SIZE);
    throw std::invalid_argument(errMsg);
  }

  if (filterDescriptions.size() > MAX_FILTER_DESCRIPTIONS || numberOfFilterDescriptions > MAX_FILTER_DESCRIPTIONS)
  {
    auto errMsg = "Too many filter descriptions provided: " + std::to_string(MAX_FILTER_DESCRIPTIONS);
    throw std::invalid_argument(errMsg);
  }

  return FilterDefinition(cascadedFiltersParameters, filterDescriptions, name, comments, isDesigned, removeGroupDelay, numberOfFilterDescriptions);
};

FILTER_DEFINITION FilterDefinition::to_cstruct(FilterDefinition *fd)
{
  FILTER_DEFINITION defStruct;

  defStruct.cascaded_filters_parameters = CascadedFiltersParameters::to_cstruct(&(fd->cascadedFiltersParameters));

  std::strcpy(defStruct.name, fd->name.c_str());
  std::strcpy(defStruct.comments, fd->comments.c_str());

  for (int i = 0; i < fd->numberOfFilterDescriptions; i++)
  {
    defStruct.filter_description[i] = FilterDescription::to_cstruct(&fd->filterDescriptions[i]);
  }

  defStruct.is_designed = fd->isDesigned;

  defStruct.num_filter_descriptions = fd->numberOfFilterDescriptions;
  defStruct.remove_group_delay = fd->removeGroupDelay;
  return defStruct;
};

FilterDefinition FilterDefinition::from_cstruct(FILTER_DEFINITION *fd)
{
  CascadedFiltersParameters cfp = CascadedFiltersParameters::from_cstruct(&fd->cascaded_filters_parameters);

  std::vector<FilterDescription> fdescs;
  for (int i = 0; i < fd->num_filter_descriptions; i++)
  {
    fdescs.push_back(FilterDescription::from_cstruct(&fd->filter_description[i]));
  }

  return FilterDefinition(cfp, fdescs, fd->name, fd->comments, fd->is_designed, fd->remove_group_delay, fd->num_filter_descriptions);
};