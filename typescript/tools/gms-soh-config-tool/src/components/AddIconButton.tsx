import AddIcon from '@mui/icons-material/Add';
import { Tooltip } from '@mui/material';
import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

interface AddIconButtonProps {
  containerId: string;
  helpText: string | undefined;
  className: string;
  onIconClick: (containerId: string) => void;
}

export const AddIconButton: React.FC<
  React.PropsWithChildren<AddIconButtonProps>
> = (props: React.PropsWithChildren<AddIconButtonProps>) => {
  const { helpText, containerId, className, onIconClick } = props;
  return (
    <Tooltip
      title={
        <ReactMarkdown remarkPlugins={[remarkGfm]}>
          {helpText ?? ''}
        </ReactMarkdown>
      }
    >
      <AddIcon
        className={className}
        color={'inherit'}
        fontSize={'small'}
        onClick={() => onIconClick(containerId)}
      />
    </Tooltip>
  );
};
