import { UILogger } from '@gms/ui-util';

const logger = UILogger.create('GMS_LOG_WORKER_STORE', process.env.GMS_LOG_WORKER_STORE);

type PersistRequestResult = 'never' | 'persisted' | 'prompt' | 'persistent-storage';

/** Tries to convert to persisted storage.
 *
  @returns Promise resolved with true if successfully
  persisted the storage, false if not, and undefined if the API is not present.
 */
export async function persistStorage(): Promise<boolean> {
  if (navigator.storage && navigator.storage.persist) {
    return navigator.storage.persist();
  }
  return Promise.reject(new Error('Persistence API not supported'));
}

/** Tries to persist storage without ever prompting user.
 *
  @returns
    "never" In case persisting is not ever possible. Caller don't bother
      asking user for permission.
    "prompt" In case persisting would be possible if prompting user first.
    "persisted" In case this call successfully silently persisted the storage,
      or if it was already persisted.
 */
export async function tryPersistWithoutPromptingUser(): Promise<PersistRequestResult> {
  if (!navigator.storage || !navigator.storage.persisted) {
    return 'never';
  }
  let persisted = await navigator.storage.persisted();
  if (persisted) {
    return 'persisted';
  }
  if (!navigator.permissions || !navigator.permissions.query) {
    return 'prompt'; // It MAY be successful to prompt. Don't know.
  }
  const permission = await navigator.permissions.query({
    name: 'persistent-storage'
  });
  if (permission.state === 'granted') {
    persisted = await navigator.storage.persist();
    if (persisted) {
      return 'persisted';
    }
    throw new Error('Failed to persist even though permission was granted');
  }
  if (permission.state === 'prompt') {
    return 'prompt';
  }
  return 'never';
}

/**
 * Attempts to persist storage silently. Logs the results.
 *
 * @throws if the persistence api is not supported, or if permission is granted, but persistence fails
 */
export async function initStoragePersistence() {
  const persist = await tryPersistWithoutPromptingUser();
  switch (persist) {
    case 'never':
      logger.warn('Not possible to persist storage');
      break;
    case 'persisted':
      logger.info('Successfully persisted storage silently');
      break;
    case 'prompt':
      logger.info('Not persisted, but we may prompt user when we want to');
      await persistStorage();
      break;
    default:
      throw new Error(`Invalid persistence response ${persist}`);
  }
}
