import { EventTypes } from '@gms/common-model';
import type { Table } from '@gms/ui-core-components';
import type { EventStatus } from '@gms/ui-state';

import {
  buildEventRow,
  updateRowSelection
} from '../../../../../src/ts/components/analyst-ui/components/events/events-util';
import type { EventRow } from '../../../../../src/ts/components/analyst-ui/components/events/types';
import { eventData } from '../../../../__data__/event-data';

const getTableApi = () => ({
  forEachNode: jest.fn()
});
const tableRef = {
  current: {
    getTableApi
  }
};
const selectedEvents = ['event1', 'event2', 'event3'];

describe('Events util', () => {
  it('builds a row correctly', () => {
    const eventStatuses: Record<string, EventStatus> = {
      '82ca9908-4272-4738-802b-f3d8f3099767': {
        stageId: { name: 'sample' },
        eventId: '82ca9908-4272-4738-802b-f3d8f3099767',
        eventStatusInfo: {
          eventStatus: EventTypes.EventStatus.IN_PROGRESS,
          activeAnalystIds: ['user1', 'user2']
        }
      }
    };

    expect(
      buildEventRow(
        eventData.id,
        eventData.eventHypotheses[0],
        eventData.eventHypotheses[0].locationSolutions[0].id,
        { startTimeSecs: 0, endTimeSecs: 100 },
        eventStatuses,
        false
      )
    ).toEqual({
      activeAnalysts: ['user1', 'user2'],
      confidenceSemiMajorAxis: 0,
      confidenceSemiMinorAxis: 0,
      conflict: false,
      coverageSemiMajorAxis: undefined,
      coverageSemiMinorAxis: undefined,
      coverageSemiMajorAxisTrend: undefined,
      depthKm: 1.1,
      edgeEventType: 'Interval',
      id: '82ca9908-4272-4738-802b-f3d8f3099767',
      isOpen: false,
      latitudeDegrees: 1.1,
      longitudeDegrees: 1.1,
      magnitudeMb: 1.1,
      magnitudeMl: undefined,
      magnitudeMs: undefined,
      preferred: 'TBD',
      region: 'TBD',
      status: 'IN_PROGRESS',
      time: 0,
      rejected: 'False'
    });
  });

  test('updateRowSelection returns tableRef', () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const result = updateRowSelection(tableRef as any, selectedEvents);
    expect(result).toEqual(tableRef);
  });

  test('updateRowSelection returns null', () => {
    const emptyTableRef = {};
    let result = updateRowSelection(
      emptyTableRef as React.MutableRefObject<Table<EventRow, unknown>>,
      selectedEvents
    );
    expect(result).toBeNull();
    result = updateRowSelection(null, selectedEvents);
    expect(result).toBeNull();
  });
});
