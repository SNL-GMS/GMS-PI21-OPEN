import type { StationDeselectHandlerProps } from '../../../../../src/ts/components/data-acquisition-ui/shared/table/station-deselect-handler';
import { keyDown } from '../../../../../src/ts/components/data-acquisition-ui/shared/table/station-deselect-handler';

describe('Station deselect handler', () => {
  const dropZoneProps: StationDeselectHandlerProps = {
    className: 'test',
    dataCy: 'test',
    setSelectedStationIds: jest.fn()
  };
  it('is defined', () => {
    const event: any = {
      nativeEvent: {
        code: 'Escape'
      }
    };
    const result = keyDown(event, dropZoneProps);
    expect(result).toBeUndefined();
  });
});
