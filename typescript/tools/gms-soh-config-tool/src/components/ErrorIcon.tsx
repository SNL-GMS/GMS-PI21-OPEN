import ErrorIcon from '@mui/icons-material/Error';
import { Tooltip } from '@mui/material';
import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { useAppSelector } from '../state/react-redux-hooks';

interface ErrorIconInfoProps {
  rollupId: string;
  className: string;
}

/**
 * Loops through errors to determine if need to add icon
 *
 * @param props id of rollup to check if errors
 * @returns error icon with message or empty fragment
 */
export const ErrorIconInfo: React.FC<
  React.PropsWithChildren<ErrorIconInfoProps>
> = (props: React.PropsWithChildren<ErrorIconInfoProps>) => {
  const { rollupId, className } = props;
  const configErrors = useAppSelector((state) => state.stationControls.error);

  let hasError = false;
  let errorMessage = 'Error: ';

  Object.keys(configErrors).forEach((errorId) => {
    if (errorId.includes(rollupId) && configErrors[errorId].hasError) {
      hasError = true;
      errorMessage = errorMessage.concat(
        configErrors[errorId].reason,
        '. Error: '
      );
    }
  });
  if (hasError) {
    // remove floating error and and whitespace text
    errorMessage = errorMessage.replace(/. Error: \s*$/, '');
    return (
      <Tooltip
        title={
          <ReactMarkdown remarkPlugins={[remarkGfm]}>
            {errorMessage}
          </ReactMarkdown>
        }
      >
        <ErrorIcon className={className} color={'inherit'} fontSize={'small'} />
      </Tooltip>
    );
  }
  return <></>;
};
