/* eslint-disable react/jsx-no-constructed-context-values */
import { getStore } from '@gms/ui-state';
import { waitForComponentToPaint } from '@gms/ui-state/__tests__/test-util';
import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';
import { Provider } from 'react-redux';

import {
  ActivityIntervalCell,
  determineTextForCell,
  preventDefaultEvent
} from '../../../../../src/ts/components/analyst-ui/components/workflow/activity-interval-cell';
import { WorkflowContext } from '../../../../../src/ts/components/analyst-ui/components/workflow/workflow-context';
import * as WorkflowDataTypes from './workflow-data-types';

const store = getStore();

describe('Activity Interval Cell', () => {
  it('is exported', () => {
    expect(ActivityIntervalCell).toBeDefined();
  });

  it('matches snapshot', () => {
    const { container } = render(
      <Provider store={store}>
        <WorkflowContext.Provider
          value={{
            staleStartTime: 1,
            allActivitiesOpenForSelectedInterval: false,
            openConfirmationPrompt: jest.fn(),
            openAnythingConfirmationPrompt: jest.fn(),
            closeConfirmationPrompt: jest.fn()
          }}
        >
          <ActivityIntervalCell activityInterval={WorkflowDataTypes.activityInterval} />
        </WorkflowContext.Provider>
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });

  it('shallow mounts', () => {
    const { container } = render(
      <Provider store={store}>
        <WorkflowContext.Provider
          value={{
            staleStartTime: 1,
            allActivitiesOpenForSelectedInterval: false,
            openConfirmationPrompt: jest.fn(),
            openAnythingConfirmationPrompt: jest.fn(),
            closeConfirmationPrompt: jest.fn()
          }}
        >
          <ActivityIntervalCell activityInterval={WorkflowDataTypes.activityInterval} />
        </WorkflowContext.Provider>
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });

  it('can determine text for cell', () => {
    const text = determineTextForCell(WorkflowDataTypes.status, WorkflowDataTypes.analysts);
    expect(text).toBe('larry + 2');

    const text2 = determineTextForCell(
      WorkflowDataTypes.status,
      WorkflowDataTypes.analysts.slice(-1)
    );
    expect(text2).toBe('curly');

    const desiredAnalystItem = -2;
    const text3 = determineTextForCell(
      WorkflowDataTypes.status,
      WorkflowDataTypes.analysts.slice(desiredAnalystItem)
    );
    expect(text3).toBe('moe + 1');

    const text4 = determineTextForCell(
      WorkflowDataTypes.notStartedStatus,
      WorkflowDataTypes.analysts
    );
    expect(text4).toBe('');

    const text5 = determineTextForCell(
      WorkflowDataTypes.notCompleteStatus,
      WorkflowDataTypes.analysts
    );
    expect(text5).toBe('');

    const text6 = determineTextForCell(
      WorkflowDataTypes.completeStatus,
      WorkflowDataTypes.analysts
    );
    expect(text6).toBe('larry');

    const text7 = determineTextForCell(WorkflowDataTypes.completeStatus, []);
    expect(text7).toBe('');

    const text8 = determineTextForCell(WorkflowDataTypes.completeStatus, undefined);
    expect(text8).toBe('');
  });

  it('Activity Interval Cell functions and clicks', async () => {
    const wrapper = Enzyme.mount(
      <Provider store={store}>
        <WorkflowContext.Provider
          value={{
            staleStartTime: 1,
            allActivitiesOpenForSelectedInterval: false,
            openConfirmationPrompt: jest.fn(),
            openAnythingConfirmationPrompt: jest.fn(),
            closeConfirmationPrompt: jest.fn()
          }}
        >
          <ActivityIntervalCell activityInterval={WorkflowDataTypes.activityInterval} />
        </WorkflowContext.Provider>
      </Provider>
    );

    expect(wrapper.props().isSelected).toBeFalsy();

    wrapper.setProps({ isSelected: true });
    await waitForComponentToPaint(wrapper);
    expect(wrapper.props().isSelected).toBeTruthy();
  });
  it('can prevent default for context menu', () => {
    const event: any = {
      preventDefault: jest.fn(),
      openAnythingConfirmationPrompt: jest.fn(),
      closeConfirmationPrompt: jest.fn()
    };
    preventDefaultEvent(event);
    const spy = jest.spyOn(event, 'preventDefault');
    expect(spy).toHaveBeenCalled();
  });

  it('double clicking opens interactive events', () => {
    const openConfirmationPromptMock = jest.fn();

    const component = Enzyme.mount(
      <Provider store={store}>
        <WorkflowContext.Provider
          value={{
            staleStartTime: 1,
            allActivitiesOpenForSelectedInterval: false,
            openConfirmationPrompt: openConfirmationPromptMock,
            openAnythingConfirmationPrompt: jest.fn(),
            closeConfirmationPrompt: jest.fn()
          }}
        >
          <ActivityIntervalCell activityInterval={WorkflowDataTypes.activityInterval} />
        </WorkflowContext.Provider>
      </Provider>
    );
    const actIntItem = component.find('[data-cy="1622053587-Event Review"]');
    actIntItem.simulate('doubleClick');
    expect(openConfirmationPromptMock).toHaveBeenCalledTimes(1);
  });
});
