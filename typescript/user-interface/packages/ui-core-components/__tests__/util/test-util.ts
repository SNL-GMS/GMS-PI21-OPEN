import { sleep } from '@gms/common-util';
import { act } from 'react-dom/test-utils';

const TIME_TO_WAIT_MS = 200;

/**
 * Fixes React warning that "An update to Component inside a test was not wrapped in act(...)."
 */
export const waitForComponentToPaint = async (wrapper: any): Promise<void> => {
  // eslint-disable-next-line @typescript-eslint/await-thenable
  await act(async () => {
    await sleep(TIME_TO_WAIT_MS);
    wrapper.update();
  });
};
