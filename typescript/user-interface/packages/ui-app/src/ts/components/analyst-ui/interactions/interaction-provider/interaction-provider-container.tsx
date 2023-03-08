import { compose } from '@gms/common-util';
import type { AppState } from '@gms/ui-state';
import { analystActions, setCommandPaletteVisibility } from '@gms/ui-state';
import type React from 'react';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import { InteractionProvider } from './interaction-provider-component';
import type { InteractionProviderReduxProps } from './types';

// Map parts of redux state into this component as props
const mapStateToProps = (state: AppState): Partial<InteractionProviderReduxProps> => ({
  currentTimeInterval: state.app.workflow ? state.app.workflow.timeRange : undefined,
  analysisMode: state.app.workflow ? state.app.workflow.analysisMode : undefined,
  openEventId: state.app.analyst.openEventId,
  historyActionInProgress: state.app.analyst.historyActionInProgress,
  commandPaletteIsVisible: state.app.common.commandPaletteIsVisible
});

// Map actions dispatch callbacks into this component as props
const mapDispatchToProps = (dispatch): Partial<InteractionProviderReduxProps> =>
  bindActionCreators(
    {
      incrementHistoryActionInProgress: analystActions.incrementHistoryAction,
      decrementHistoryActionInProgress: analystActions.decrementHistoryAction,
      setCommandPaletteVisibility
    },
    dispatch
  );

/**
 * Higher-order component react-redux
 */
export const ReduxInteractionProviderContainer: React.ComponentClass<Pick<
  unknown,
  never
>> = compose(ReactRedux.connect(mapStateToProps, mapDispatchToProps))(InteractionProvider);
