import { BandType, FilterType } from '@gms/common-model/lib/filter-list/types';
import React from 'react';
import renderer from 'react-test-renderer';

import { FilterTooltipContent } from '../../../../../src/ts/components/analyst-ui/components/filters/filter-tooltip-content';

describe('FilterTooltipContent', () => {
  it('matches a snapshot with all props', () => {
    const tree = renderer.create(
      <FilterTooltipContent
        name="Test"
        filter={{
          namedFilter: null,
          unfiltered: false,
          withinHotKeyCycle: true,
          filterDefinition: {
            comments: 'Test comments',
            name: 'Test definition name',
            filterDescription: {
              causal: true,
              comments: 'Test description comments',
              filterDescriptions: [
                {
                  causal: false,
                  comments: 'Cascade 1',
                  filterType: FilterType.IIR_BUTTERWORTH,
                  highFrequency: 0.8,
                  lowFrequency: 0.3,
                  order: 2,
                  passBandType: BandType.BAND_PASS,
                  zeroPhase: true,
                  parameters: {
                    aCoefficients: [1, 2, 3],
                    bCoefficients: [3, 2, 1],
                    groupDelaySec: 2,
                    sampleRateHz: 40,
                    sampleRateToleranceHz: 20
                  }
                },
                {
                  causal: false,
                  comments: 'Cascade 2',
                  filterType: FilterType.IIR_BUTTERWORTH,
                  highFrequency: 0.8,
                  lowFrequency: 0.3,
                  order: 2,
                  passBandType: BandType.BAND_PASS,
                  zeroPhase: true,
                  parameters: {
                    aCoefficients: [1, 2, 3],
                    bCoefficients: [3, 2, 1],
                    groupDelaySec: 2,
                    sampleRateHz: 40,
                    sampleRateToleranceHz: 20
                  }
                }
              ],
              filterType: FilterType.CASCADE,
              parameters: {
                groupDelaySec: 2,
                sampleRateHz: 40,
                sampleRateToleranceHz: 20
              }
            }
          }
        }}
      />
    );
    expect(tree.toJSON()).toMatchSnapshot();
  });
  it('matches a snapshot with only required props', () => {
    const tree = renderer.create(
      <FilterTooltipContent
        name="Test with less"
        filter={{
          filterDefinition: null,
          namedFilter: null,
          unfiltered: true,
          withinHotKeyCycle: false
        }}
      />
    );
    expect(tree.toJSON()).toMatchSnapshot();
  });
});
