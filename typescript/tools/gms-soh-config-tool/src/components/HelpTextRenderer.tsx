import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import DownloadingOutlinedIcon from '@mui/icons-material/DownloadingOutlined';
import ReportOutlinedIcon from '@mui/icons-material/ReportOutlined';
import { Tooltip } from '@mui/material';
import makeStyles from '@mui/styles/makeStyles';
import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { ErrorInfo } from './ErrorInfo';

interface HelpTextRendererProps {
  helpText: string | undefined;
  isError?: boolean;
  isLoading?: boolean;
}

// Making it so icon only shows on hover and is higher then the component being wrapped
// Position the icon on the top left
const useStyles = makeStyles({
  icon: {
    position: 'absolute',
    left: 0,
    top: 'calc(50% - 25px)',
    transform: 'translate3d(-50%, -50%, 0)',
    borderRadius: '100%',
    background: 'white',
    zIndex: 9,
    display: 'none',
  },
  wrapper: {
    position: 'relative',
    margin: 0,
    padding: 0,
    '&:hover $icon': {
      display: 'block',
    },
  },
});

/**
 * Component that is an info icon that has a tooltip which shows help text
 * if isLoading shows a loading icon
 * if isError shows error icon with list of errors in tooltip
 *
 * @returns an info Icon with a tooltip showing the help text
 */
export const HelpTextRenderer: React.FC<
  React.PropsWithChildren<HelpTextRendererProps>
> = (props: React.PropsWithChildren<HelpTextRendererProps>) => {
  const { children, helpText, isError, isLoading } = props;
  const classes = useStyles();

  const determineIconAndText = (): JSX.Element => {
    if (isLoading) {
      return (
        <Tooltip title={'loading...'}>
          <DownloadingOutlinedIcon
            className={classes.icon}
            color={'inherit'}
            fontSize={'small'}
          />
        </Tooltip>
      );
    }
    if (isError) {
      return (
        <Tooltip title={<ErrorInfo />}>
          <ReportOutlinedIcon
            className={classes.icon}
            color={'error'}
            fontSize={'small'}
          />
        </Tooltip>
      );
    }
    return (
      <Tooltip
        title={
          <ReactMarkdown remarkPlugins={[remarkGfm]}>
            {helpText ?? ''}
          </ReactMarkdown>
        }
      >
        <InfoOutlinedIcon
          className={classes.icon}
          color={'inherit'}
          fontSize={'small'}
        />
      </Tooltip>
    );
  };
  return (
    <div className={classes.wrapper}>
      {determineIconAndText()}
      {children ?? <></>}
    </div>
  );
};
