/* eslint-disable react/destructuring-assignment */
import type { FkTypes } from '@gms/common-model';
import React from 'react';

import { frequencyBandToString } from '~analyst-ui/common/utils/fk-utils';

import type { FkUnits } from '../../types';
import { FkThumbnail } from '../fk-thumbnail';

const SIZE_PX_OF_FREQUENCY_THUMBNAILS_PX = 100;

export interface FkFrequencyThumbnailProps {
  fkFrequencySpectra: FkTypes.FkFrequencyThumbnail[];
  fkUnit: FkUnits;
  arrivalTimeMovieSpectrumIndex: number;

  onThumbnailClick(minFrequency: number, maxFrequency: number): void;
}
export class FkFrequencyThumbnails extends React.PureComponent<FkFrequencyThumbnailProps, any> {
  public render(): JSX.Element {
    return (
      <div className="fk-frequency-thumbnails">
        {this.props.fkFrequencySpectra.map((spectra, index) => (
          <FkThumbnail
            fkData={spectra.fkSpectra}
            label={frequencyBandToString(spectra.frequencyBand)}
            // eslint-disable-next-line react/no-array-index-key
            key={index}
            selected={false}
            dimFk={false}
            sizePx={SIZE_PX_OF_FREQUENCY_THUMBNAILS_PX}
            fkUnit={this.props.fkUnit}
            // eslint-disable-next-line @typescript-eslint/no-empty-function
            showFkThumbnailMenu={() => {}}
            arrivalTimeMovieSpectrumIndex={0}
            onClick={() => {
              this.props.onThumbnailClick(
                spectra.frequencyBand.minFrequencyHz,
                spectra.frequencyBand.maxFrequencyHz
              );
            }}
          />
        ))}
      </div>
    );
  }
}
