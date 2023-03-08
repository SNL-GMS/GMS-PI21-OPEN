import { compose } from '@gms/common-util';
import type { AppState } from '@gms/ui-state';
import { setSelectedStationIds } from '@gms/ui-state';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';

import { SohMapComponent } from './soh-map-component';
import type { SohMapProps } from './types';

/**
 * Mapping redux state to the properties of the component
 *
 * @param state App state, root level redux store
 */
const mapStateToProps = (state: AppState): Partial<SohMapProps> => ({
  selectedStationIds: state.app.common.selectedStationIds,
  sohStatus: state.app.dataAcquisition.data.sohStatus
});
/**
 * Mapping methods (actions and operations) to dispatch one or more updates to the redux store
 *
 * @param dispatch the redux dispatch event alerting the store has changed
 */
const mapDispatchToProps = (dispatch): Partial<SohMapProps> =>
  bindActionCreators(
    {
      setSelectedStationIds
    },
    dispatch
  );

/**
 * A new redux component that's wrapping the SohMap component
 */
export const SohMapContainer = compose(ReactRedux.connect(mapStateToProps, mapDispatchToProps))(
  SohMapComponent
);
