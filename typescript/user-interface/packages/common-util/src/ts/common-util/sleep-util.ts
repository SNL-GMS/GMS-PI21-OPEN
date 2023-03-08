/**
 * A generic sleep function, that will sleep for x amount of milliseconds
 * and allow a code block to be executed after the delay
 *
 * @param milliseconds number of milliseconds to sleep
 * @returns a generic promise
 */
export const sleep = async (milliseconds: number): Promise<void> =>
  new Promise(resolve => {
    setTimeout(resolve, milliseconds);
  });
