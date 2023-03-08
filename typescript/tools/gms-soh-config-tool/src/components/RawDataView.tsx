import { makeStyles } from '@mui/styles';
import React from 'react';
import ReactJson from 'react-json-view';

const useStyles = makeStyles({
  dataContent: {
    display: 'flex',
    justifyContent: 'left',
    borderRadius: '0.25em',
    backgroundColor: '#cecece',
    marginBottom: '1rem',
    overflow: 'auto'
  }
});

export interface ReactDataViewProps {
  data: object;
}

export const RawDataView: React.FC<ReactDataViewProps> = ({data}: ReactDataViewProps) => {
  const classes = useStyles();
  return <div className={classes.dataContent}>
    <pre id='boundData'>
      <ReactJson src={data} collapseStringsAfterLength={60} shouldCollapse={(field) => {
        if (field.name === 'root') {
          return false;
        }
        return true;
      }}/>
    </pre>
  </div>; 
}
