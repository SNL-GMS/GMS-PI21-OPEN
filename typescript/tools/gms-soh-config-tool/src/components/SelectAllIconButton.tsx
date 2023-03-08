import DoneAllIcon from '@mui/icons-material/DoneAll';
import { Tooltip } from '@mui/material';
import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

interface SelectAllIconButtonProps {
  containerId: string;
  helpText: string | undefined;
  className: string;
  onIconClick: (containerId: string) => void;
}

export const SelectAllIconButton: React.FC<
  React.PropsWithChildren<SelectAllIconButtonProps>
> = (props: React.PropsWithChildren<SelectAllIconButtonProps>) => {
  const { helpText, containerId, className, onIconClick } = props;
  return (
    <Tooltip
      title={
        <ReactMarkdown remarkPlugins={[remarkGfm]}>
          {helpText ?? ''}
        </ReactMarkdown>
      }
    >
      <DoneAllIcon
        className={className}
        color={'inherit'}
        fontSize={'small'}
        onClick={() => onIconClick(containerId)}
      />
    </Tooltip>
  );
};
