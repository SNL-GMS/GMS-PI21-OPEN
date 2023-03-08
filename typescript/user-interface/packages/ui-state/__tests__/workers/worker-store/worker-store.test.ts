import { sleep } from '@gms/common-util';

import { WorkerStore } from '../../../src/ts/workers/waveform-worker/worker-store';

let ws: WorkerStore<unknown>;

// eslint-disable-next-line @typescript-eslint/no-magic-numbers
const TIMEOUT_DURATION = 200 as const;

function flushPromises(): any {
  return sleep(0);
}

const sleepAndReleaseExecution = async () => {
  await new Promise<void>(resolve => {
    setTimeout(resolve, TIMEOUT_DURATION);
  });
  await flushPromises();
};

describe('WorkerStore', () => {
  // Necessary to support retries for retrieve
  // eslint-disable-next-line @typescript-eslint/no-magic-numbers
  jest.setTimeout(10000);

  beforeEach(() => {
    ws = new WorkerStore('test-store');
  });

  afterEach(async () => {
    await ws.cleanup();
    ws = null;
  });

  it('exposes store, retrieve, and remove functions', () => {
    expect(ws.store).toBeDefined();
    expect(ws.delete).toBeDefined();
    expect(ws.retrieve).toBeDefined();
  });

  it('can store a promise and retrieve the data', async () => {
    const testString = 'Test storage and retrieval';
    const promise = Promise.resolve(testString);
    const id = 'storeId';
    await ws.store(id, promise);
    const result = await ws.retrieve(id);
    expect(result).toBe(testString);
  });

  it('will resolve a stored promise if given time', async () => {
    const testString = 'Test promise resolution and retrieval';
    const promise = Promise.resolve(testString);
    const id = 'resolvePromiseId';
    await ws.store(id, promise);
    await sleepAndReleaseExecution();
    const result = await ws.retrieve(id);
    expect(result).toBe(testString);
  });

  it('should return undefined after deletion', async () => {
    const testString = 'Test deletion';
    const promise = Promise.resolve(testString);
    const id = 'deletionId';
    await ws.store(id, promise);
    await ws.delete(id);
    const result = await ws.retrieve(id);
    expect(result).toBeUndefined();
  });

  it('can check to see if a promise was set with an id', async () => {
    const testString = 'Test has';
    const promise = Promise.resolve(testString);
    const id = 'hasId';
    await ws.store(id, promise);
    expect(await ws.has(id)).toBe(true);
  });

  it('can check to see if it has resolved data that was set with an id', async () => {
    const testString = 'Test has';
    const promise = Promise.resolve(testString);
    const id = 'hasId';
    await ws.store(id, promise);
    await sleepAndReleaseExecution();
    expect(await ws.has(id)).toBe(true);
  });

  it('should know that an ID was removed after deletion', async () => {
    const testString = 'Test deletion lookup';
    const promise = Promise.resolve(testString);
    const id = 'deletionLookupId';
    await ws.store(id, promise);
    await ws.delete(id);
    expect(await ws.has(id)).toBe(false);
  });

  it('cleans up after itself', async () => {
    const testString = 'Test cleanup';
    const promise = Promise.resolve(testString);
    const id1 = 'cleanupId1';
    await ws.store(id1, promise);
    await sleepAndReleaseExecution();
    const id2 = 'cleanupId2';
    await ws.store(id2, promise);
    expect(await ws.has(id1)).toBe(true);
    expect(await ws.has(id2)).toBe(true);
    await ws.cleanup();
    expect(await ws.has(id1)).toBe(false);
    expect(await ws.has(id2)).toBe(false);
  });

  it('handles gracefully if a timeout fires after a retrieve has already been called', async () => {
    const testString = 'Test timeout after retrieval';
    const promise = Promise.resolve(testString);
    const id = 'timeoutId';
    await ws.store(id, promise);
    // retrieving will force the promise to resolve.
    const result = await ws.retrieve(id);
    expect(result).toBe(testString);
    await sleepAndReleaseExecution();
    const result2 = await ws.retrieve(id);
    expect(result2).toBe(testString);
  });
});
