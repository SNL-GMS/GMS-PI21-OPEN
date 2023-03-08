/* eslint-disable @typescript-eslint/no-unused-vars */
import { uuid, ValueType } from '@gms/common-util';
import type { PercentBarProps } from '@gms/ui-core-components';
import { PercentBar } from '@gms/ui-core-components';
import { UILogger } from '@gms/ui-util';
import { render } from '@testing-library/react';
import React from 'react';

import type {
  NameCellProps,
  SohCellRendererProps
} from '../../../../../src/ts/components/data-acquisition-ui/shared/table/soh-cell-renderers';
import {
  formatSohValue,
  NameCell,
  SohRollupCell
} from '../../../../../src/ts/components/data-acquisition-ui/shared/table/soh-cell-renderers';
import {
  CellStatus,
  DataReceivedStatus
} from '../../../../../src/ts/components/data-acquisition-ui/shared/table/utils';

const logger = UILogger.create(
  'GMS_SOH_CELL_RENDERERS_TEST',
  process.env.GMS_SOH_CELL_RENDERERS_TEST
);

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

uuid.asString = jest.fn().mockReturnValue('1e872474-b19f-4325-9350-e217a6feddc0');

/**
 * Tests the ability to check if the peak trough is in warning
 */
describe('Station cell renderers', () => {
  // start formatSohDetailValue function tests
  test('formatSohDetailValue function can detect non-numbers', () => {
    expect(formatSohValue('NaN' as any, ValueType.PERCENTAGE, true)).toEqual('Unknown');
  });

  test('formatSohDetailValue function correctly formats input number', () => {
    const inputNumber = 999;
    expect(formatSohValue(inputNumber, ValueType.PERCENTAGE, true)).toEqual('999.00');
  });
  // end formatSohDetailValue function tests

  // start Percentage bar pure component tests
  const percentBarProps: PercentBarProps = {
    percentage: 10
  };
  // eslint-disable-next-line react/jsx-props-no-spreading
  const percentBar = Enzyme.mount(<PercentBar {...percentBarProps} />);
  // TODO redo test in RTL
  test('PercentBar should be defined', () => {
    expect(percentBar).toBeDefined();
  });
  test('PercentBar should not have state', () => {
    expect(percentBar.state()).toEqual(null);
  });
  test('PercentBar should have props of type PercentBarProps', () => {
    expect(percentBar.props()).toEqual({ percentage: 10 });
  });
  test('PercentBar match snapshot', () => {
    expect(percentBar).toMatchSnapshot();
  });
  // end Percentage bar tests

  // start StationCellRenderer function component tests
  const stationCellRendererProps: SohCellRendererProps = {
    className: 'string',
    stationId: 'string',
    value: 'string',
    dataReceivedStatus: DataReceivedStatus.RECEIVED,
    cellStatus: CellStatus.GOOD
  };
  const nameCellProps: NameCellProps = {
    value: 'name',
    valueFormatted: 'NAME',
    data: undefined,
    node: undefined,
    name: 'test',
    colDef: undefined,
    api: undefined,
    columnApi: undefined,
    context: undefined,
    getValue: jest.fn(),
    setValue: jest.fn(),
    formatValue: jest.fn(),
    column: undefined,
    rowIndex: 1,
    refreshCell: undefined,
    eGridCell: undefined,
    eParentOfValue: undefined,
    cellStatus: CellStatus.GOOD,
    dataReceivedStatus: DataReceivedStatus.RECEIVED,
    registerRowDragger(
      rowDraggerElement: HTMLElement,
      dragStartPixels?: number,
      value?: string
    ): void {
      logger.info('registerRowDragger function not implemented');
    }
  };
  // eslint-disable-next-line react/jsx-props-no-spreading
  const stationCellRenderer = Enzyme.mount(<SohRollupCell {...stationCellRendererProps} />);
  // eslint-disable-next-line react/jsx-props-no-spreading
  const nameCell = Enzyme.mount(<NameCell {...nameCellProps} />);
  // TODO redo test in RTL
  it('stationCellRenderer should be defined', () => {
    expect(stationCellRenderer).toBeDefined();
  });
  it('NameCell should be defined', () => {
    expect(NameCell).toBeDefined();
  });
  it('stationCellRenderer should have props of type stationCellRendererProps', () => {
    expect(stationCellRenderer.props()).toMatchSnapshot();
  });

  it('nameCell should have props of type nameCellProps', () => {
    expect(nameCell.props()).toMatchSnapshot();
  });

  it('stationCellRenderer should match snapshot', () => {
    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<SohRollupCell {...stationCellRendererProps} />);
    expect(container).toMatchSnapshot();
  });
  // end StationCellRenderer function component tests
});
