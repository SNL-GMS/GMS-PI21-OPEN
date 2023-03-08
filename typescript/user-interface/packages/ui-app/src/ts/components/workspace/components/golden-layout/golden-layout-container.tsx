import { compose } from '@gms/common-util';
import type { AppState } from '@gms/ui-state';
import { analystActions, AppOperations, commonActions } from '@gms/ui-state';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import { GoldenLayoutComponent } from './golden-layout-component';
import type { GoldenLayoutComponentProps } from './types';

// Map parts of redux state into this component as props
const mapStateToProps = (state: AppState): Partial<GoldenLayoutComponentProps> => ({
  currentTimeInterval: state.app.workflow ? state.app.workflow.timeRange : undefined,
  analysisMode: state.app.workflow ? state.app.workflow.analysisMode : undefined,
  openLayoutName: state.app.analyst.openLayoutName,
  keyPressActionQueue: state.app.common.keyPressActionQueue
});

// Map actions dispatch callbacks into this component as props
const mapDispatchToProps = (dispatch): Partial<GoldenLayoutComponentProps> =>
  bindActionCreators(
    {
      setOpenLayoutName: analystActions.setOpenLayoutName,
      setKeyPressActionQueue: commonActions.setKeyPressActionQueue,
      setAppAuthenticationStatus: AppOperations.setAppAuthenticationStatus
    } as any,
    dispatch
  );

/**
 * Connects the AppToolbar to the Redux store
 */
export const GoldenLayoutContainer = compose(
  ReactRedux.connect(mapStateToProps, mapDispatchToProps)
)(GoldenLayoutComponent);
