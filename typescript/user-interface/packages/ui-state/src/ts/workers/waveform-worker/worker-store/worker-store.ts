import type { Logger } from '@gms/common-util';
import { isPromise } from '@gms/common-util';
import { UILogger } from '@gms/ui-util';
import Immutable from 'immutable';

import { ResultDB } from './result-db';

/**
 * A WorkerStore is a cache that stores promises, resolves the promise, and caches that result.
 * When a promise is stored, the WorkerStore will enqueue a function call to resolve the promise.
 * When a promise is retrieved, the WorkerStore will return the result of the promise. If the
 * promise is not yet resolved when the retrieve method is called, then the worker will await that
 * result and return it.
 */
export class WorkerStore<StoredType = unknown> {
  private readonly logger: Logger;

  /**
   * The number of bytes to at which point to clear the in memory cache, if using a browser
   * that supports performance.memory. See {@link https://caniuse.com/mdn-api_performance_memory}
   */
  // eslint-disable-next-line @typescript-eslint/no-magic-numbers
  private readonly MEMORY_CLEAR_THRESHOLD = 250 * 1000 ** 2;

  private readonly MAX_RETRIES = 6;

  /**
   * This is where the results are stored.
   */
  private readonly resultDB: ResultDB<StoredType>;

  /**
   * This is the in-memory location where the results are stored.
   * If not found in here, get them from resultDB instead.
   *
   * The idea is that we could clear this out if we have memory pressure.
   */
  private inMemResultMap = Immutable.Map<string, StoredType>();

  /**
   * This is where the promises are stored.
   */
  private promiseMap = Immutable.Map<string, Promise<StoredType> | PromiseLike<StoredType>>();

  /**
   * A map of strings to timeout handles. Used to clear timeouts if deletion happens
   * before the timeout has resolved.
   */
  private timeoutMap: Immutable.Map<string, ReturnType<typeof setTimeout>> = Immutable.Map();

  /**
   * Creates a new WorkerStore
   *
   * @param storeID a string uniquely identifying this store
   */
  public constructor(storeID: string) {
    this.resultDB = new ResultDB(storeID);
    this.logger = UILogger.create(storeID, process.env.GMS_WORKER_STORE_LOGGER);
  }

  /**
   * Stores the provided object with the given ID. If the ID was already stored, this will
   * overwrite the previous stored value.
   * When given a promise, this will store the promise until it resolves, at which point
   * it will store the result.
   * This ID may be used to retrieve the stored boundaries (even if a promise was given).
   *
   * @param id the key that uniquely identifies this data
   * @param valueOrPromise either a value to store, or a promise or PromiseLike object that can be resolved
   * for the data to store. If given a value, will simply store it. If given a promise, will store the promise
   * and resolve it asynchronously
   */
  public readonly store = async (
    id: string,
    valueOrPromise: Promise<StoredType> | PromiseLike<StoredType> | StoredType
  ): Promise<void> => {
    if (isPromise(valueOrPromise)) {
      this.promiseMap = this.promiseMap.set(id, valueOrPromise);
      // Set timeout with time of 0ms to enqueue this right away.
      const timeoutHandle = setTimeout(async () => {
        if (this.promiseMap.has(id)) {
          await this.resolvePromiseAndStore(id);
        }
      }, 0);
      this.timeoutMap = this.timeoutMap.set(id, timeoutHandle);
    } else {
      await this.storeToMemAndDB({ id, value: valueOrPromise });
    }
  };

  /**
   * Checks to see if this store has a result or promise with the given ID.
   *
   * @param id the string identifying the data
   * @returns whether the store contains either a promise or the result
   */
  public readonly has = async (id: string): Promise<boolean> => {
    if (this.inMemResultMap.has(id) || this.promiseMap.has(id)) {
      return Promise.resolve(true);
    }
    return this.resultDB.has(id);
  };

  /**
   * Deletes the object and/or promise represented by this key.
   *
   * @param id the id to delete
   */
  public readonly delete = async (id: string): Promise<void> => {
    const timeoutHandle = this.timeoutMap.get(id);
    if (timeoutHandle !== undefined) {
      this.cleanupTimeout(id);
    }
    await this.resultDB.results.delete(id);
    this.promiseMap = this.promiseMap.delete(id);
  };

  /**
   * Cancels timeouts and deletes internal storage. Calling this is destructive,
   * and should only be used to clean up after this store when it is no longer needed.
   */
  public readonly cleanup = async (): Promise<void> => {
    this.timeoutMap.forEach((timeout, key) => {
      this.cleanupTimeout(key);
    });
    await this.resultDB.results.clear();
    this.promiseMap.forEach((val, key) => {
      this.promiseMap = this.promiseMap.remove(key);
    });
    this.inMemResultMap = this.inMemResultMap.clear();
  };

  /**
   * Retrieves an object out of storage.
   *
   * This is asynchronous because it may be waiting on a promise to resolve.
   * If that is the case, this retrieve method will wait for the data to return, and return that.
   *
   * @param id the claim check string that was provided when the data was stored.
   * @returns a promise for the data that was stored with the given id, or undefined.
   */
  public readonly retrieve = async (id: string, depth = 0): Promise<StoredType> => {
    // First try in memory result map, which is fastest
    const inMemValue = this.inMemResultMap.get(id);
    if (inMemValue) {
      return inMemValue;
    }

    // Second, try the ResultDB, which will check indexedDB
    const result = await this.resultDB.results.get(id);
    if (result) {
      this.inMemResultMap.set(id, result.value);
      return result.value;
    }

    // Finally, resolve the promise, since the fact that we reached this point means that no result has
    // yet been calculated
    if (this.promiseMap.has(id)) {
      return this.resolvePromiseAndStore(id);
    }

    // If we reached here, there is no match for the id
    // since other tabs might still be processing a promise for this id, we schedule a retry
    return new Promise(resolve => {
      if (depth > this.MAX_RETRIES) {
        this.logger.error('No value found for claim check', id);
        resolve(undefined);
      }
      setTimeout(() => {
        resolve(this.retrieve(id, depth + 1));
      }, depth ** 2 * 100); // back off with increasing amount of time
    });
  };

  private readonly cleanupTimeout = (id: string) => {
    clearTimeout(this.timeoutMap.get(id));
    this.timeoutMap = this.timeoutMap.remove(id);
  };

  /**
   * Not supported on all browsers, so we must guard. If we have this API, use it!
   *
   * @returns whether we are within the configured threshold at which point we should clear our memory.
   */
  private readonly shouldClearMemCache = () =>
    Object.prototype.hasOwnProperty.call(performance, 'memory')
      ? (performance as any).memory.jsHeapSizeLimit - (performance as any).memory.usedJSHeapSize <
        this.MEMORY_CLEAR_THRESHOLD
      : false;

  /**
   * Store a result in memory and in the ResultDB. If nearing the heap limit, this will clear the in-memory
   * cache in browsers that support the performance.memory API. See {@link https://caniuse.com/mdn-api_performance_memory}
   *
   * @param id the unique string to use to refer to the value
   * @param value the value to store
   */
  private readonly storeToMemAndDB = async ({ id, value }: { id: string; value: StoredType }) => {
    if (this.shouldClearMemCache()) {
      this.inMemResultMap = this.inMemResultMap.clear();
    }
    this.inMemResultMap = this.inMemResultMap.set(id, value);
    await this.resultDB.results.put({ id, value });
  };

  /**
   * Resolves a stored promise, and removes that promise from this.promiseMap.
   * Stores the result of that promise in the in-memory cache and in the ResultDB.
   *
   * @param id the id of the promise to resolve
   * @returns the value returned by the promise
   */
  private readonly resolvePromiseAndStore = async (id: string): Promise<StoredType> => {
    const p = this.promiseMap.get(id);
    const value = await p;
    await this.storeToMemAndDB({ id, value });
    this.promiseMap = this.promiseMap.remove(id);
    this.cleanupTimeout(id);
    return value;
  };
}
