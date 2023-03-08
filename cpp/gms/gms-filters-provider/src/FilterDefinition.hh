#ifndef FILTER_DEFINITION_H
#define FILTER_DEFINITION_H

#include <array>
#include <vector>
#include <cstring>
#include "CascadedFiltersParameters.hh"
#include "FilterDescription.hh"
#include <iostream>

#if (__EMSCRIPTEN__)

#include <emscripten/emscripten.h>
#include <emscripten/bind.h>
#include <emscripten/em_macros.h>

#endif

class FilterDefinition
{

private:
  FilterDefinition(CascadedFiltersParameters cascadedFiltersParameters,
                   std::vector<FilterDescription> filterDescriptions,
                   std::string name,
                   std::string comments,
                   bool isDesigned,
                   bool removeGroupDelay,
                   int numberOfFilterDescriptions);

public:
  CascadedFiltersParameters cascadedFiltersParameters;
  std::vector<FilterDescription> filterDescriptions;
  std::string name;
  std::string comments;
  bool isDesigned;
  bool removeGroupDelay;
  int numberOfFilterDescriptions;

  FilterDefinition() = default;

  static FilterDefinition build(CascadedFiltersParameters cascadedFiltersParameters,
                                std::vector<FilterDescription> filterDescriptions,
                                std::string name,
                                std::string comments,
                                bool isDesigned,
                                bool removeGroupDelay,
                                int numberOfFilterDescriptions);

  static FILTER_DEFINITION to_cstruct(FilterDefinition *fd);

  static FilterDefinition from_cstruct(FILTER_DEFINITION *fd);
};

#endif // FILTER_DEFINITION_H
