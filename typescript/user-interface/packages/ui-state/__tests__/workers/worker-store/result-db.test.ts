import type { Result } from '../../../src/ts/workers/waveform-worker/worker-store/result-db';
import { ResultDB } from '../../../src/ts/workers/waveform-worker/worker-store/result-db';

describe('ResultDB', () => {
  describe('has', () => {
    it('provides a has function', () => {
      const rdb = new ResultDB('provides a has function');
      expect(rdb.has).toBeDefined();
    });
    it('returns true if it has a result with an ID that matches', async () => {
      const rdb = new ResultDB('returns true if it has a result with an ID that matches');
      const result: Result<string> = { id: '1', value: 'test1' };
      await rdb.results.add(result, result.id);
      expect(await rdb.has(result.id)).toBe(true);
      await rdb.results.clear();
    });
    it('returns false if it does not have a result with an ID that matches', async () => {
      const rdb = new ResultDB(
        'returns false if it does not have a result with an ID that matches'
      );
      const result: Result<string> = { id: '1', value: 'test1' };
      await rdb.results.add(result, result.id);
      expect(await rdb.has('not found')).toBe(false);
      await rdb.results.clear();
    });
  });
  describe('get', () => {
    it('returns the result matched by an id', async () => {
      const rdb = new ResultDB('returns the result matched by an id');
      const result: Result<string> = { id: '1', value: 'test1' };
      await rdb.results.add(result, result.id);
      expect(await rdb.get(result.id)).toMatchObject(result);
      await rdb.results.clear();
    });
    it('returns multiple results', async () => {
      const rdb = new ResultDB('returns multiple results');
      const result1: Result<string> = { id: '1', value: 'test1' };
      const result2: Result<string> = { id: '2', value: 'test2' };
      const result3: Result<string> = { id: '3', value: 'test3' };
      await rdb.results.add(result1, result1.id);
      await rdb.results.add(result2, result2.id);
      await rdb.results.add(result3, result3.id);
      expect(await rdb.get(result1.id)).toMatchObject(result1);
      expect(await rdb.get(result2.id)).toMatchObject(result2);
      expect(await rdb.get(result3.id)).toMatchObject(result3);
      // do it again so that we can be sure that things were left in a good state
      const result4: Result<string> = { id: '4', value: 'test4' };
      const result5: Result<string> = { id: '5', value: 'test5' };
      const result6: Result<string> = { id: '6', value: 'test6' };
      await rdb.results.add(result4, result4.id);
      await rdb.results.add(result5, result5.id);
      await rdb.results.add(result6, result6.id);
      expect(await rdb.get(result4.id)).toMatchObject(result4);
      expect(await rdb.get(result5.id)).toMatchObject(result5);
      expect(await rdb.get(result6.id)).toMatchObject(result6);
      await rdb.results.clear();
    });
    it('rejects promise if no result is found for the requested id', async () => {
      const rdb = new ResultDB('rejects promise if no result is found for the requested id');
      const result: Result<string> = { id: '1', value: 'test1' };
      await rdb.results.add(result, result.id);
      await expect(rdb.get('garbage')).rejects.toBe('No result found in resultDB for id garbage');
      await rdb.results.clear();
    });
  });
});
