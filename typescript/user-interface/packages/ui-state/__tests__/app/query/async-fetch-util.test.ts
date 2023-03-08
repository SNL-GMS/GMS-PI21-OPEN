import { hasAlreadyBeenRequested } from '../../../src/ts/app/query/async-fetch-util';
import type { AsyncFetchHistoryEntry } from '../../../src/ts/app/query/types';
import { AsyncActionStatus } from '../../../src/ts/app/query/types';

interface TestArg {
  name: string;
}

const requests: Record<string, AsyncFetchHistoryEntry<TestArg>> = {};
requests['0'] = { arg: { name: 'name0' }, error: undefined, status: AsyncActionStatus.pending };
requests['1'] = { arg: { name: 'name1' }, error: undefined, status: AsyncActionStatus.fulfilled };
requests['2'] = { arg: { name: 'name2' }, error: undefined, status: AsyncActionStatus.fulfilled };
requests['3'] = { arg: { name: 'name3' }, error: undefined, status: AsyncActionStatus.fulfilled };
requests['4'] = { arg: { name: 'name4' }, error: undefined, status: AsyncActionStatus.fulfilled };
requests['5'] = { arg: { name: 'name5' }, error: undefined, status: AsyncActionStatus.pending };
requests['6'] = { arg: { name: 'name6' }, error: undefined, status: AsyncActionStatus.idle };

describe('async fetch utils', () => {
  it('is exported', () => {
    expect(hasAlreadyBeenRequested).toBeDefined();
  });

  it('has been requested', () => {
    expect(hasAlreadyBeenRequested(requests, { name: 'name5' })).toBeTruthy();
  });

  it('has been not requested', () => {
    expect(hasAlreadyBeenRequested(requests, { name: 'unknown' })).toBeFalsy();
  });

  it('has been not requested, is idle', () => {
    expect(hasAlreadyBeenRequested(requests, { name: 'name6' })).toBeFalsy();
  });
});
