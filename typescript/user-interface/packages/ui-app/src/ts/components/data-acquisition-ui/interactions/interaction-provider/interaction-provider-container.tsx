import { compose } from '@gms/common-util';
import type { AppState } from '@gms/ui-state';
import { setCommandPaletteVisibility, setSelectedStationIds } from '@gms/ui-state';
import type React from 'react';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import { InteractionProvider } from './interaction-provider-component';
import type { InteractionProviderReduxProps } from './types';

/**
 * Mapping redux state to the properties of the component
 *
 * @param state App state, root level redux store
 */
const mapStateToProps = (state: AppState): Partial<InteractionProviderReduxProps> => ({
  commandPaletteIsVisible: state.app.common.commandPaletteIsVisible
});

// Map actions dispatch callbacks into this component as props
const mapDispatchToProps = (dispatch): Partial<InteractionProviderReduxProps> =>
  bindActionCreators(
    {
      setCommandPaletteVisibility,
      setSelectedStationIds
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
