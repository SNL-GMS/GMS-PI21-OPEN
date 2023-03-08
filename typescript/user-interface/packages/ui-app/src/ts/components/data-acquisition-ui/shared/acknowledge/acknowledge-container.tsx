import { compose } from '@gms/common-util';
import type { AppState } from '@gms/ui-state';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import { AcknowledgeWrapper } from './acknowledge-wrapper';
import type { AcknowledgeWrapperProps } from './types';

// Map parts of redux state into this component as props
const mapStateToProps = (state: AppState): Partial<AcknowledgeWrapperProps> => ({
  sohStatus: state.app.dataAcquisition.data.sohStatus
});

// Map actions dispatch callbacks into this component as props
const mapDispatchToProps = (dispatch): Partial<AcknowledgeWrapperProps> =>
  bindActionCreators({}, dispatch);

/**
 * Connects the AppToolbar to the Redux store
 */
export const ReduxAcknowledgeContainer = compose(
  ReactRedux.connect(mapStateToProps, mapDispatchToProps)
)(AcknowledgeWrapper);
