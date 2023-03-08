/* eslint-disable no-promise-executor-return */
/* eslint-disable react/jsx-no-constructed-context-values */
import { EventTypes } from '@gms/common-model';
import type { EventStatus, SignalDetectionColumn, SignalDetectionFetchResult } from '@gms/ui-state';
import { getStore } from '@gms/ui-state';
import { signalDetectionsData } from '@gms/ui-state/__tests__/__data__';
import { render } from '@testing-library/react';
import { act, renderHook } from '@testing-library/react-hooks';
import type Immutable from 'immutable';
import cloneDeep from 'lodash/cloneDeep';
import * as React from 'react';
import { Provider } from 'react-redux';

import {
  sdPanelMemoCheck,
  SignalDetectionsTable,
  useUpdatedSignalDetectionSelection
} from '../../../../../src/ts/components/analyst-ui/components/signal-detections/table/signal-detections-table';
import {
  buildSignalDetectionRows,
  signalDetectionsColumnsToDisplay
} from '../../../../../src/ts/components/analyst-ui/components/signal-detections/table/signal-detections-table-utils';
import type {
  SignalDetectionRow,
  SignalDetectionsTableProps
} from '../../../../../src/ts/components/analyst-ui/components/signal-detections/types';
import { BaseDisplayContext } from '../../../../../src/ts/components/common-ui/components/base-display';
import { eventData } from '../../../../__data__/event-data';

const SDQueryResult: SignalDetectionFetchResult = {
  data: signalDetectionsData,
  isLoading: false,
  pending: 0,
  isError: false,
  fulfilled: 0,
  rejected: 0
};

const associatedEvent = cloneDeep(eventData);
const eventStatusesComplete: Record<string, EventStatus> = {
  '82ca9908-4272-4738-802b-f3d8f3099767': {
    stageId: { name: 'sample' },
    eventId: '82ca9908-4272-4738-802b-f3d8f3099767',
    eventStatusInfo: {
      eventStatus: EventTypes.EventStatus.COMPLETE,
      activeAnalystIds: ['user1', 'user2']
    }
  }
};
const currentIntervalMock = {
  startTimeSecs: 6000,
  endTimeSecs: 7000
};

const rowData = buildSignalDetectionRows(
  signalDetectionsData,
  [associatedEvent],
  eventStatusesComplete,
  '82ca9908-4272-4738-802b-f3d8f3099767',
  currentIntervalMock
);

describe('signal detections table', () => {
  const { container: withDataUnsynced } = render(
    <Provider store={getStore()}>
      <BaseDisplayContext.Provider
        value={{
          glContainer: {} as any,
          widthPx: 100,
          heightPx: 100
        }}
      >
        <SignalDetectionsTable
          signalDetectionsQuery={SDQueryResult}
          columnsToDisplay={signalDetectionsColumnsToDisplay}
          isSynced={false}
          data={rowData}
        />
      </BaseDisplayContext.Provider>
    </Provider>
  );

  const { container: withDataSynced } = render(
    <Provider store={getStore()}>
      <BaseDisplayContext.Provider
        value={{
          glContainer: {} as any,
          widthPx: 100,
          heightPx: 100
        }}
      >
        <SignalDetectionsTable
          signalDetectionsQuery={SDQueryResult}
          columnsToDisplay={signalDetectionsColumnsToDisplay}
          isSynced
          data={rowData}
        />
      </BaseDisplayContext.Provider>
    </Provider>
  );

  test('can mount', () => {
    expect(withDataUnsynced).toBeDefined();
    expect(SignalDetectionsTable).toBeDefined();
  });

  test('matches snapshot given data unsynced', () => {
    expect(withDataUnsynced).toMatchSnapshot();
  });
  test('matches snapshot given data synced', () => {
    expect(withDataSynced).toMatchSnapshot();
  });

  test('sets up correctly', async () => {
    await act(async () => {
      const waitDurationMs = 2000;
      await new Promise(resolve => setTimeout(resolve, waitDurationMs));
    });
    const { container: mountWithDataSynced } = render(
      <Provider store={getStore()}>
        <BaseDisplayContext.Provider
          value={{
            glContainer: {} as any,
            widthPx: 100,
            heightPx: 100
          }}
        >
          <SignalDetectionsTable
            signalDetectionsQuery={SDQueryResult}
            columnsToDisplay={signalDetectionsColumnsToDisplay}
            isSynced
            data={rowData}
          />
        </BaseDisplayContext.Provider>
      </Provider>
    );

    expect(mountWithDataSynced).toBeDefined();
  });

  describe('non-ideal state', () => {
    const SDQueryResultNoData: SignalDetectionFetchResult = {
      data: [],
      isLoading: false,
      pending: 0,
      isError: false,
      fulfilled: 0,
      rejected: 0
    };

    const SDQueryResultNoDataLoadingPending: SignalDetectionFetchResult = {
      data: [],
      isLoading: true,
      pending: 5,
      isError: false,
      fulfilled: 0,
      rejected: 0
    };

    const SDQueryResultNoDataLoading: SignalDetectionFetchResult = {
      data: [],
      isLoading: true,
      pending: 0,
      isError: false,
      fulfilled: 0,
      rejected: 0
    };
    const SDQueryResultError: SignalDetectionFetchResult = {
      data: [],
      isLoading: false,
      pending: 0,
      isError: true,
      fulfilled: 0,
      rejected: 0
    };

    const { container: SDQueryResultNoDataLoadingPendingWrapper } = render(
      <Provider store={getStore()}>
        <BaseDisplayContext.Provider
          value={{
            glContainer: {} as any,
            widthPx: 100,
            heightPx: 100
          }}
        >
          <SignalDetectionsTable
            signalDetectionsQuery={SDQueryResultNoDataLoadingPending}
            columnsToDisplay={signalDetectionsColumnsToDisplay}
            isSynced={false}
            data={rowData}
          />
        </BaseDisplayContext.Provider>
      </Provider>
    );
    const { container: SDQueryResultNoDataLoadingWrapper } = render(
      <Provider store={getStore()}>
        <BaseDisplayContext.Provider
          value={{
            glContainer: {} as any,
            widthPx: 100,
            heightPx: 100
          }}
        >
          <SignalDetectionsTable
            signalDetectionsQuery={SDQueryResultNoDataLoading}
            columnsToDisplay={signalDetectionsColumnsToDisplay}
            isSynced={false}
            data={rowData}
          />
        </BaseDisplayContext.Provider>
      </Provider>
    );
    const { container: SDQueryResultErrorWrapper } = render(
      <Provider store={getStore()}>
        <BaseDisplayContext.Provider
          value={{
            glContainer: {} as any,
            widthPx: 100,
            heightPx: 100
          }}
        >
          <SignalDetectionsTable
            signalDetectionsQuery={SDQueryResultError}
            columnsToDisplay={signalDetectionsColumnsToDisplay}
            isSynced={false}
            data={rowData}
          />
        </BaseDisplayContext.Provider>
      </Provider>
    );

    const { container: noDataUnsyncedWrapper } = render(
      <Provider store={getStore()}>
        <BaseDisplayContext.Provider
          value={{
            glContainer: {} as any,
            widthPx: 100,
            heightPx: 100
          }}
        >
          <SignalDetectionsTable
            signalDetectionsQuery={SDQueryResultNoData}
            columnsToDisplay={signalDetectionsColumnsToDisplay}
            isSynced={false}
            data={rowData}
          />
        </BaseDisplayContext.Provider>
      </Provider>
    );
    const { container: noDataSyncedWrapper } = render(
      <Provider store={getStore()}>
        <BaseDisplayContext.Provider
          value={{
            glContainer: {} as any,
            widthPx: 100,
            heightPx: 100
          }}
        >
          <SignalDetectionsTable
            signalDetectionsQuery={SDQueryResultNoData}
            columnsToDisplay={signalDetectionsColumnsToDisplay}
            isSynced
            data={rowData}
          />
        </BaseDisplayContext.Provider>
      </Provider>
    );
    test('no data Synced', () => {
      expect(noDataSyncedWrapper).toMatchSnapshot();
    });
    test('no data UNSynced', () => {
      expect(noDataUnsyncedWrapper).toMatchSnapshot();
    });
    test('no data loading and pending', () => {
      expect(SDQueryResultNoDataLoadingPendingWrapper).toMatchSnapshot();
    });

    test('no data loading not pending', () => {
      expect(SDQueryResultNoDataLoadingWrapper).toMatchSnapshot();
    });
    test('no data error', () => {
      expect(SDQueryResultErrorWrapper).toMatchSnapshot();
    });
  });
  describe('useUpdatedSignalDetectionSelection', () => {
    it('works if there is a table ref api', () => {
      const tableRef = {
        current: {
          api: {
            forEachNode: jest.fn()
          } as any
        }
      } as any;
      act(() => {
        renderHook(() => useUpdatedSignalDetectionSelection(tableRef, ['TEST', 'TEST2']));
      });
      expect(tableRef.current.api.forEachNode).toHaveBeenCalled();
    });
  });
});

describe('signal detections memo check', () => {
  const prevProps: SignalDetectionsTableProps = {
    isSynced: false,
    signalDetectionsQuery: SDQueryResult,
    data: rowData,
    columnsToDisplay: signalDetectionsColumnsToDisplay
  };
  it('is defined', () => {
    const newProps = cloneDeep(prevProps);
    expect(sdPanelMemoCheck(prevProps, newProps)).toBeDefined();
  });
  it('isError has changed', () => {
    expect(
      sdPanelMemoCheck(prevProps, {
        isSynced: false,
        signalDetectionsQuery: {
          data: signalDetectionsData,
          isLoading: false,
          pending: 0,
          isError: true,
          fulfilled: 0,
          rejected: 0
        },
        data: rowData,
        columnsToDisplay: signalDetectionsColumnsToDisplay
      })
    ).toBeFalsy();
  });

  it('isLoading has changed', () => {
    expect(
      sdPanelMemoCheck(prevProps, {
        isSynced: false,
        signalDetectionsQuery: {
          data: signalDetectionsData,
          isLoading: true,
          pending: 0,
          isError: false,
          fulfilled: 0,
          rejected: 0
        },
        data: rowData,
        columnsToDisplay: signalDetectionsColumnsToDisplay
      })
    ).toBeFalsy();
  });
  //
  it('pending has changed', () => {
    expect(
      sdPanelMemoCheck(prevProps, {
        isSynced: false,
        signalDetectionsQuery: {
          data: signalDetectionsData,
          isLoading: false,
          pending: 1,
          isError: false,
          fulfilled: 0,
          rejected: 0
        },
        data: rowData,
        columnsToDisplay: signalDetectionsColumnsToDisplay
      })
    ).toBeFalsy();
  });
  it('query data length has changed', () => {
    expect(
      sdPanelMemoCheck(prevProps, {
        isSynced: false,
        signalDetectionsQuery: {
          data: [],
          isLoading: false,
          pending: 0,
          isError: false,
          fulfilled: 0,
          rejected: 0
        },
        data: rowData,
        columnsToDisplay: signalDetectionsColumnsToDisplay
      })
    ).toBeFalsy();
  });
  it('rowData has changed', () => {
    expect(
      sdPanelMemoCheck(prevProps, {
        isSynced: false,
        signalDetectionsQuery: SDQueryResult,
        data: [] as SignalDetectionRow[],
        columnsToDisplay: signalDetectionsColumnsToDisplay
      })
    ).toBeFalsy();
  });
  it('synced state has changed', () => {
    expect(
      sdPanelMemoCheck(prevProps, {
        isSynced: true,
        signalDetectionsQuery: SDQueryResult,
        data: rowData,
        columnsToDisplay: signalDetectionsColumnsToDisplay
      })
    ).toBeFalsy();
  });
  it('columnsToDisplay has changed', () => {
    expect(
      sdPanelMemoCheck(prevProps, {
        isSynced: false,
        signalDetectionsQuery: SDQueryResult,
        data: rowData,
        columnsToDisplay: {} as Immutable.Map<SignalDetectionColumn, boolean>
      })
    ).toBeFalsy();
  });
});
