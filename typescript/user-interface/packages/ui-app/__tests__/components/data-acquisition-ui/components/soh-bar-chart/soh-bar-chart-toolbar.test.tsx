/* eslint-disable react/jsx-no-constructed-context-values */
import { SohTypes } from '@gms/common-model';
import { uuid } from '@gms/common-util';
import type { DropdownItem } from '@gms/ui-core-components/lib/components/ui-widgets/deprecated-toolbar/types';
import { dataAcquisitionActions, FilterableSOHTypes, getStore } from '@gms/ui-state';
import { sohConfiguration } from '@gms/ui-state/__tests__/__data__/soh-configuration-query-data';
import { render } from '@testing-library/react';
import uniqueId from 'lodash/uniqueId';
import React from 'react';
import { Provider } from 'react-redux';

import { BaseDisplayContext } from '../../../../../src/ts/components/common-ui/components/base-display';
import type { ToolbarProps } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-bar-chart/soh-bar-chart-toolbar';
import { DeprecatedToolbar } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-bar-chart/soh-bar-chart-toolbar';
import {
  makeLagSortingDropdown,
  SOHLagOptions
} from '../../../../../src/ts/components/data-acquisition-ui/shared/toolbars/soh-toolbar-items';

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    useGetSohConfigurationQuery: jest.fn(() => ({
      data: sohConfiguration,
      isLoading: false
    }))
  };
});

uuid.asString = jest.fn().mockImplementation(uniqueId);

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('Toolbar class', () => {
  const sortDropdown: DropdownItem = makeLagSortingDropdown(SOHLagOptions.LAG_HIGHEST, jest.fn());
  // see https://stackoverflow.com/questions/57805917/mocking-refs-in-react-function-component
  const mockUseRef = (obj: any) => () =>
    Object.defineProperty({}, 'current', {
      get: () => obj,
      // eslint-disable-next-line no-empty,@typescript-eslint/no-empty-function
      set: () => {}
    });
  const ref: any = mockUseRef({ refFunction: jest.fn() });
  const station: SohTypes.UiStationSoh = {
    id: '1',
    uuid: '1',
    needsAcknowledgement: true,
    needsAttention: true,
    sohStatusSummary: undefined,
    stationGroups: [],
    statusContributors: [],
    time: undefined,
    stationName: '1',
    allStationAggregates: [],
    channelSohs: [
      {
        channelName: 'adsf',
        channelSohStatus: undefined,
        allSohMonitorValueAndStatuses: [
          {
            monitorType: SohTypes.SohMonitorType.LAG,
            value: 10,
            valuePresent: true,
            status: SohTypes.SohStatusSummary.GOOD,
            hasUnacknowledgedChanges: true,
            contributing: false,
            quietUntilMs: 1,
            thresholdBad: 3,
            thresholdMarginal: 3
          },
          {
            monitorType: SohTypes.SohMonitorType.LAG,
            value: 11,
            valuePresent: true,
            status: SohTypes.SohStatusSummary.GOOD,
            hasUnacknowledgedChanges: true,
            contributing: false,
            quietUntilMs: 1,
            thresholdBad: 3,
            thresholdMarginal: 3
          }
        ]
      },
      {
        channelName: 'adsf2',
        channelSohStatus: undefined,
        allSohMonitorValueAndStatuses: [
          {
            monitorType: SohTypes.SohMonitorType.LAG,
            value: 10,
            valuePresent: true,
            status: SohTypes.SohStatusSummary.GOOD,
            hasUnacknowledgedChanges: true,
            contributing: false,
            quietUntilMs: 1,
            thresholdBad: 3,
            thresholdMarginal: 3
          },
          {
            monitorType: SohTypes.SohMonitorType.LAG,
            value: 11,
            valuePresent: true,
            status: SohTypes.SohStatusSummary.GOOD,
            hasUnacknowledgedChanges: true,
            contributing: false,
            quietUntilMs: 1,
            thresholdBad: 3,
            thresholdMarginal: 3
          }
        ]
      }
    ]
  };

  const toolbarProps: ToolbarProps = {
    statusesToDisplay: {
      [FilterableSOHTypes.GOOD]: true,
      [FilterableSOHTypes.BAD]: true,
      [FilterableSOHTypes.MARGINAL]: true
    } as Record<FilterableSOHTypes, boolean>,
    setStatusesToDisplay: jest.fn(),
    sortDropdown,
    forwardRef: ref,
    station,
    monitorType: SohTypes.SohMonitorType.LAG
  };

  const store = getStore();
  store.dispatch(
    dataAcquisitionActions.setSohStatus({
      isStale: false,
      lastUpdated: 0,
      loading: false,
      stationAndStationGroupSoh: undefined
    })
  );

  const { container } = render(
    <Provider store={store}>
      <BaseDisplayContext.Provider
        value={{
          glContainer: { width: 150, height: 150 } as any,
          widthPx: 150,
          heightPx: 150
        }}
      >
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <DeprecatedToolbar {...toolbarProps} />
      </BaseDisplayContext.Provider>
    </Provider>
  );
  it('should be defined', () => {
    expect(DeprecatedToolbar).toBeDefined();
  });
  it('should match snapshot', () => {
    expect(container).toMatchSnapshot();
  });
});
