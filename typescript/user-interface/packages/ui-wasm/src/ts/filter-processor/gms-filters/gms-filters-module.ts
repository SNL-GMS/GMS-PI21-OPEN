/* eslint-disable no-underscore-dangle */
import { Timer, uuid } from '@gms/common-util';
import { UILogger } from '@gms/ui-util';

// ! IGNORED TO SUPPORT ESLINT CHECKS WITHOUT REQUIRING TO BUILD THE WASM
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
// eslint-disable-next-line import/no-unresolved
import gmsFilters from '../../wasm/gms-filters/gms-filters.mjs';
import type { CascadedFiltersParametersModule } from './types/cascade-filter-parameters.js';
import type { IFilterBandType } from './types/filter-band-type';
import type { IFilterComputationType } from './types/filter-computation-type';
import type { FilterDefinitionModule } from './types/filter-definition.js';
import type { FilterDescriptionModule } from './types/filter-description.js';
import type { IFilterDesignModel } from './types/filter-design-model';
import type { FilterProviderModule } from './types/filter-provider.js';
import type { IIRFilterParametersModule } from './types/iir-filter-parameters.js';
import type { LinearIIRFilterDescriptionModule } from './types/linear-iir-filter-definition.js';
import type { VectorDoubleModule } from './types/vector-double.js';
import type { VectorFilterDescriptionModule } from './types/vector-filter-description.js';

// ! IGNORED TO SUPPORT ESLINT CHECKS WITHOUT REQUIRING TO BUILD THE WASM
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
// eslint-disable-next-line @typescript-eslint/no-require-imports, import/no-unresolved
require('../../wasm/gms-filters/gms-filters.wasm');

const logger = UILogger.create('GMS_FILTERS', process.env.GMS_FILTERS);

/**
 * Emscripten GMS Filters Module
 */
export interface GmsFiltersModule extends EmscriptenModule {
  // Module.cwrap() will be available by doing this; requires -s "EXTRA_EXPORTED_RUNTIME_METHODS=['cwrap']"
  cwrap: typeof cwrap;

  VectorDouble: VectorDoubleModule;

  VectorFilterDescription: VectorFilterDescriptionModule;

  MAX_NAME_SIZE: number;
  MAX_COMMENT_SIZE: number;
  MAX_FILTER_ORDER: number;
  MAX_POLES: number;
  MAX_SOS: number;
  MAX_TRANSFER_FUNCTION: number;
  MAX_FILTER_DESCRIPTIONS: number;

  FilterComputationType: IFilterComputationType;
  FilterDesignModel: IFilterDesignModel;
  FilterBandType: IFilterBandType;

  CascadedFiltersParameters: CascadedFiltersParametersModule;
  IIRFilterParameters: IIRFilterParametersModule;
  LinearIIRFilterDescription: LinearIIRFilterDescriptionModule;
  FilterDescription: FilterDescriptionModule;
  FilterDefinition: FilterDefinitionModule;

  FilterProvider: FilterProviderModule;
}

let loadedGmsFiltersModule: GmsFiltersModule;

/**
 * !Helper function to ensure that the module only loads once.
 *
 * @returns a promise to load the GMS filters module
 */
const getGmsFiltersModule = async (): Promise<GmsFiltersModule> => {
  // load the module only once
  if (loadedGmsFiltersModule === undefined) {
    const id = uuid.asString();
    Timer.start(`${id} GMS Filter: load wasm`);
    loadedGmsFiltersModule = await (gmsFilters as () => Promise<GmsFiltersModule>)();
    Timer.end(`${id}GMS Filter: load wasm`);
    logger.debug('Loaded GMS Filter WASM Module', loadedGmsFiltersModule);
  }
  return loadedGmsFiltersModule;
};

/**
 * GMS filters module promise; used to load the module only once
 */
export const gmsFiltersModulePromise: Promise<GmsFiltersModule> = getGmsFiltersModule();
