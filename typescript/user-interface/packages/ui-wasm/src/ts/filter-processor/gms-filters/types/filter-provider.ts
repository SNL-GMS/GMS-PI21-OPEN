import type { FilterDefinition } from './filter-definition';
import type { LinearIIRFilterDescription } from './linear-iir-filter-definition';

export interface FilterProviderModule {
  filterCascadeDesign(filterDefinition: FilterDefinition): FilterDefinition;

  filterIIRDesign(
    linearIIRFilterDescription: LinearIIRFilterDescription
  ): LinearIIRFilterDescription;

  filterIIRApply(
    data: Float64Array,
    indexOffset: number,
    indexInc: number,
    linearIIRFilterDescription: LinearIIRFilterDescription
  ): Float64Array;

  filterCascadeApply(
    filter_definition: FilterDefinition,
    data: Float64Array,
    indexOffset: number,
    indexInc: number
  ): Float64Array;
}
