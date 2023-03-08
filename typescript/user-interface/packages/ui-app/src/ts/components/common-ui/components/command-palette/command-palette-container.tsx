import { compose } from '@gms/common-util';
import type { AppState } from '@gms/ui-state';
import { commonActions, setCommandPaletteVisibility } from '@gms/ui-state';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import { CommandPaletteComponent } from './command-palette-component';
import type { CommandPaletteComponentProps } from './types';

/**
 * Mapping redux state to the properties of the component
 *
 * @param state App state, root level redux store
 */
const mapStateToProps = (state: AppState): Partial<CommandPaletteComponentProps> => ({
  commandPaletteIsVisible: state.app.common.commandPaletteIsVisible,
  keyPressActionQueue: state.app.common.keyPressActionQueue
});

/**
 * Mapping methods (actions and operations) to dispatch one or more updates to the redux store
 *
 * @param dispatch the redux dispatch event alerting the store has changed
 */
const mapDispatchToProps = (
  dispatch
): Partial<React.PropsWithChildren<CommandPaletteComponentProps>> =>
  bindActionCreators(
    {
      setCommandPaletteVisibility,
      setKeyPressActionQueue: commonActions.setKeyPressActionQueue
    },
    dispatch
  );

/**
 * A new redux component that is wrapping the CommandPalette component and injecting in the redux state
 */
export const ReduxCommandPaletteContainer = compose(
  ReactRedux.connect(mapStateToProps, mapDispatchToProps)
)(CommandPaletteComponent);
