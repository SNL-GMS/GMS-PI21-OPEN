import {
  initStoragePersistence,
  persistStorage
} from '../../../src/ts/workers/waveform-worker/util/storage-util';

jest.mock('@gms/ui-util', () => {
  const mockPersist = jest.fn(async () => Promise.resolve(true));
  const mockPersisted = jest.fn(async () => Promise.resolve(true));
  // const mockQuery = jest.fn(async () => Promise.resolve(true));
  const actual = jest.requireActual('@gms/ui-util');
  // do this in jest.mock so that it is defined early enough
  Object.defineProperty(navigator, 'storage', {
    value: {
      persist: mockPersist,
      persisted: mockPersisted
    },
    // This is necessary so we can overwrite this
    configurable: true
  });
  // do this in jest.mock so that it is defined early enough
  Object.defineProperty(navigator, 'permissions', {
    value: {
      query: undefined
    },
    // This is necessary so we can overwrite this
    configurable: true
  });
  // this is a no-op. We just use this mock to do some super hoisting so we can mock things early enough
  return {
    ...actual
  };
});

describe('Storage Util', () => {
  describe('persistStorage', () => {
    it('calls persist if the api is available', async () => {
      const persistSpy = jest
        .spyOn(navigator.storage, 'persist')
        .mockReturnValue(Promise.resolve(true));
      expect(await persistStorage()).toBe(true);
      expect(persistSpy).toHaveBeenCalledTimes(1);
    });
    it('rejects if the api is not available', async () => {
      Object.defineProperty(navigator, 'storage', {
        value: {
          persist: undefined,
          persisted: jest.fn(async () => Promise.resolve(true))
        },
        // This is necessary so we can overwrite this
        configurable: true
      });
      await expect(persistStorage).rejects.toMatchObject(
        new Error('Persistence API not supported')
      );
    });
  });
  describe('initStoragePersistence', () => {
    beforeEach(() => {
      Object.defineProperty(navigator, 'storage', {
        value: {
          persist: jest.fn(async () => Promise.resolve(true)),
          persisted: jest.fn(async () => Promise.resolve(true))
        },
        // This is necessary so we can overwrite this
        configurable: true
      });
      // do this in jest.mock so that it is defined early enough
      Object.defineProperty(navigator, 'permissions', {
        value: {
          query: undefined
        },
        // This is necessary so we can overwrite this
        configurable: true
      });
    });
    it('logs a warning if persistent storage is not possible', async () => {
      const loggerSpy = jest.spyOn(console, 'warn');
      Object.defineProperty(navigator, 'storage', {
        value: {
          persist: undefined
        },
        configurable: true
      });
      await initStoragePersistence();
      expect(loggerSpy).toHaveBeenCalledWith(
        'GMS_LOG_WORKER_STORE Not possible to persist storage'
      );
    });
    it('logs "Successfully persisted storage silently" if storage is persisted', async () => {
      const loggerSpy = jest.spyOn(console, 'info');
      jest.spyOn(navigator.storage, 'persisted').mockReturnValue(Promise.resolve(true));
      await initStoragePersistence();
      expect(loggerSpy).toHaveBeenCalledWith(
        'GMS_LOG_WORKER_STORE Successfully persisted storage silently'
      );
    });
    it('calls persist to request persistence if navigator.permissions.query is not defined', async () => {
      const loggerSpy = jest.spyOn(console, 'info');
      const persistSpy = jest
        .spyOn(navigator.storage, 'persist')
        .mockReturnValue(Promise.resolve(true));
      jest.spyOn(navigator.storage, 'persisted').mockReturnValue(Promise.resolve(false));
      Object.defineProperty(navigator, 'permissions', {
        value: {
          query: undefined
        },
        configurable: true
      });
      await initStoragePersistence();
      expect(loggerSpy).toHaveBeenCalledWith(
        'GMS_LOG_WORKER_STORE Not persisted, but we may prompt user when we want to'
      );
      expect(persistSpy).toHaveBeenCalledTimes(1);
    });
    it('logs success and calls persist if permission is granted', async () => {
      const loggerSpy = jest.spyOn(console, 'info');
      const persistSpy = jest
        .spyOn(navigator.storage, 'persist')
        .mockReturnValue(Promise.resolve(true));
      jest.spyOn(navigator.storage, 'persisted').mockReturnValue(Promise.resolve(false));
      Object.defineProperty(navigator, 'permissions', {
        value: {
          query: jest.fn().mockReturnValue({ state: 'granted' })
        },
        configurable: true
      });
      await initStoragePersistence();
      expect(loggerSpy).toHaveBeenCalledWith(
        'GMS_LOG_WORKER_STORE Successfully persisted storage silently'
      );
      expect(persistSpy).toHaveBeenCalledTimes(1);
    });
    it('logs that will prompt and calls persist if permission is "prompt"', async () => {
      const loggerSpy = jest.spyOn(console, 'info');
      Object.defineProperty(navigator, 'storage', {
        value: {
          persist: jest.fn(async () => Promise.resolve(true)),
          persisted: jest.fn(async () => Promise.resolve(false))
        },
        configurable: true
      });
      const persistSpy = jest
        .spyOn(navigator.storage, 'persist')
        .mockReturnValue(Promise.resolve(true));
      jest.spyOn(navigator.storage, 'persisted').mockReturnValue(Promise.resolve(false));
      Object.defineProperty(navigator, 'permissions', {
        value: {
          query: jest.fn().mockReturnValue({ state: 'prompt' })
        },
        configurable: true
      });
      await initStoragePersistence();
      expect(loggerSpy).toHaveBeenCalledWith(
        'GMS_LOG_WORKER_STORE Not persisted, but we may prompt user when we want to'
      );
      expect(persistSpy).toHaveBeenCalledTimes(1);
    });
  });
});
