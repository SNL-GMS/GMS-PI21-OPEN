import { getStore } from '@gms/ui-state';
import { render } from '@testing-library/react';
import React from 'react';
import { Provider } from 'react-redux';

import { StageColumnEntry } from '../../../../../src/ts/components/analyst-ui/components/workflow/stage-column-entry';
import { WorkflowContext } from '../../../../../src/ts/components/analyst-ui/components/workflow/workflow-context';
import * as WorkflowDataTypes from './workflow-data-types';

const store = getStore();
const workflowContextData = {
  staleStartTime: 1,
  allActivitiesOpenForSelectedInterval: false,
  openConfirmationPrompt: jest.fn(),
  openAnythingConfirmationPrompt: jest.fn(),
  closeConfirmationPrompt: jest.fn()
};

describe('Stage Column Entry', () => {
  it('is exported', () => {
    expect(StageColumnEntry).toBeDefined();
  });

  it('matches snapshot', () => {
    const { container } = render(
      <Provider store={store}>
        <WorkflowContext.Provider value={workflowContextData}>
          <StageColumnEntry
            stageInterval={WorkflowDataTypes.interactiveAnalysisStageInterval}
            workflow={WorkflowDataTypes.workflow}
          />
        </WorkflowContext.Provider>
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
