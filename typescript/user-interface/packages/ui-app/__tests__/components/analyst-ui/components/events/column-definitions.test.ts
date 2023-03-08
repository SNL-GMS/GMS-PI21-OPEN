/* eslint-disable @typescript-eslint/no-unused-vars */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import type {
  CellRendererParams,
  TooltipParams,
  ValueFormatterParams
} from '@gms/ui-core-components';
import { EventsColumn } from '@gms/ui-state';
import Immutable from 'immutable';

import { getColumnDefs } from '../../../../../src/ts/components/analyst-ui/components/events/column-definitions';
import type { EventValueGetterParams } from '../../../../../src/ts/components/analyst-ui/components/events/columns/active-analysts';
import {
  activeAnalystsColumnDef,
  activeAnalystsTooltipValueGetter,
  activeAnalystsValueFormatter,
  activeAnalystsValueGetter
} from '../../../../../src/ts/components/analyst-ui/components/events/columns/active-analysts';
import { confidenceSemiMajorColumnDef } from '../../../../../src/ts/components/analyst-ui/components/events/columns/confidence-semi-major';
import { confidenceSemiMinorColumnDef } from '../../../../../src/ts/components/analyst-ui/components/events/columns/confidence-semi-minor';
import { conflictColumnDef } from '../../../../../src/ts/components/analyst-ui/components/events/columns/conflict-marker';
import { coverageSemiMajorColumnDef } from '../../../../../src/ts/components/analyst-ui/components/events/columns/coverage-semi-major';
import { coverageSemiMinorColumnDef } from '../../../../../src/ts/components/analyst-ui/components/events/columns/coverage-semi-minor';
import { depthColumnDef } from '../../../../../src/ts/components/analyst-ui/components/events/columns/depth';
import { dirtyDotColumnDef } from '../../../../../src/ts/components/analyst-ui/components/events/columns/dirty-dot';
import { latitudeColumnDef } from '../../../../../src/ts/components/analyst-ui/components/events/columns/latitude';
import { longitudeColumnDef } from '../../../../../src/ts/components/analyst-ui/components/events/columns/longitude';
import { mbColumnDef } from '../../../../../src/ts/components/analyst-ui/components/events/columns/mb';
import { mlColumnDef } from '../../../../../src/ts/components/analyst-ui/components/events/columns/ml';
import { msColumnDef } from '../../../../../src/ts/components/analyst-ui/components/events/columns/ms';
import { preferredColumnDef } from '../../../../../src/ts/components/analyst-ui/components/events/columns/preferred';
import { regionColumnDef } from '../../../../../src/ts/components/analyst-ui/components/events/columns/region';
import { statusColumnDef } from '../../../../../src/ts/components/analyst-ui/components/events/columns/status';
import { timeColumnDef } from '../../../../../src/ts/components/analyst-ui/components/events/columns/time';
import type { EventRow } from '../../../../../src/ts/components/analyst-ui/components/events/types';
import { dummyData } from './event-table-mock-data';

const columnFilterMap = Immutable.Map(
  Object.values(EventsColumn)
    // all columns are visible by default
    .map(v => [v, true])
);

const columnFilterMapWithCoverage = columnFilterMap
  .set(EventsColumn.confidenceSemiMajorAxis, false)
  .set(EventsColumn.confidenceSemiMinorAxis, false);
const columnFilterMapWithConfidence = columnFilterMap
  .set(EventsColumn.coverageSemiMajorAxis, false)
  .set(EventsColumn.coverageSemiMinorAxis, false);
const columnFilterMapWithNoGroups = columnFilterMapWithConfidence
  .set(EventsColumn.coverageSemiMajorAxis, false)
  .set(EventsColumn.coverageSemiMinorAxis, false);
describe('ConflictMarkerCellRenderer', () => {
  it('is exported', () => {
    expect(activeAnalystsValueGetter).toBeDefined();
    expect(activeAnalystsTooltipValueGetter).toBeDefined();
    expect(activeAnalystsValueFormatter).toBeDefined();
    expect(activeAnalystsColumnDef).toBeDefined();
    expect(confidenceSemiMajorColumnDef).toBeDefined();
    expect(confidenceSemiMinorColumnDef).toBeDefined();
    expect(conflictColumnDef).toBeDefined();
    expect(coverageSemiMajorColumnDef).toBeDefined();
    expect(coverageSemiMinorColumnDef).toBeDefined();
    expect(depthColumnDef).toBeDefined();
    expect(dirtyDotColumnDef).toBeDefined();
    expect(latitudeColumnDef).toBeDefined();
    expect(longitudeColumnDef).toBeDefined();
    expect(mbColumnDef).toBeDefined();
    expect(mlColumnDef).toBeDefined();
    expect(msColumnDef).toBeDefined();
    expect(preferredColumnDef).toBeDefined();
    expect(regionColumnDef).toBeDefined();
    expect(timeColumnDef).toBeDefined();
    expect(statusColumnDef).toBeDefined();
    expect(getColumnDefs).toBeDefined();
  });

  it('has column definitions', () => {
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    expect(getColumnDefs(columnFilterMap)).toHaveLength(18);
    expect(getColumnDefs(columnFilterMap)).toMatchSnapshot();
  });

  it('can render coverage group', () => {
    expect(getColumnDefs(columnFilterMapWithCoverage)).toMatchSnapshot();
  });

  it('can render confidence group', () => {
    expect(getColumnDefs(columnFilterMapWithConfidence)).toMatchSnapshot();
  });

  it('can render with no groups', () => {
    expect(getColumnDefs(columnFilterMapWithNoGroups)).toMatchSnapshot();
  });

  it('has active analysts value getters', () => {
    const params: EventValueGetterParams = {
      getValue(field: string) {
        dummyData.activeAnalysts.join(', ');
      },
      node: undefined,
      data: dummyData,
      colDef: undefined,
      column: undefined,
      api: undefined,
      columnApi: undefined,
      context: undefined
    };

    const formattedParams: ValueFormatterParams<
      EventRow,
      unknown,
      string,
      CellRendererParams<any, any, any, any, any>,
      unknown
    > = {
      value: '',
      node: undefined,
      data: undefined,
      colDef: undefined,
      column: undefined,
      api: undefined,
      columnApi: undefined,
      context: undefined
    };

    const tooltipParams: TooltipParams = {
      ...params,
      location: 'cell'
    };

    params.data = { ...params.data, activeAnalysts: undefined };

    expect(activeAnalystsValueGetter(params)).toMatchInlineSnapshot(`undefined`);
    expect(activeAnalystsValueFormatter(formattedParams)).toMatchInlineSnapshot(`""`);

    params.data = { ...params.data, activeAnalysts: [] };

    expect(activeAnalystsValueGetter(params)).toMatchInlineSnapshot(`""`);
    expect(activeAnalystsValueFormatter(formattedParams)).toMatchInlineSnapshot(`""`);

    params.data = { ...params.data, activeAnalysts: ['Chillas'] };

    expect(activeAnalystsValueGetter(params)).toMatchInlineSnapshot(`"Chillas"`);
    expect(activeAnalystsValueFormatter(formattedParams)).toMatchInlineSnapshot(`""`);

    params.data = { ...params.data, activeAnalysts: dummyData.activeAnalysts };

    expect(activeAnalystsValueGetter(params)).toMatchInlineSnapshot(
      `"Chillas, Echidnas, I&T, Platform, SMEs"`
    );
    expect(activeAnalystsValueFormatter(formattedParams)).toMatchInlineSnapshot(`""`);
    expect(activeAnalystsTooltipValueGetter(tooltipParams)).toMatchInlineSnapshot(`undefined`);
  });
  it('has data formatters', () => {
    const formatterParams: any = {
      data: dummyData,
      api: undefined,
      colDef: undefined,
      column: undefined,
      addRenderedRowListener: undefined,
      columnApi: undefined,
      context: undefined,
      eGridCell: undefined,
      eParentOfValue: undefined,
      node: undefined,
      value: dummyData.activeAnalysts.join(', '),
      refreshCell: undefined,
      rowIndex: undefined,
      formatValue: undefined,
      getValue: undefined,
      setValue: undefined,
      valueFormatted: undefined,
      $scope: undefined
    };

    const mockSetDecimalPrecision = jest.fn(() => '0.0');

    jest.mock('@gms/ui-core-components', () => ({
      setDecimalPrecision: mockSetDecimalPrecision
    }));

    let testValue = '';

    // lat
    const latColDefFormatter = getColumnDefs(columnFilterMap)[3].valueFormatter;
    if (typeof latColDefFormatter === 'function') {
      testValue = latColDefFormatter(formatterParams);
    }
    expect(testValue).toMatch('41.343');

    // lon
    const lonDefFormatter = getColumnDefs(columnFilterMap)[4].valueFormatter;
    if (typeof lonDefFormatter === 'function') {
      testValue = lonDefFormatter(formatterParams);
    }
    expect(testValue).toMatch('129.036');

    // depth
    const depthDefFormatter = getColumnDefs(columnFilterMap)[5].valueFormatter;
    if (typeof depthDefFormatter === 'function') {
      testValue = depthDefFormatter(formatterParams);
    }
    expect(testValue).toMatch('0.611');

    // mb
    const mbDefFormatter = getColumnDefs(columnFilterMap)[6].valueFormatter;
    if (typeof mbDefFormatter === 'function') {
      testValue = mbDefFormatter(formatterParams);
    }
    expect(testValue).toMatch('5.2');

    // ms
    const msDefFormatter = getColumnDefs(columnFilterMap)[7].valueFormatter;
    if (typeof msDefFormatter === 'function') {
      testValue = msDefFormatter(formatterParams);
    }
    expect(testValue).toMatch('4.9');

    // ml
    const mlDefFormatter = getColumnDefs(columnFilterMap)[8].valueFormatter;
    if (typeof mlDefFormatter === 'function') {
      testValue = mlDefFormatter(formatterParams);
    }
    expect(testValue).toMatch('5.0');

    // coverage major axis
    const coverageSemiMajorDefFormatter = getColumnDefs(columnFilterMap)[9].valueFormatter;
    if (typeof coverageSemiMajorDefFormatter === 'function') {
      testValue = coverageSemiMajorDefFormatter(formatterParams);
    }
    expect(testValue).toMatch('820.24');

    // coverage minor axis
    const coverageSemiMinorDefFormatter = getColumnDefs(columnFilterMap)[10].valueFormatter;
    if (typeof coverageSemiMinorDefFormatter === 'function') {
      testValue = coverageSemiMinorDefFormatter(formatterParams);
    }
    expect(testValue).toMatch('677.49');

    // confidence major axis
    const confidenceSemiMajorDefFormatter = getColumnDefs(columnFilterMap)[11].valueFormatter;
    if (typeof confidenceSemiMajorDefFormatter === 'function') {
      testValue = confidenceSemiMajorDefFormatter(formatterParams);
    }
    expect(testValue).toMatch('120.25');

    // confidence minor axis
    const confidenceSemiMinorDefFormatter = getColumnDefs(columnFilterMap)[12].valueFormatter;
    if (typeof confidenceSemiMinorDefFormatter === 'function') {
      testValue = confidenceSemiMinorDefFormatter(formatterParams);
    }
    expect(testValue).toMatch('67.41');
  });

  it('has filter value getters', () => {
    const formatterParams: any = {
      data: dummyData,
      api: undefined,
      colDef: undefined,
      column: undefined,
      addRenderedRowListener: undefined,
      columnApi: undefined,
      context: undefined,
      eGridCell: undefined,
      eParentOfValue: undefined,
      node: undefined,
      value: dummyData.activeAnalysts.join(', '),
      refreshCell: undefined,
      rowIndex: undefined,
      formatValue: undefined,
      getValue: undefined,
      setValue: undefined,
      valueFormatted: undefined,
      $scope: undefined
    };

    const mockSetDecimalPrecision = jest.fn(() => '0.0');

    jest.mock('@gms/ui-core-components', () => ({
      setDecimalPrecision: mockSetDecimalPrecision
    }));

    let testValue = '';

    // lat
    const latColDefFormatter = getColumnDefs(columnFilterMap)[3].filterValueGetter;
    if (typeof latColDefFormatter === 'function') {
      testValue = latColDefFormatter(formatterParams);
    }
    expect(testValue).toMatch('41.343');

    // lon
    const lonDefFormatter = getColumnDefs(columnFilterMap)[4].filterValueGetter;
    if (typeof lonDefFormatter === 'function') {
      testValue = lonDefFormatter(formatterParams);
    }
    expect(testValue).toMatch('129.036');

    // depth
    const depthDefFormatter = getColumnDefs(columnFilterMap)[5].filterValueGetter;
    if (typeof depthDefFormatter === 'function') {
      testValue = depthDefFormatter(formatterParams);
    }
    expect(testValue).toMatch('0.611');

    // mb
    const mbDefFormatter = getColumnDefs(columnFilterMap)[6].filterValueGetter;
    if (typeof mbDefFormatter === 'function') {
      testValue = mbDefFormatter(formatterParams);
    }
    expect(testValue).toMatch('5.2');

    // ms
    const msDefFormatter = getColumnDefs(columnFilterMap)[7].valueFormatter;
    if (typeof msDefFormatter === 'function') {
      testValue = msDefFormatter(formatterParams);
    }
    expect(testValue).toMatch('4.9');

    // ml
    const mlDefFormatter = getColumnDefs(columnFilterMap)[8].filterValueGetter;
    if (typeof mlDefFormatter === 'function') {
      testValue = mlDefFormatter(formatterParams);
    }
    expect(testValue).toMatch('5.0');

    // coverage major axis
    const coverageSemiMajorDefFormatter = getColumnDefs(columnFilterMap)[9].filterValueGetter;
    if (typeof coverageSemiMajorDefFormatter === 'function') {
      testValue = coverageSemiMajorDefFormatter(formatterParams);
    }
    expect(testValue).toMatch('820.24');

    // coverage minor axis
    const coverageSemiMinorDefFormatter = getColumnDefs(columnFilterMap)[10].filterValueGetter;
    if (typeof coverageSemiMinorDefFormatter === 'function') {
      testValue = coverageSemiMinorDefFormatter(formatterParams);
    }
    expect(testValue).toMatch('677.49');

    // confidence major axis
    const confidenceSemiMajorDefFormatter = getColumnDefs(columnFilterMap)[11].filterValueGetter;
    if (typeof confidenceSemiMajorDefFormatter === 'function') {
      testValue = confidenceSemiMajorDefFormatter(formatterParams);
    }
    expect(testValue).toMatch('120.25');

    // confidence minor axis
    const confidenceSemiMinorDefFormatter = getColumnDefs(columnFilterMap)[12].filterValueGetter;
    if (typeof confidenceSemiMinorDefFormatter === 'function') {
      testValue = confidenceSemiMinorDefFormatter(formatterParams);
    }
    expect(testValue).toMatch('67.41');
  });
});
