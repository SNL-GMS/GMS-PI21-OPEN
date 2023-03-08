import { compose } from '@gms/common-util';
import type { AppState } from '@gms/ui-state';
import { commonActions } from '@gms/ui-state';
import type React from 'react';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import { InteractionConsumer } from './interaction-consumer-component';
import type { InteractionConsumerReduxProps } from './types';

// Map parts of redux state into this component as props
const mapStateToProps = (state: AppState): Partial<InteractionConsumerReduxProps> => ({
  keyPressActionQueue: state.app.common.keyPressActionQueue
});

// Map actions dispatch callbacks into this component as props
const mapDispatchToProps = (dispatch): Partial<InteractionConsumerReduxProps> =>
  bindActionCreators(
    {
      setKeyPressActionQueue: commonActions.setKeyPressActionQueue
    },
    dispatch
  );

/**
 * Higher-order component react-redux
 */
export const ReduxInteractionConsumerContainer: React.ComponentClass<Pick<
  unknown,
  never
>> = compose(ReactRedux.connect(mapStateToProps, mapDispatchToProps))(InteractionConsumer);
