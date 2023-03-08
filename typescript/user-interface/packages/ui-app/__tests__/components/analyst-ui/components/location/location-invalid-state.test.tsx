import type { TableInvalidStateProps } from '../../../../../src/ts/components/analyst-ui/common/utils/table-invalid-state';
import {
  DataType,
  TableDataState,
  TableInvalidState
} from '../../../../../src/ts/components/analyst-ui/common/utils/table-invalid-state';

describe('Location invalid state tests', () => {
  it('Renders invalid state for no sds', () => {
    const noSdProps: TableInvalidStateProps = {
      message: TableDataState.NO_SDS,
      dataType: DataType.SD
    };
    const noSDInvalid = TableInvalidState(noSdProps);
    expect(noSDInvalid).toMatchSnapshot();
  });
  it('Renders invalid state for no events loaded', () => {
    const noEventProps: TableInvalidStateProps = {
      message: TableDataState.NO_EVENTS,
      dataType: DataType.EVENT
    };
    const noEventInvalid = TableInvalidState(noEventProps);
    expect(noEventInvalid).toMatchSnapshot();
  });
  it('Renders invalid state for no event opened', () => {
    const noEventOpen: TableInvalidStateProps = {
      message: TableDataState.NO_EVENT_OPEN,
      dataType: DataType.SD,
      noEventMessage: 'Select an event to refine location'
    };
    const noEventOpenNode = TableInvalidState(noEventOpen);
    expect(noEventOpenNode).toMatchSnapshot();
  });
  it('Renders invalid state for no open interval', () => {
    const noOpenInterval: TableInvalidStateProps = {
      message: TableDataState.NO_INTERVAL,
      dataType: DataType.EVENT
    };
    const noOpenIntervalNode = TableInvalidState(noOpenInterval);
    expect(noOpenIntervalNode).toMatchSnapshot();
  });
});
