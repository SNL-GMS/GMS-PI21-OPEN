/* eslint-disable react/jsx-no-constructed-context-values */
/* eslint-disable react/jsx-props-no-spreading */
import { WorkflowTypes } from '@gms/common-model';
import { secondsToString } from '@gms/common-util';
import { render } from '@testing-library/react';
import * as React from 'react';

import { WorkflowContext } from '../../../../../src/ts/components/analyst-ui/components/workflow/workflow-context';
import {
  getActiveAnalysts,
  getStatus,
  Tooltip,
  TooltipPanel
} from '../../../../../src/ts/components/analyst-ui/components/workflow/workflow-tooltip';
import * as WorkFlowDataTypes from './workflow-data-types';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

window.ResizeObserver = jest.fn(() => {
  return { observe: jest.fn(), disconnect: jest.fn(), unobserve: jest.fn() };
});
const globalAny: any = global;
globalAny.ResizeObserver = window.ResizeObserver;
globalAny.DOMRect = jest.fn(() => ({}));

const MOCK_TIME = 1606818240000;
global.Date.now = jest.fn(() => MOCK_TIME);
describe('Workflow Tooltip', () => {
  it('is exported', () => {
    expect(TooltipPanel).toBeDefined();
    expect(getActiveAnalysts).toBeDefined();
    expect(Tooltip).toBeDefined();
  });

  it('can get active analysts', () => {
    expect(getActiveAnalysts(undefined)).toMatchInlineSnapshot(`undefined`);

    expect(
      getActiveAnalysts(WorkFlowDataTypes.interactiveAnalysisStageInterval)
    ).toMatchInlineSnapshot(`undefined`);

    expect(getActiveAnalysts(WorkFlowDataTypes.activityInterval)).toMatchInlineSnapshot(
      `"larry, moe, curly"`
    );
  });

  it('can get status', () => {
    expect(getStatus).toBeDefined();

    expect(getStatus(undefined)).toMatchInlineSnapshot(`undefined`);

    expect(getStatus(WorkFlowDataTypes.automaticProcessingStageInterval)).toMatchInlineSnapshot(
      `"In Progress (last-step)"`
    );

    expect(getStatus(WorkFlowDataTypes.activityInterval)).toMatchInlineSnapshot(`"In Progress"`);

    expect(getStatus(WorkFlowDataTypes.automaticProcessingStageInterval)).toMatchInlineSnapshot(
      `"In Progress (last-step)"`
    );

    expect(getStatus(WorkFlowDataTypes.activityInterval)).toMatchInlineSnapshot(`"In Progress"`);

    expect(
      getStatus(
        (WorkFlowDataTypes.processingSequenceInterval as unknown) as WorkflowTypes.StageInterval
      )
    ).toMatchInlineSnapshot(`"In Progress (last-step)"`);
  });

  it('matches tooltip panel snapshot', () => {
    expect(
      render(
        <WorkflowContext.Provider
          value={{
            staleStartTime: 1,
            allActivitiesOpenForSelectedInterval: false,
            openConfirmationPrompt: jest.fn(),
            openAnythingConfirmationPrompt: jest.fn(),
            closeConfirmationPrompt: jest.fn()
          }}
        >
          <TooltipPanel
            startTime={undefined}
            endTime={undefined}
            status={undefined}
            activeAnalysts={undefined}
            lastModified={undefined}
            setTooltipKey={jest.fn()}
            tooltipRef={{
              current: {
                focus: jest.fn()
              } as any
            }}
            isStale={false}
          />
        </WorkflowContext.Provider>
      ).container
    ).toMatchSnapshot();

    expect(
      render(
        <WorkflowContext.Provider
          value={{
            staleStartTime: 1,
            allActivitiesOpenForSelectedInterval: false,
            openConfirmationPrompt: jest.fn(),
            openAnythingConfirmationPrompt: jest.fn(),
            closeConfirmationPrompt: jest.fn()
          }}
        >
          <TooltipPanel
            startTime="500"
            endTime="600"
            status={WorkflowTypes.IntervalStatus.IN_PROGRESS}
            activeAnalysts="analyst 1, analyst 2"
            lastModified={secondsToString(0)}
            setTooltipKey={undefined}
            tooltipRef={{
              current: {
                focus: jest.fn()
              } as any
            }}
            isStale={false}
          />
        </WorkflowContext.Provider>
      ).container
    ).toMatchSnapshot();
  });

  it('matches tooltip snapshot', () => {
    expect(
      render(
        <WorkflowContext.Provider
          value={{
            staleStartTime: 1,
            allActivitiesOpenForSelectedInterval: false,
            openConfirmationPrompt: jest.fn(),
            openAnythingConfirmationPrompt: jest.fn(),
            closeConfirmationPrompt: jest.fn()
          }}
        >
          <Tooltip interval={WorkFlowDataTypes.activityInterval} staleStartTime={500} />
        </WorkflowContext.Provider>
      ).container
    ).toMatchSnapshot();

    expect(
      render(
        <WorkflowContext.Provider
          value={{
            staleStartTime: 1,
            allActivitiesOpenForSelectedInterval: false,
            openConfirmationPrompt: jest.fn(),
            openAnythingConfirmationPrompt: jest.fn(),
            closeConfirmationPrompt: jest.fn()
          }}
        >
          <Tooltip
            interval={WorkFlowDataTypes.automaticProcessingStageInterval}
            staleStartTime={500}
          >
            <div>content</div>
          </Tooltip>
        </WorkflowContext.Provider>
      ).container
    ).toMatchSnapshot();
  });

  it('can handle undefined interval with tooltip', () => {
    expect(
      render(
        <WorkflowContext.Provider
          value={{
            staleStartTime: 1,
            allActivitiesOpenForSelectedInterval: false,
            openConfirmationPrompt: jest.fn(),
            openAnythingConfirmationPrompt: jest.fn(),
            closeConfirmationPrompt: jest.fn()
          }}
        >
          <Tooltip interval={undefined} staleStartTime={500} />{' '}
        </WorkflowContext.Provider>
      ).container
    ).toMatchSnapshot();
  });
});
