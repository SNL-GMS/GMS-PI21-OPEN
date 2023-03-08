/* eslint-disable react/jsx-no-useless-fragment */
/* eslint-disable jest/expect-expect */
import { WorkflowTypes } from '@gms/common-model';
import { SECONDS_IN_HOUR } from '@gms/common-util';
import { renderHook } from '@testing-library/react-hooks';
import { produceWithPatches } from 'immer';
import flatMap from 'lodash/flatMap';
import React from 'react';
import { Provider } from 'react-redux';
import { create } from 'react-test-renderer';

import type {
  StageIntervalList,
  UpdateActivityIntervalStatusParams,
  UpdateStageIntervalStatusParams
} from '../../../../src/ts/app/api/workflow/workflow-api-slice';
import {
  produceCleanUp,
  updateStageIntervals,
  useCleanupStageIntervalsByIdAndTimeQuery,
  useGetStageIntervalsByIdAndTimeParams,
  useStageIntervalsByIdAndTimeQuery,
  useUpdateActivityIntervalStatusMutation,
  useUpdateStageIntervalStatusMutation,
  useWorkflowQuery,
  workflowApiSlice
} from '../../../../src/ts/app/api/workflow/workflow-api-slice';
import { getStore } from '../../../../src/ts/app/store';
import { expectQueryHookToMakeAxiosRequest } from '../query-test-util';
import * as sampleData from './sample-data';

describe('workflow api slice', () => {
  it('exists', () => {
    expect(useGetStageIntervalsByIdAndTimeParams).toBeDefined();
    expect(useCleanupStageIntervalsByIdAndTimeQuery).toBeDefined();
    expect(useStageIntervalsByIdAndTimeQuery).toBeDefined();
    expect(useUpdateActivityIntervalStatusMutation).toBeDefined();
    expect(useUpdateStageIntervalStatusMutation).toBeDefined();
    expect(useWorkflowQuery).toBeDefined();
    expect(produceCleanUp).toBeDefined();
    expect(workflowApiSlice).toBeDefined();
  });

  it('get stage interval params', () => {
    const test1 = renderHook(() =>
      useGetStageIntervalsByIdAndTimeParams(['test1', 'test2'], undefined)
    );
    expect(test1.result).toMatchSnapshot();

    const test2 = renderHook(() =>
      useGetStageIntervalsByIdAndTimeParams(['test1', 'test2'], {
        startTimeSecs: 5,
        endTimeSecs: 10
      })
    );
    expect(test2.result).toMatchSnapshot();
  });

  it('hook queries for workflow', async () => {
    await expectQueryHookToMakeAxiosRequest(useWorkflowQuery);
  });

  it('hook queries for stage intervals by id and time', async () => {
    const useQuery = () => {
      const query = useStageIntervalsByIdAndTimeQuery(['test'], {
        startTimeSecs: 200,
        endTimeSecs: 500
      });
      return query;
    };
    await expectQueryHookToMakeAxiosRequest(useQuery);
  });

  it('hook updates activity interval status', async () => {
    const params: UpdateActivityIntervalStatusParams = {
      userName: 'test',
      time: 100,
      activityIntervalId: undefined,
      stageIntervalId: undefined,
      status: WorkflowTypes.IntervalStatus.COMPLETE
    };
    const useMutation = () => {
      const [mutation] = useUpdateActivityIntervalStatusMutation();
      React.useEffect(() => {
        mutation(params).catch(() => {
          /* do nothing */
        });
      }, [mutation]);
      return 'pass';
    };
    await expectQueryHookToMakeAxiosRequest(useMutation);
  });

  it('hook updates stage interval status', async () => {
    const params: UpdateStageIntervalStatusParams = {
      userName: 'test',
      time: 100,
      stageIntervalId: undefined,
      status: WorkflowTypes.IntervalStatus.COMPLETE
    };
    const useMutation = () => {
      const [mutation] = useUpdateStageIntervalStatusMutation();
      React.useEffect(() => {
        mutation(params).catch(() => {
          /* do nothing */
        });
      }, [mutation]);
      return 'pass';
    };
    await expectQueryHookToMakeAxiosRequest(useMutation);
  });

  it('able to update stage intervals', () => {
    expect(produceWithPatches(undefined, updateStageIntervals(undefined))).toEqual([
      undefined,
      undefined,
      undefined
    ]);

    const stageIntervalList: StageIntervalList = [];
    stageIntervalList.push({
      name: sampleData.interactiveStage.name,
      value: [sampleData.interactiveAnalysisStageInterval]
    });

    expect(produceWithPatches(stageIntervalList, updateStageIntervals(undefined))).toEqual([
      stageIntervalList,
      [],
      []
    ]);

    expect(
      produceWithPatches(
        stageIntervalList,
        updateStageIntervals([{ ...sampleData.interactiveAnalysisStageInterval, name: 'unknown' }])
      )
    ).toEqual([stageIntervalList, [], []]);

    expect(
      produceWithPatches(
        stageIntervalList,
        updateStageIntervals([
          {
            ...sampleData.interactiveAnalysisStageInterval,
            modificationTime: sampleData.interactiveAnalysisStageInterval.modificationTime - 1
          }
        ])
      )[0]
    ).toEqual(stageIntervalList);

    expect(
      produceWithPatches(
        stageIntervalList,
        updateStageIntervals([
          {
            ...sampleData.interactiveAnalysisStageInterval,
            startTime: sampleData.interactiveAnalysisStageInterval.startTime + 5,
            endTime: sampleData.interactiveAnalysisStageInterval.endTime + 5
          }
        ])
      )[0]
    ).toMatchSnapshot();

    expect(
      produceWithPatches(
        stageIntervalList,
        updateStageIntervals([sampleData.interactiveAnalysisStageInterval])
      )[0]
    ).toEqual(stageIntervalList);
  });

  it('able to use cleanup stage intervals', () => {
    const store = getStore();

    function Component() {
      const results = useCleanupStageIntervalsByIdAndTimeQuery(
        [sampleData.interactiveAnalysisStage.name],
        { startTimeSecs: 100, endTimeSecs: 200 }
      );
      results(100);
      return <>{results}</>;
    }
    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });

  describe('can produce cleaned up intervals', () => {
    const start = 1643202000; // represents Wednesday, January 26, 2022 1:00:00 PM

    it('test boundary to the left, no clean up', () => {
      const itr = [0, 1, 2, 3, 4, 5];
      const increment = SECONDS_IN_HOUR;

      const list: {
        name: string;
        value: Partial<WorkflowTypes.StageInterval>[];
      }[] = [
        {
          name: 'name1',
          value: itr.map(v => ({
            startTime: start + v * SECONDS_IN_HOUR,
            endTime: start + v * SECONDS_IN_HOUR + increment
          }))
        }
      ];

      const [result] = produceWithPatches(list, produceCleanUp(start));
      expect(result).toHaveLength(1);
      expect(result[0].name).toEqual(list[0].name);
      expect(result[0].value).toHaveLength(list[0].value.length);
      expect(result[0].value).toEqual(list[0].value);
    });

    it('test boundary to remove all data', () => {
      const itr = [0, 1, 2, 3, 4, 5];
      const increment = SECONDS_IN_HOUR;

      const list: {
        name: string;
        value: Partial<WorkflowTypes.StageInterval>[];
      }[] = [
        {
          name: 'name1',
          value: itr.map(v => ({
            startTime: start + v * SECONDS_IN_HOUR,
            endTime: start + v * SECONDS_IN_HOUR + increment
          }))
        }
      ];

      const [result] = produceWithPatches(
        list,
        produceCleanUp(start + 5 * SECONDS_IN_HOUR + increment)
      );
      expect(result).toHaveLength(1);
      expect(result[0].name).toEqual(list[0].name);
      expect(result[0].value).toHaveLength(0);
      expect(result[0].value).toEqual([]);
    });

    it('test error conditions', () => {
      const itr = [0, 1, 2, 3, 4, 5];
      const increment = SECONDS_IN_HOUR;

      const list: {
        name: string;
        value: Partial<WorkflowTypes.StageInterval>[];
      }[] = [
        {
          name: 'name1',
          value: itr.map(v => ({
            endTime: start + v * SECONDS_IN_HOUR,
            startTime: start + v * SECONDS_IN_HOUR + increment
          }))
        }
      ];

      expect(() => {
        produceWithPatches(list, produceCleanUp(start + 5 * SECONDS_IN_HOUR + increment));
      }).toThrow();

      const [result] = produceWithPatches(list, produceCleanUp(undefined));
      expect(result).toEqual(list);
    });

    it('test shifting boundary', () => {
      const itr = [0, 1, 2, 3, 4, 5];
      const increment = SECONDS_IN_HOUR;

      const list: {
        name: string;
        value: Partial<WorkflowTypes.StageInterval>[];
      }[] = [
        {
          name: 'name1',
          value: itr.map(v => ({
            startTime: start + v * SECONDS_IN_HOUR,
            endTime: start + v * SECONDS_IN_HOUR + increment
          }))
        }
      ];

      itr.forEach(i => {
        const staleCleanUpBoundary = start + i * SECONDS_IN_HOUR;
        const [result] = produceWithPatches(list, produceCleanUp(staleCleanUpBoundary));
        expect(result).toHaveLength(1);
        expect(result[0].name).toEqual(list[0].name);
        expect(result[0].value).toHaveLength(itr.filter(j => j >= i).length);
        expect(result[0].value).toEqual(
          itr
            .filter(j => j >= i)
            .map(v => ({
              startTime: start + v * SECONDS_IN_HOUR,
              endTime: start + v * SECONDS_IN_HOUR + increment
            }))
        );
      });
    });

    it('test shifting boundary - out of order', () => {
      // eslint-disable-next-line @typescript-eslint/no-magic-numbers
      const itr = [8, 7, 0, 6, 1, 2, 3, 4, 5, 8];
      const increment = SECONDS_IN_HOUR;

      const list: {
        name: string;
        value: Partial<WorkflowTypes.StageInterval>[];
      }[] = [
        {
          name: 'name1',
          value: itr.map(v => ({
            startTime: start + v * SECONDS_IN_HOUR,
            endTime: start + v * SECONDS_IN_HOUR + increment
          }))
        }
      ];

      itr.forEach(i => {
        const staleCleanUpBoundary = start + i * SECONDS_IN_HOUR;
        const [result] = produceWithPatches(list, produceCleanUp(staleCleanUpBoundary));
        expect(result).toHaveLength(1);
        expect(result[0].name).toEqual(list[0].name);
        expect(result[0].value).toHaveLength(itr.filter(j => j >= i).length);
        expect(result[0].value).toEqual(
          itr
            .filter(j => j >= i)
            .map(v => ({
              startTime: start + v * SECONDS_IN_HOUR,
              endTime: start + v * SECONDS_IN_HOUR + increment
            }))
        );
      });
    });

    it('test shifting boundary - with multiples', () => {
      // eslint-disable-next-line @typescript-eslint/no-magic-numbers
      const itr1 = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
      const increment1 = SECONDS_IN_HOUR;

      // eslint-disable-next-line @typescript-eslint/no-magic-numbers
      const itr2 = [0, 2, 4, 6, 8, 10];
      const increment2 = SECONDS_IN_HOUR * 2;

      const list: {
        name: string;
        value: Partial<WorkflowTypes.StageInterval>[];
      }[] = [
        {
          name: 'name1',
          value: itr1.map(v => ({
            startTime: start + v * SECONDS_IN_HOUR,
            endTime: start + v * SECONDS_IN_HOUR + increment1
          }))
        },
        {
          name: 'name2',
          value: itr2.map(v => ({
            startTime: start + v * SECONDS_IN_HOUR,
            endTime: start + v * SECONDS_IN_HOUR + increment2
          }))
        }
      ];

      itr1.forEach(i => {
        const staleCleanUpBoundary = start + i * SECONDS_IN_HOUR;
        const [result] = produceWithPatches(list, produceCleanUp(staleCleanUpBoundary));
        expect(result).toHaveLength(2);

        expect(result[0].name).toEqual(list[0].name);
        expect(result[1].name).toEqual(list[1].name);

        expect(Math.min(...flatMap(result[0].value.map(e => e.endTime)))).toBeGreaterThan(
          staleCleanUpBoundary
        );
        expect(Math.min(...flatMap(result[1].value.map(e => e.endTime)))).toBeGreaterThan(
          staleCleanUpBoundary
        );
      });
    });
  });
});
