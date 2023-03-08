import { UILogger } from '@gms/ui-util';
import Dexie from 'dexie';
import clone from 'lodash/clone';

const logger = UILogger.create('GMS_LOG_RESULT_DB', process.env.GMS_LOG_RESULT_DB);

/**
 * The type of value stored in an instance of @class ResultDB.
 * The value must be supported by the structured clone algorithm.
 *
 * @see {@link https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API/Structured_clone_algorithm}
 */
export interface Result<T> {
  /**
   * The key for this result
   */
  id: string;

  /**
   * The value to store. Must be serializable by the structured clone algorithm.
   *
   * @see {@link https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API/Structured_clone_algorithm}
   */
  value: T;
}

/**
 * An instance of Dexie that caches the results of some expensive operation. Uses IndexedDB as the backing store.
 *
 * The generic @param StoredType is used to set the type of the stored values.
 * The type of object stored must be supported by the structured clone algorithm in order to be stored with indexedDB.
 *
 * @see {@link https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API/Structured_clone_algorithm}
 */
export class ResultDB<StoredType> extends Dexie {
  /**
   * The internal Dexie table, which may be interacted with directly, following a pattern from their examples.
   * This is set in the constructor.
   */
  public readonly results: Dexie.Table<Result<StoredType>, string>;

  private readonly BATCH_DEBOUNCE_DELAY_MS = 50;

  private idQueue: string[] = [];

  private resolutions: { id: string; resolve: (value: Result<StoredType>) => void }[] = [];

  private rejections: { id: string; reject: (reason: unknown) => void }[] = [];

  private getAction;

  public constructor(databaseId: string) {
    super(databaseId);
    this.version(1).stores({
      results: 'id'
    });
    this.open().catch(err => {
      logger.error(`Failed to open db: ${err.stack || err}`);
    });
  }

  /**
   * Calls bulkGet on the internal resultsDB for all ids in this.idQueue, and resolves
   * all stored promises associated with those ids
   */
  private readonly getAll = async () => {
    try {
      /**
       * Capture the values of the IDs and resolvers before we call await
       */
      const idQueue = clone(this.idQueue);
      const res = clone(this.resolutions);
      const rej = clone(this.rejections);
      /**
       * Reset these before calling await to avoid race conditions
       */
      this.idQueue = [];
      this.resolutions = [];
      this.rejections = [];
      this.getAction = undefined;

      /**
       * Perform the lookup
       */
      const results = await this.results.bulkGet(idQueue);

      /**
       * Resolve each promise that we captured above
       */
      res.forEach(({ id, resolve }, index) => {
        // resolve the promise associated with this id
        if (typeof resolve !== 'function') {
          throw new Error(`ResultDB failed to resolve id ${id}`);
        } else {
          const result = results[index];
          if (!result) {
            rej[index].reject(`No result found in resultDB for id ${id}`);
          } else {
            resolve(result);
          }
        }
      });
    } catch (e) {
      logger.error('ResultDB failed calling getAll', e);
      throw e;
    }
  };

  /**
   * Async function that will eventually return the result of an indexedDB lookup
   * from the internal IndexedDB instance. This uses a batch lookup, so it may be faster
   * than calling dbInstance.results.get, where dbInstance is the instance of the ResultDB
   * class. In other cases, this may be slower, so performance testing is advised.
   *
   * Batch lookups are performed at a configured interval, set by this.BATCH_DEBOUNCE_DELAY_MS
   *
   * @param id the string used to identify the result stored
   * @returns a promise for a result matched by the key, if found.
   */
  public readonly get = async (id: string) => {
    // store the ID for eventual lookup
    this.idQueue.push(id);

    // if there is not already a call to getAll scheduled, then schedule one
    if (!this.getAction) {
      this.getAction = setTimeout(this.getAll, this.BATCH_DEBOUNCE_DELAY_MS);
    }

    return new Promise(
      (resolve: (val: Result<StoredType>) => void, reject: (reason: any) => void) => {
        // store the reject function for the promise, which gets called if no match is found with
        // the requested id.
        this.rejections.push({ id, reject });
        // store the resolver for the promise, which will get called when the batch lookup occurs
        // since this promise executor is synchronous, this resolver is guaranteed to be stored.
        this.resolutions.push({ id, resolve });
      }
    );
  };

  /**
   * Promise that resolves to true if the id provided matches a value in the ResultDB instance.
   *
   * @param id the string that is used to identify the value being checked
   */
  public readonly has = async (id: string): Promise<boolean> => {
    const count = await this.results.where('id').equals(id).count();
    return !!count;
  };
}
