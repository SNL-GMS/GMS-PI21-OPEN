import { compose } from '@gms/common-util';
import type { AppState } from '@gms/ui-state';
import { setSelectedStationIds } from '@gms/ui-state';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import { WithAcknowledge } from '../shared/acknowledge';
import { CommandRegistrarComponent } from './command-registrar-component';
import type { CommandRegistrarProps } from './types';

/**
 * Mapping redux state to the properties of the component
 *
 * @param state App state, root level redux store
 */
const mapStateToProps = (state: AppState): Partial<CommandRegistrarProps> => ({
  selectedStationIds: state.app.common.selectedStationIds,
  sohStatus: state.app.dataAcquisition.data.sohStatus
});

/**
 * Mapping methods (actions and operations) to dispatch one or more updates to the redux store
 *
 * @param dispatch the redux dispatch event alerting the store has changed
 */
const mapDispatchToProps = (dispatch): Partial<React.PropsWithChildren<CommandRegistrarProps>> =>
  bindActionCreators(
    {
      setSelectedStationIds
    },
    dispatch
  );

/**
 * A new redux component that is wrapping the CommandPalette component and injecting in the redux state
 */
export const ReduxCommandRegistrarContainer = compose(
  ReactRedux.connect(mapStateToProps, mapDispatchToProps)
)(WithAcknowledge(CommandRegistrarComponent));
