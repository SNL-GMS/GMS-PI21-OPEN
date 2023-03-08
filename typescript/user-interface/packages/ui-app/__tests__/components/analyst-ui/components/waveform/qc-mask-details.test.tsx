import { render } from '@testing-library/react';
import React from 'react';

import {
  QcMaskForm,
  QcMaskOverlap
} from '../../../../../src/ts/components/analyst-ui/common/dialogs';
import { QcMaskDialogBoxType } from '../../../../../src/ts/components/analyst-ui/common/dialogs/types';
import { overlappingQcMaskData } from '../../../../__data__/qc-mask-data';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const startTime = 1274394556;
const endTime = 1274394621;

it('QC Mask details renders & matches snapshot (single mask)', () => {
  const { container } = render(
    <QcMaskForm
      qcMaskDialogBoxType={QcMaskDialogBoxType.Create}
      startTimeSecs={startTime}
      endTimeSecs={endTime}
      applyChanges={undefined}
    />
  );
  expect(container).toMatchSnapshot();
});

it('Multi Mask dialog renders & matches snapshot  (overlapping masks)', () => {
  const { container } = render(
    <QcMaskOverlap
      masks={overlappingQcMaskData}
      contextMenuCoordinates={{
        xPx: 5,
        yPx: 5
      }}
      openNewContextMenu={() => {
        /* no-op */
      }}
      selectMask={() => {
        /* no-op */
      }}
    />
  );
  expect(container).toMatchSnapshot();
});
