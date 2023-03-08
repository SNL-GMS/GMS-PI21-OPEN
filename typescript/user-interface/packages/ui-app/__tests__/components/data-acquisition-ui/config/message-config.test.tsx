/* eslint-disable @typescript-eslint/no-magic-numbers */
import { messageConfig } from '../../../../src/ts/components/data-acquisition-ui/config/message-config';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('Message Config', () => {
  it('is exported', () => {
    expect(messageConfig).toBeDefined();
  });

  it('is config defined', () => {
    expect(messageConfig.labels.decimationDescription(50)).toEqual(
      'Displaying 50% of available points'
    );
    expect(JSON.stringify(messageConfig)).toMatchSnapshot();
  });
});
