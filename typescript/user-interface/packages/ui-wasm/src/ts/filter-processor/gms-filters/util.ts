import type { BandType } from '@gms/common-model/lib/filter-list/types';
import { FilterType } from '@gms/common-model/lib/filter-list/types';

import { FilterBandType } from './types/filter-band-type';
import { FilterComputationType } from './types/filter-computation-type';
import { FilterDesignModel } from './types/filter-design-model';

/**
 * Converts the GMS COI Band Type Enum to the GMS Filters Filter Band Type
 *
 * @param passBandType a GMS COI Band Type
 * @returns a GMS Filters Filter Band Type
 */
export const getFilterBandType = (passBandType: BandType): FilterBandType =>
  FilterBandType[passBandType as keyof typeof FilterBandType];

/**
 * Converts the GMS COI Filter Type Enum to the GMS Filters Filter Design Model
 *
 * @param filterType a GMS COI Filter Type
 * @returns a GMS Filters Filter Design Model
 */
export const getFilterDesignModel = (filterType: FilterType): FilterDesignModel => {
  if (filterType === FilterType.CASCADE) {
    throw new Error(`Unable to determine Filter Design Model of type ${FilterType.CASCADE}`);
  }
  // filterType field in the data model combines: filterComputationType and filterDesignModel with '_'
  return FilterDesignModel[filterType.split('_')[1] as keyof typeof FilterDesignModel];
};

/**
 * Converts the GMS COI Filter Type Enum to the GMS Filters Filter Computation Type
 *
 * @param filterType a GMS COI Filter Type
 * @returns a GMS Filters Filter Computation Type
 */
export const getFilterComputationType = (filterType: FilterType): FilterComputationType => {
  if (filterType === FilterType.CASCADE) {
    throw new Error(`Unable to determine Filter Design Model of type ${FilterType.CASCADE}`);
  }
  // filterType field in the data model combines: filterComputationType and filterDesignModel with '_'
  return FilterComputationType[filterType.split('_')[0] as keyof typeof FilterComputationType];
};
