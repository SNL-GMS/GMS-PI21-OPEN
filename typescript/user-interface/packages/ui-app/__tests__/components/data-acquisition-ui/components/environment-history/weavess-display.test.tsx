/* eslint-disable @typescript-eslint/no-magic-numbers */
import { SohTypes } from '@gms/common-model';
import { render } from '@testing-library/react';
import React from 'react';

import { WeavessDisplay } from '../../../../../src/ts/components/data-acquisition-ui/components/environment-history/weavess-display';
import { testStationSoh } from '../../../../__data__/data-acquisition-ui/soh-overview-data';

const MOCK_TIME = 1611153271425;
Date.now = jest.fn(() => MOCK_TIME);
Date.constructor = jest.fn(() => new Date(MOCK_TIME));

describe('Environment history panel', () => {
  it('should be defined', () => {
    expect(Date.now()).toEqual(MOCK_TIME);
    expect(WeavessDisplay).toBeDefined();
  });

  it('render weavess display', () => {
    const { container } = render(
      <WeavessDisplay
        startTimeMs={500}
        endTimeMs={1000}
        channelSohs={testStationSoh.channelSohs}
        sohHistoricalDurations={[]}
        station={testStationSoh}
        aceiData={[
          {
            channelName: testStationSoh.channelSohs[0].channelName,
            monitorType: SohTypes.AceiType.CLIPPED,
            issues: [
              [
                [1482456217000, 0],
                [1482456293000, 1],
                [1482456369000, 0],
                [1482456445000, 0]
              ],
              [
                [1482457781000, 1],
                [1482457857000, 0],
                [1482457933000, 1],
                [1482458009000, 1]
              ],
              [
                [1482459345000, 0],
                [1482459421000, 1],
                [1482459497000, 0],
                [1482459573000, 0]
              ]
            ]
          }
        ]}
      />
    );
    expect(container).toMatchSnapshot();
  });
});
