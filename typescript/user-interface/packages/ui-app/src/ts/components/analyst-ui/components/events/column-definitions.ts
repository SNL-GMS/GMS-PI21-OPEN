import type { ColumnDefinition } from '@gms/ui-core-components';
import type { EventsColumn } from '@gms/ui-state/lib/app/state/events';
import type Immutable from 'immutable';

import { activeAnalystsColumnDef } from './columns/active-analysts';
import { confidenceSemiMajorColumnDef } from './columns/confidence-semi-major';
import { confidenceSemiMinorColumnDef } from './columns/confidence-semi-minor';
import { conflictColumnDef } from './columns/conflict-marker';
import { coverageSemiMajorColumnDef } from './columns/coverage-semi-major';
import { coverageSemiMinorColumnDef } from './columns/coverage-semi-minor';
import { depthColumnDef } from './columns/depth';
import { dirtyDotColumnDef } from './columns/dirty-dot';
import { latitudeColumnDef } from './columns/latitude';
import { longitudeColumnDef } from './columns/longitude';
import { mbColumnDef } from './columns/mb';
import { mlColumnDef } from './columns/ml';
import { msColumnDef } from './columns/ms';
import { preferredColumnDef } from './columns/preferred';
import { regionColumnDef } from './columns/region';
import { rejectedColumnDef } from './columns/rejected';
import { statusColumnDef } from './columns/status';
import { timeColumnDef } from './columns/time';
import type { EventRow } from './types';

export const getColumnDefs = (
  columnsToDisplayMap: Immutable.Map<EventsColumn, boolean>
): ColumnDefinition<EventRow, unknown, any, unknown, unknown>[] => {
  return [
    dirtyDotColumnDef,
    conflictColumnDef(columnsToDisplayMap),
    timeColumnDef(columnsToDisplayMap),
    latitudeColumnDef(columnsToDisplayMap),
    longitudeColumnDef(columnsToDisplayMap),
    depthColumnDef(columnsToDisplayMap),
    mbColumnDef(columnsToDisplayMap),
    msColumnDef(columnsToDisplayMap),
    mlColumnDef(columnsToDisplayMap),
    coverageSemiMajorColumnDef(columnsToDisplayMap),
    coverageSemiMinorColumnDef(columnsToDisplayMap),
    confidenceSemiMajorColumnDef(columnsToDisplayMap),
    confidenceSemiMinorColumnDef(columnsToDisplayMap),
    regionColumnDef(columnsToDisplayMap),
    activeAnalystsColumnDef(columnsToDisplayMap),
    preferredColumnDef(columnsToDisplayMap),
    statusColumnDef(columnsToDisplayMap),
    rejectedColumnDef(columnsToDisplayMap)
  ];
};
