/* eslint-disable react/destructuring-assignment */
import { UILogger } from '@gms/ui-util';
import React, { createRef, useEffect, useState } from 'react';

const logger = UILogger.create('GMS_LOG_SYSTEM_MESSAGE', process.env.GMS_LOG_SYSTEM_MESSAGE);

interface SoundSampleProps {
  soundToPlay: string;
}

// eslint-disable-next-line react/function-component-definition
export const SoundSample: React.FunctionComponent<SoundSampleProps> = (props: SoundSampleProps) => {
  const ref = createRef<HTMLAudioElement>();
  const filename: string = props.soundToPlay?.split('/').slice(-1)[0];

  const [play, setPlay] = useState(false);

  useEffect(() => {
    if (play && props.soundToPlay && ref.current && filename !== 'None') {
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      ref.current.play().catch(e => {
        logger.error(`Error playing sound "${props.soundToPlay}": ${e}`);
      });
    }
    setPlay(true);
    // !FIX ESLINT Validate and check REACT HOOK dependencies
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [props.soundToPlay]);

  if (filename === 'None') {
    return null;
  }
  // eslint-disable-next-line jsx-a11y/media-has-caption
  return <audio ref={ref} src={props.soundToPlay} autoPlay={false} />;
};
