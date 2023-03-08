/* eslint-disable @typescript-eslint/no-magic-numbers */
import { WorkerOperations } from '../../src/ts/workers/operations';

describe('Worker operations', () => {
  it('Operations to be defined', () => {
    expect(WorkerOperations).toBeDefined();
    expect(WorkerOperations.CREATE_POSITION_BUFFER).toEqual('createPositionBuffer');
    expect(WorkerOperations.CREATE_RECORD_SECTION_POSITION_BUFFER).toEqual(
      'createRecordSectionPositionBuffer'
    );
  });
});
