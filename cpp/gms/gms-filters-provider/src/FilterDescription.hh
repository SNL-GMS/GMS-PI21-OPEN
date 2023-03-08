
#ifndef FILTER_DESCRIPTION_H
#define FILTER_DESCRIPTION_H

#include <string>
#include <stdexcept>
#include "LinearIIRFilterDescription.hh"
#include <iostream>

extern "C"
{
#include "gms_filter.h"
}

class FilterDescription
{
private:
  FilterDescription(LinearIIRFilterDescription linearIIRFilterDescription,
                    FILTER_COMPUTATION_TYPE filterComputationType,
                    std::string comments,
                    bool isCausal);

public:
  LinearIIRFilterDescription linearIIRFilterDescription;
  FILTER_COMPUTATION_TYPE filterComputationType;
  std::string comments;
  bool isCausal;

  FilterDescription() = default;

  static FilterDescription build(LinearIIRFilterDescription linearIIRFilterDescription,
                                 int filterComputationType,
                                 std::string comments,
                                 bool isCausal);

  static FILTER_DESCRIPTION to_cstruct(FilterDescription *filterDescription);

  static FilterDescription from_cstruct(FILTER_DESCRIPTION *fd);
};

#endif // FILTER_DESCRIPTION_H
