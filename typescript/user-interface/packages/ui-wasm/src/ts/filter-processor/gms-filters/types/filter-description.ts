import type { FilterComputationType } from './filter-computation-type';
import type { LinearIIRFilterDescription } from './linear-iir-filter-definition';

export interface FilterDescriptionModule {
  build: (
    linearIIRFilterDescription: LinearIIRFilterDescription,
    filterComputationType: FilterComputationType,
    comments: string,
    isCausal: boolean
  ) => FilterDescription;
}

export interface FilterDescription {
  linearIIRFilterDescription: LinearIIRFilterDescription;
  filterComputationType: {
    value: FilterComputationType;
  };
  comments: string;
  isCausal: boolean;
  delete: () => void;
}
