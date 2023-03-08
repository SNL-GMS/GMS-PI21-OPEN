/* eslint-disable @typescript-eslint/no-magic-numbers */
/* eslint-disable no-return-assign */
import { EventTypes } from '@gms/common-model';
import type { AgGridReact, RowNode } from '@gms/ui-core-components';
import type { EventStatus } from '@gms/ui-state';
import { signalDetectionsData } from '@gms/ui-state/__tests__/__data__';
import cloneDeep from 'lodash/cloneDeep';

import { EdgeTypes } from '../../../../../src/ts/components/analyst-ui/components/events/types';
import {
  agGridDoesExternalFilterPass,
  agGridIsExternalFilterPresent,
  buildSignalDetectionRow,
  buildSignalDetectionRows,
  formatRectilinearityOrEmergenceForDisplay,
  getEdgeType,
  signalDetectionsColumnsToDisplay,
  updateColumns,
  updateRowSelection
} from '../../../../../src/ts/components/analyst-ui/components/signal-detections/table/signal-detections-table-utils';
import { messageConfig } from '../../../../../src/ts/components/analyst-ui/config/message-config';
import { setRowNodeSelection } from '../../../../../src/ts/components/common-ui/common/table-utils';
import { eventData } from '../../../../__data__/event-data';

describe('Signal Detection Table Utils', () => {
  const currentIntervalMock = {
    startTimeSecs: 6000,
    endTimeSecs: 7000
  };

  it('are defined', () => {
    expect(formatRectilinearityOrEmergenceForDisplay).toBeDefined();
    expect(buildSignalDetectionRows).toBeDefined();
    expect(buildSignalDetectionRow).toBeDefined();
    expect(updateRowSelection).toBeDefined();
    expect(signalDetectionsColumnsToDisplay).toBeDefined();
    expect(getEdgeType).toBeDefined();
  });

  describe('getEdgeType', () => {
    it('returns invalid cell text with poorly defined interval', () => {
      const badIntervalMock = {
        startTimeSecs: undefined,
        endTimeSecs: undefined
      };
      const edgeResult = getEdgeType(badIntervalMock, 6459);
      expect(edgeResult).toEqual(messageConfig.invalidCellText);
    });
    it('returns invalid cell text with undefined interval', () => {
      const edgeResult = getEdgeType(undefined, 6459);
      expect(edgeResult).toEqual(messageConfig.invalidCellText);
    });
    it('returns invalid cell text with poorly defined time', () => {
      const edgeResult = getEdgeType(currentIntervalMock, undefined);
      expect(edgeResult).toEqual(messageConfig.invalidCellText);
    });
    it('returns before', () => {
      const edgeResult = getEdgeType(currentIntervalMock, 0);
      expect(edgeResult).toEqual(EdgeTypes.BEFORE);
    });
    it('returns after', () => {
      const edgeResult = getEdgeType(currentIntervalMock, 9001);
      expect(edgeResult).toEqual(EdgeTypes.AFTER);
    });
    it('returns during', () => {
      const edgeResult = getEdgeType(currentIntervalMock, 6565);
      expect(edgeResult).toEqual(EdgeTypes.INTERVAL);
    });
  });

  describe('agGridIsExternalFilterPresent', () => {
    it('Given all true values, returns false', () => {
      const filterState = {
        syncWaveform: false,
        signalDetectionBeforeInterval: true,
        signalDetectionAfterInterval: true,
        signalDetectionAssociatedToOpenEvent: true,
        signalDetectionAssociatedToCompletedEvent: true,
        signalDetectionAssociatedToOtherEvent: true,
        signalDetectionUnassociated: true
      };
      const result = agGridIsExternalFilterPresent(filterState);
      expect(result).toBe(false);
    });
    it('Filter SDs associated with OpenEvent, returns true', () => {
      const filterState = {
        syncWaveform: false,
        signalDetectionBeforeInterval: true,
        signalDetectionAfterInterval: true,
        signalDetectionAssociatedToOpenEvent: true,
        signalDetectionAssociatedToCompletedEvent: false,
        signalDetectionAssociatedToOtherEvent: false,
        signalDetectionUnassociated: false
      };
      const result = agGridIsExternalFilterPresent(filterState);
      expect(result).toBe(true);
    });
    it('Filter SDs associated with CompletedEvent, returns true', () => {
      const filterState = {
        syncWaveform: false,
        signalDetectionBeforeInterval: true,
        signalDetectionAfterInterval: true,
        signalDetectionAssociatedToOpenEvent: false,
        signalDetectionAssociatedToCompletedEvent: true,
        signalDetectionAssociatedToOtherEvent: false,
        signalDetectionUnassociated: false
      };
      const result = agGridIsExternalFilterPresent(filterState);
      expect(result).toBe(true);
    });
    it('Filter SDs associated with OtherEvent, returns true', () => {
      const filterState = {
        syncWaveform: false,
        signalDetectionBeforeInterval: true,
        signalDetectionAfterInterval: true,
        signalDetectionAssociatedToOpenEvent: false,
        signalDetectionAssociatedToCompletedEvent: false,
        signalDetectionAssociatedToOtherEvent: true,
        signalDetectionUnassociated: false
      };
      const result = agGridIsExternalFilterPresent(filterState);
      expect(result).toBe(true);
    });
    it('Filter unassociated SDs, returns true', () => {
      const filterState = {
        syncWaveform: false,
        signalDetectionBeforeInterval: true,
        signalDetectionAfterInterval: true,
        signalDetectionAssociatedToOpenEvent: false,
        signalDetectionAssociatedToCompletedEvent: false,
        signalDetectionAssociatedToOtherEvent: false,
        signalDetectionUnassociated: true
      };
      const result = agGridIsExternalFilterPresent(filterState);
      expect(result).toBe(true);
    });
  });

  describe('agGridDoesExternalFilterPass', () => {
    const filterState = {
      syncWaveform: false,
      signalDetectionBeforeInterval: true,
      signalDetectionAfterInterval: true,
      signalDetectionAssociatedToOpenEvent: true,
      signalDetectionAssociatedToCompletedEvent: true,
      signalDetectionAssociatedToOtherEvent: true,
      signalDetectionUnassociated: true
    };
    const associatedEvent = cloneDeep(eventData);
    const detection = cloneDeep(signalDetectionsData[0]);
    // associate  the detection to the event
    associatedEvent.eventHypotheses[0].associatedSignalDetectionHypotheses.push(
      detection.signalDetectionHypotheses[0]
    );

    it('Filters OPEN_ASSOCIATED event association', () => {
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
      const rowNode = buildSignalDetectionRow(
        signalDetectionsData[0],
        [associatedEvent],
        eventStatusesComplete,
        '82ca9908-4272-4738-802b-f3d8f3099767',
        currentIntervalMock
      );
      const result = agGridDoesExternalFilterPass({ data: rowNode } as RowNode, filterState);
      expect(result).toBe(true);
    });

    it('Filters COMPLETE_ASSOCIATED event association', () => {
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
      const rowNode = buildSignalDetectionRow(
        signalDetectionsData[0],
        [associatedEvent],
        eventStatusesComplete,
        '',
        currentIntervalMock
      );
      const result = agGridDoesExternalFilterPass({ data: rowNode } as RowNode, filterState);
      expect(result).toBe(true);

      const newFilterState = cloneDeep(filterState);
      newFilterState.signalDetectionAssociatedToCompletedEvent = false;

      const result2 = agGridDoesExternalFilterPass({ data: rowNode } as RowNode, newFilterState);
      expect(result2).toBe(false);
    });

    it('Filters out COMPLETE_ASSOCIATED event association if it is an edge event', () => {
      const newFilterState = cloneDeep(filterState);
      newFilterState.signalDetectionBeforeInterval = false;
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
      const rowNode = buildSignalDetectionRow(
        signalDetectionsData[0],
        [associatedEvent],
        eventStatusesComplete,
        '',
        currentIntervalMock
      );

      rowNode.edgeType = EdgeTypes.BEFORE;
      const result = agGridDoesExternalFilterPass({ data: rowNode } as RowNode, newFilterState);
      expect(result).toBe(false);

      rowNode.edgeType = EdgeTypes.AFTER;
      const resultAfter = agGridDoesExternalFilterPass(
        { data: rowNode } as RowNode,
        newFilterState
      );
      expect(resultAfter).toBe(true);
    });

    it('Filters OTHER_ASSOCIATED event association', () => {
      const eventStatusesInProgress: Record<string, EventStatus> = {
        '82ca9908-4272-4738-802b-f3d8f3099767': {
          stageId: { name: 'sample' },
          eventId: '82ca9908-4272-4738-802b-f3d8f3099767',
          eventStatusInfo: {
            eventStatus: EventTypes.EventStatus.IN_PROGRESS,
            activeAnalystIds: ['user1', 'user2']
          }
        }
      };
      const rowNode = buildSignalDetectionRow(
        signalDetectionsData[0],
        [associatedEvent],
        eventStatusesInProgress,
        '',
        currentIntervalMock
      );
      const result = agGridDoesExternalFilterPass({ data: rowNode } as RowNode, filterState);
      expect(result).toBe(true);
    });
  });

  describe('formatRectilinearityOrEmergenceForDisplay', () => {
    it('Given -1, returns Unknown', () => {
      const result = formatRectilinearityOrEmergenceForDisplay(-1);
      expect(result).toEqual('Unknown');
    });
    it('Given null returns messageConfig.invalidCellText', () => {
      const result = formatRectilinearityOrEmergenceForDisplay(-1);
      expect(result).toEqual(messageConfig.invalidCellText);
    });
    it('Given undefined, returns messageConfig.invalidCellText', () => {
      const result = formatRectilinearityOrEmergenceForDisplay(-1);
      expect(result).toEqual(messageConfig.invalidCellText);
    });
    it('Given a number, returns a string containing that number with three decimal point precision', () => {
      const result = formatRectilinearityOrEmergenceForDisplay(234.2342);
      expect(result).toEqual('234.234');
    });
  });

  describe('buildRows', () => {
    const associatedEvent = cloneDeep(eventData);
    const detection = cloneDeep(signalDetectionsData[0]);
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

    it('returns an empty array with empty input', () => {
      const result = buildSignalDetectionRows([], [], null, '', currentIntervalMock);
      expect(result).toEqual([]);
    });
    it('returns an empty array with null input', () => {
      const result = buildSignalDetectionRows(null, [], null, '', currentIntervalMock);
      expect(result).toEqual([]);
    });
    it('returns an empty array with undefined input', () => {
      const result = buildSignalDetectionRows(undefined, [], null, '', currentIntervalMock);
      expect(result).toEqual([]);
    });
    it('returns rows when given SD data', () => {
      // associate  the detection to the event
      associatedEvent.eventHypotheses[0].associatedSignalDetectionHypotheses.push(
        detection.signalDetectionHypotheses[0]
      );
      const result = buildSignalDetectionRows(
        signalDetectionsData,
        [associatedEvent],
        eventStatusesComplete,
        '82ca9908-4272-4738-802b-f3d8f3099767',
        currentIntervalMock
      );
      expect(result).toMatchSnapshot();
    });
  });

  describe('updateColumns', () => {
    test('returns undefined with no tableref', () => {
      const result = updateColumns(undefined, signalDetectionsColumnsToDisplay);
      expect(result).toBeUndefined();
    });
    test('returns undefined with no tableref.current', () => {
      const result = updateColumns({ current: null }, signalDetectionsColumnsToDisplay);
      expect(result).toBeUndefined();
    });
    test('returns columns when provided tableref.current', () => {
      const result = updateColumns(
        { current: { columnApi: { setColumnVisible: jest.fn() } } } as any,
        signalDetectionsColumnsToDisplay
      );
      expect(result).toEqual(signalDetectionsColumnsToDisplay);
    });
  });

  describe('updateRowSelection', () => {
    const tableRef = { current: { api: { forEachNode: jest.fn(n => n.map) } } };
    const selectedSdIds = ['ASAR', 'RASAR', 'RACECAR', 'LASAR'];

    test('updates rows', () => {
      updateRowSelection(
        (tableRef as unknown) as React.MutableRefObject<AgGridReact>,
        selectedSdIds
      );
    });
  });
  describe('updateRowNodeSelection', () => {
    const rowNode = { selected: true, setSelected: jest.fn(s => (rowNode.selected = s)) };

    test('updates row to true', () => {
      const result = setRowNodeSelection((rowNode as unknown) as RowNode, true) as any;
      expect(result.selected).toEqual(true);
    });

    test('updates row if false', () => {
      const result = setRowNodeSelection((rowNode as unknown) as RowNode, false) as any;
      expect(result.selected).toEqual(false);
    });
  });
});
