/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import { OverlayWrapper } from '@gms/ui-core-components';
import * as React from 'react';

import { AcknowledgeForm } from './acknowledge-form';
import type { AcknowledgeOverlayProps } from './types';

// eslint-disable-next-line react/function-component-definition
export const AcknowledgeOverlay: React.FunctionComponent<AcknowledgeOverlayProps> = props => (
  <OverlayWrapper isOpen={props.isOpen} onClose={() => props.onClose()}>
    <AcknowledgeForm
      acknowledgeStationsByName={(stationNames, comment) =>
        props.acknowledgeStationsByName(stationNames, comment)
      }
      classNames="overlay__contents"
      stationNames={props.stationNames}
      onClose={() => props.onClose()}
      requiresModificationForSubmit={props.requiresModificationForSubmit ?? true}
    />
  </OverlayWrapper>
);
