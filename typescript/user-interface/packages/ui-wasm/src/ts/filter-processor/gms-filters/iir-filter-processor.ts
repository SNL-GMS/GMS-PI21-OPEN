import type {
  CascadedFilterDescription,
  LinearFilterDefinition
} from '@gms/common-model/lib/filter-list/types';
import { FilterType } from '@gms/common-model/lib/filter-list/types';
import { Timer, uuid } from '@gms/common-util';
import { UILogger } from '@gms/ui-util';
import produce from 'immer';

import { indexInc, indexOffset, taper } from './constants';
import { gmsFiltersModulePromise } from './gms-filters-module';
import { FilterBandType } from './types/filter-band-type';
import { FilterDesignModel } from './types/filter-design-model';
import type { IIRFilterParameters } from './types/iir-filter-parameters';
import type { LinearIIRFilterDescription } from './types/linear-iir-filter-definition';

const logger = UILogger.create('GMS_FILTERS', process.env.GMS_FILTERS);

/**
 * Converts a GMS COI Filter Definition to a GMS Filters Linear IIR Filter Description
 *
 * ! Must properly free/delete the memory of the returned object
 *
 * @param filterDefinition a GMS COI Filter Definition to convert
 * @returns a GMS Filters Linear IIR Filter Description
 */
export const convertToGMSFiltersLinearIIRFilterDescription = async (
  filterDefinition: LinearFilterDefinition
): Promise<LinearIIRFilterDescription> => {
  const gmsFiltersModule = await gmsFiltersModulePromise;

  const { aCoefficients, bCoefficients } = filterDefinition.filterDescription.parameters;

  if (filterDefinition.filterDescription.filterType !== FilterType.IIR_BUTTERWORTH) {
    throw new Error(`FilterType of ${FilterType.IIR_BUTTERWORTH} is only supported`);
  }

  if (
    ((filterDefinition.filterDescription as unknown) as CascadedFilterDescription)
      .filterDescriptions !== undefined
  ) {
    throw new Error(
      `Filter Descriptions should be undefined, not expecting Cascade Filter Definition`
    );
  }

  if (
    aCoefficients === undefined ||
    bCoefficients === undefined ||
    aCoefficients.length === 0 ||
    aCoefficients.length === 0
  ) {
    throw new Error('Invalid aCoefficients or bCoefficients');
  }

  if (aCoefficients.length !== bCoefficients.length) {
    throw new Error('Invalid aCoefficients or bCoefficients');
  }

  let iirFilterParameters: IIRFilterParameters;
  let linearIIRFilterDescription: LinearIIRFilterDescription;

  try {
    // * map GMS Filter Definition to GMS Filter Algorithm Definition

    // bCoefficients => sosNumerator
    const sosNumerator = new Float64Array(bCoefficients);

    // aCoefficients => sosDenominator
    const sosDenominator = new Float64Array(aCoefficients);

    // ! sosCoefficients can be ignored for now
    const sosCoefficients = new Float64Array();

    const numberOfSos = sosNumerator.length;
    const isDesigned = false;
    const groupDelay = filterDefinition.filterDescription.parameters.groupDelaySec;

    iirFilterParameters = gmsFiltersModule.IIRFilterParameters.buildWithTypedArray(
      sosNumerator,
      sosDenominator,
      sosCoefficients,
      isDesigned,
      numberOfSos,
      groupDelay
    );

    // filterType field in the data model combines: filterComputationType and filterDesignModel  with '_'
    const filterDesignModel =
      FilterDesignModel[
        filterDefinition.filterDescription.filterType.split(
          '_'
        )[1] as keyof typeof FilterDesignModel
      ];

    const filterBandType =
      FilterBandType[
        filterDefinition.filterDescription.passBandType as keyof typeof FilterBandType
      ];

    const cutoffLow = filterDefinition.filterDescription.lowFrequency;
    const cutoffHigh = filterDefinition.filterDescription.highFrequency;
    const filterOrder = filterDefinition.filterDescription.order;
    const sampleRate = filterDefinition.filterDescription.parameters.sampleRateHz;
    const sampleRateTolerance = filterDefinition.filterDescription.parameters.sampleRateToleranceHz;
    const zeroPhase = +filterDefinition.filterDescription.zeroPhase;

    linearIIRFilterDescription = gmsFiltersModule.LinearIIRFilterDescription.build(
      iirFilterParameters,
      filterDesignModel,
      filterBandType,
      cutoffLow,
      cutoffHigh,
      filterOrder,
      sampleRate,
      sampleRateTolerance,
      zeroPhase,
      taper
    );
  } catch (e) {
    logger.error('Failed to design filter using GMS iir filter design', e);
    throw e;
  } finally {
    // ! free any memory used for WASM
    /* eslint-disable no-underscore-dangle */
    iirFilterParameters.delete();
    gmsFiltersModule._free(iirFilterParameters as any);
    /* eslint-enable no-underscore-dangle */
  }

  return linearIIRFilterDescription;
};

/**
 * Converts a GMS Filters Linear IIR Filter Description to a GMS COI Filter Definition.
 *
 * @param filterDefinition a GMS COI Filter Definition
 * @param linearIIRFilterDescription a GMS Filters Linear IIR Filter Description
 * @returns a GMS COI Filter Definition
 */
export const convertFromGMSFiltersLinearIIRFilterDescription = (
  filterDefinition: LinearFilterDefinition,
  linearIIRFilterDescription: LinearIIRFilterDescription
): LinearFilterDefinition => {
  return produce(filterDefinition, draft => {
    // * map GMS Filter Algorithm Definition to GMS Filter Definition

    // sosNumerator => bCoefficients
    draft.filterDescription.parameters.bCoefficients = Array.from(
      linearIIRFilterDescription.iirFilterParameters.getSosNumeratorAsTypedArray()
    );

    // aCoefficients => sosDenominator
    draft.filterDescription.parameters.aCoefficients = Array.from(
      linearIIRFilterDescription.iirFilterParameters.getSosDenominatorAsTypedArray()
    );

    // ! sosCoefficients can be ignored for now

    draft.filterDescription.parameters.groupDelaySec =
      linearIIRFilterDescription.iirFilterParameters.groupDelay;
    draft.filterDescription.lowFrequency = linearIIRFilterDescription.cutoffLow;
    draft.filterDescription.highFrequency = linearIIRFilterDescription.cutoffHigh;
    draft.filterDescription.order = linearIIRFilterDescription.filterOrder;
    draft.filterDescription.parameters.sampleRateHz = linearIIRFilterDescription.sampleRate;
    draft.filterDescription.parameters.sampleRateToleranceHz =
      linearIIRFilterDescription.sampleRateTolerance;
    draft.filterDescription.zeroPhase = !!linearIIRFilterDescription.zeroPhase;
  });
};

/**
 * Designs a Linear Filter Definition
 *
 * @param filterDefinition the filter definition to design
 * @returns the designed filter definition
 */
export const iirFilterDesign = async (
  filterDefinition: LinearFilterDefinition
): Promise<LinearFilterDefinition> => {
  const id = uuid.asString();

  const gmsFiltersModule = await gmsFiltersModulePromise;

  let linearIIRFilterDescription: LinearIIRFilterDescription;
  let result: LinearIIRFilterDescription;
  let draft: LinearFilterDefinition;

  try {
    Timer.start(`${id} GMS Filter: iir filter design`);

    linearIIRFilterDescription = await convertToGMSFiltersLinearIIRFilterDescription(
      filterDefinition
    );

    result = gmsFiltersModule.FilterProvider.filterIIRDesign(linearIIRFilterDescription);

    draft = convertFromGMSFiltersLinearIIRFilterDescription(filterDefinition, result);
  } catch (e) {
    logger.error('Failed to design filter using GMS iir filter design', e);
    throw e;
  } finally {
    Timer.end(`${id} GMS Filter: iir filter design`);
    // ! free any memory used for WASM
    /* eslint-disable no-underscore-dangle */
    linearIIRFilterDescription.delete();
    gmsFiltersModule._free(linearIIRFilterDescription as any);

    result.delete();
    gmsFiltersModule._free(result as any);
    /* eslint-enable no-underscore-dangle */
  }

  return draft;
};

/**
 * Applies a Linear Filter Definition to the provided data (filters the data).
 *
 * @param filterDefinition a Linear Filter Definition
 * @param data  waveform data
 * @returns the filtered waveform data
 */
export const iirFilterApply = async (
  filterDefinition: LinearFilterDefinition,
  data: Float64Array
): Promise<Float64Array> => {
  const id = uuid.asString();

  const gmsFiltersModule = await gmsFiltersModulePromise;

  Timer.start(`${id} GMS Filter: iir filter`);

  let linearIIRFilterDescription: LinearIIRFilterDescription;
  let result: Float64Array;

  try {
    linearIIRFilterDescription = await convertToGMSFiltersLinearIIRFilterDescription(
      filterDefinition
    );

    linearIIRFilterDescription.iirFilterParameters.isDesigned = true;

    result = gmsFiltersModule.FilterProvider.filterIIRApply(
      data,
      indexOffset,
      indexInc,
      linearIIRFilterDescription
    );
  } catch (e) {
    logger.error('Failed to filter using GMS iir filter', e);
    throw e;
  } finally {
    Timer.end(`${id} GMS Filter: iir filter`);
    // ! free any memory used for WASM
    /* eslint-disable no-underscore-dangle */
    linearIIRFilterDescription.delete();
    gmsFiltersModule._free(linearIIRFilterDescription as any);
    /* eslint-enable no-underscore-dangle */
  }

  return result;
};
