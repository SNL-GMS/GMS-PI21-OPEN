import type { StageIntervalList } from '@gms/ui-state';
import { waitForComponentToPaint } from '@gms/ui-state/__tests__/test-util';
import { mount } from 'enzyme';
import toJson from 'enzyme-to-json';
import type Immutable from 'immutable';
import React from 'react';
import { act, create } from 'react-test-renderer';

import type { RowState } from '../../../../../src/ts/components/analyst-ui/components/workflow/workflow-table';
import { WorkflowTable } from '../../../../../src/ts/components/analyst-ui/components/workflow/workflow-table';
import { glContainer } from './gl-container';
import * as WorkflowDataTypes from './workflow-data-types';

window.ResizeObserver = jest.fn(() => {
  return { observe: jest.fn(), disconnect: jest.fn(), unobserve: jest.fn() };
});
const globalAny: any = global;
globalAny.ResizeObserver = window.ResizeObserver;
globalAny.DOMRect = jest.fn(() => ({}));

const intervalQueryResult: StageIntervalList = [];
intervalQueryResult.push({
  name: WorkflowDataTypes.interactiveStage.name,
  value: [WorkflowDataTypes.interactiveAnalysisStageInterval]
});

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const MOCK_TIME = 1606818240000;
global.Date.now = jest.fn(() => MOCK_TIME);

jest.spyOn(document, 'querySelector').mockImplementation(() => {
  return ({
    scrollWidth: 1200,
    clientWidth: 1200,
    getBoundingClientRect: jest.fn().mockReturnValue({
      width: 1200
    }),
    parentElement: {
      scrollTo: jest.fn()
    }
  } as unknown) as Element;
});
describe('Workflow Table', () => {
  it('is exported', () => {
    expect(WorkflowTable).toBeDefined();
  });

  it('matches snapshot', () => {
    const component = Enzyme.mount(
      <WorkflowTable
        glContainer={glContainer}
        timeRange={{
          startTimeSecs: 0,
          endTimeSecs: 360000
        }}
        widthPx={500}
        heightPx={500}
        stageIntervals={intervalQueryResult}
        workflow={WorkflowDataTypes.workflow}
        staleStartTime={123456}
      />
    );
    expect(
      toJson(component, {
        noKey: false,
        mode: 'deep'
      })
    ).toMatchSnapshot();

    component.setProps({
      timeRange: {
        startTimeSecs: 0,
        endTimeSecs: 360001
      },
      widthPx: 510,
      heightPx: 510,
      stageIntervals: intervalQueryResult,
      workflow: WorkflowDataTypes.workflow
    });

    const map: Immutable.Map<string, RowState> = component.state('expandedDataMap');
    const value: RowState = map.get(WorkflowDataTypes.interactiveStage.name);
    component.setState({
      expandedDataMap: map.set(WorkflowDataTypes.interactiveStage.name, {
        ...value,
        isExpanded: true
      })
    });

    expect(
      toJson(component, {
        noKey: false,
        mode: 'deep'
      })
    ).toMatchSnapshot();
  });
  describe('lifecycle methods', () => {
    it('mounts successfully if it finds virtualized container', () => {
      let result;
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      act(() => {
        result = create(
          <WorkflowTable
            glContainer={glContainer}
            timeRange={{
              startTimeSecs: 0,
              endTimeSecs: 360000
            }}
            widthPx={500}
            heightPx={500}
            stageIntervals={intervalQueryResult}
            workflow={WorkflowDataTypes.workflow}
            staleStartTime={123456}
          />
        );
      });
      expect(result.toJSON()).toMatchSnapshot();
    });
  });

  it('Horizontal scroll', async () => {
    const result = mount(
      <WorkflowTable
        glContainer={glContainer}
        timeRange={{
          startTimeSecs: 0,
          endTimeSecs: 360000
        }}
        widthPx={500}
        heightPx={500}
        stageIntervals={intervalQueryResult}
        workflow={WorkflowDataTypes.workflow}
        staleStartTime={123456}
      />
    );

    await waitForComponentToPaint(result);

    const spy = jest.spyOn(result.instance() as WorkflowTable, 'onWheel');
    const instance = result.instance() as WorkflowTable;

    // deltaX !== 0
    const wheelOne = {
      shiftKey: true,
      deltaX: 100,
      deltaY: 0
    };

    // deltaX === 0 and shiftKey === true
    const wheelTwo = {
      shiftKey: true,
      deltaX: 0,
      deltaY: 100
    };

    // deltaX === 0 and shiftKey === false
    const wheelThree = {
      shiftKey: false,
      deltaX: 0,
      deltaY: 100
    };

    instance.onWheel(wheelOne as React.WheelEvent<HTMLDivElement>);
    instance.onWheel(wheelTwo as React.WheelEvent<HTMLDivElement>);
    instance.onWheel(wheelThree as React.WheelEvent<HTMLDivElement>);
    expect(spy).toHaveBeenCalledTimes(3);
  });
});
