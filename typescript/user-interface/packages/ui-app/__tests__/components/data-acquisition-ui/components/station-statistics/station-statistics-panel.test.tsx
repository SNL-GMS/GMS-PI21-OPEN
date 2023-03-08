/* eslint-disable react/jsx-no-constructed-context-values */
import { uuid } from '@gms/common-util';
import { dataAcquisitionActions, FilterableSOHTypes, getStore } from '@gms/ui-state';
import { sohConfiguration } from '@gms/ui-state/__tests__/__data__/soh-configuration-query-data';
import React from 'react';
import { Provider } from 'react-redux';

import { BaseDisplayContext } from '../../../../../src/ts/components/common-ui/components/base-display';
// eslint-disable-next-line max-len
import { Columns } from '../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/column-definitions';
import type { StationStatisticsContextData } from '../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/station-statistics-context';
import { StationStatisticsContext } from '../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/station-statistics-context';
import type { StationStatisticsPanelProps } from '../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/station-statistics-panel';
import { StationStatisticsPanel } from '../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/station-statistics-panel';
import { stationAndStationGroupSohStatus } from '../../../../__data__/data-acquisition-ui/soh-overview-data';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

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

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

let idCount = 0;
// eslint-disable-next-line no-plusplus
uuid.asString = jest.fn().mockImplementation(() => ++idCount);

const contextValues: StationStatisticsContextData = {
  acknowledgeSohStatus: jest.fn(),
  selectedStationIds: ['H05N', 'H06N'],
  setSelectedStationIds: jest.fn(),
  quietTimerMs: 5000,
  updateIntervalSecs: 5,
  sohStationStaleTimeMS: 30000
};

describe('Station statistics class Panel', () => {
  // eslint-disable-next-line @typescript-eslint/no-magic-numbers
  Date.now = jest.fn().mockReturnValue(1573244087715);
  const store = getStore();
  store.dispatch(
    dataAcquisitionActions.setSohStatus({
      isStale: false,
      lastUpdated: 0,
      loading: false,
      stationAndStationGroupSoh: undefined
    })
  );

  const panelGuts = (
    <Provider store={store}>
      <StationStatisticsContext.Provider value={contextValues}>
        <BaseDisplayContext.Provider
          value={{
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            glContainer: { width: 150, height: 150 } as any,
            widthPx: 150,
            heightPx: 150
          }}
        >
          <StationStatisticsPanel
            stationGroups={stationAndStationGroupSohStatus.stationGroups}
            stationSohs={stationAndStationGroupSohStatus.stationSoh}
            groupSelected={undefined}
            setGroupSelected={jest.fn()}
            setStatusesToDisplay={jest.fn()}
            statusesToDisplay={{
              [FilterableSOHTypes.GOOD]: true,
              [FilterableSOHTypes.BAD]: true,
              [FilterableSOHTypes.MARGINAL]: true,
              [FilterableSOHTypes.NONE]: true
            }}
          />
        </BaseDisplayContext.Provider>
      </StationStatisticsContext.Provider>
    </Provider>
  );

  const sohPanel = Enzyme.mount(panelGuts).find(StationStatisticsPanel);
  const sohShallowPanel = Enzyme.shallow(panelGuts).find(StationStatisticsPanel);
  const props: StationStatisticsPanelProps = {
    stationGroups: stationAndStationGroupSohStatus.stationGroups,
    stationSohs: stationAndStationGroupSohStatus.stationSoh,
    groupSelected: undefined,
    setGroupSelected: jest.fn(),
    setStatusesToDisplay: jest.fn(),
    statusesToDisplay: {
      [FilterableSOHTypes.GOOD]: true,
      [FilterableSOHTypes.BAD]: true,
      [FilterableSOHTypes.MARGINAL]: true,
      [FilterableSOHTypes.NONE]: true
    }
  };
  const sohPanel2: any = new StationStatisticsPanel(props);
  it('should be defined', () => {
    expect(sohPanel).toBeDefined();
    expect(sohPanel2).toBeDefined();
  });

  it('should match snapshot', () => {
    expect(sohShallowPanel).toMatchSnapshot();
  });

  it('can handle onRowClicked', () => {
    const event: any = {
      api: {
        getSelectedRows: jest.fn(() => [{ id: 'test' }])
      }
    };
    sohPanel.instance().onRowClicked(event);
    expect(sohPanel2).toBeDefined();
  });

  it('can handle getEnvRollup', () => {
    const results = sohPanel2.getEnvRollup(stationAndStationGroupSohStatus.stationSoh[3]);
    expect(results).toMatchSnapshot();
  });

  it('can handle acknowledgeContextMenu', () => {
    const result = sohPanel.instance().acknowledgeContextMenu(['test', 'test2']);
    expect(result).toMatchSnapshot();
  });

  it('can handle cellDrop', () => {
    const event = {
      stationNames: ['test', 'test2'],
      context: contextValues
    };
    sohPanel.instance().cellDrop(event.stationNames, event.context);
    expect(sohPanel2).toBeDefined();
  });

  // TODO Unskip tests and fix
  it.skip('should have setIsHighlighted function', () => {
    sohPanel.instance().setIsHighlighted(true);
    expect(sohPanel.state().isHighlighted).toBe(true);
  });

  // TODO Unskip tests and fix
  it.skip('should have setGroupSelected function', () => {
    sohPanel.instance().setGroupSelected('test string');
    expect(sohPanel.state().groupSelected).toBe('test string');
  });

  // TODO Unskip tests and fix
  it.skip('should have setStatusesToDisplay function', () => {
    const myMap = new Map();
    sohPanel.instance().setStatusesToDisplay(myMap);
    expect(sohPanel.state().statusesToDisplay).toBe(myMap);
  });

  // TODO Unskip tests and fix
  it.skip('should have setColumnsToDisplay function', () => {
    const columnsToDisplay = new Map<Columns, boolean>();
    columnsToDisplay.set(Columns.ChannelLag, false);
    sohPanel.instance().setColumnsToDisplay(columnsToDisplay);
    expect(sohPanel.state().columnsToDisplay).toBe(columnsToDisplay);
  });

  it('should have toggleHighlight function', () => {
    sohPanel.instance().setIsHighlighted = jest.fn();
    sohPanel.instance().toggleHighlight({ ref: undefined });
    expect(sohPanel.instance().setIsHighlighted).toHaveBeenCalled();
  });
});
