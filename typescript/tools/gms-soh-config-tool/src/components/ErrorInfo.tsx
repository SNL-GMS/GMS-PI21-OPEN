import { useAppSelector } from '../state/react-redux-hooks';
import { makeStyles } from '@mui/styles';

const useStyles = makeStyles({
  tooManyErrorsLine: {
    borderTop: 'solid',
    borderWidth: '2px',
    borderColor: 'black',
  },
});

/**
 * Creates a ul of user input errors
 */
export const ErrorInfo: React.FC<{}> = () => {
  const classes = useStyles();
  const userInputErrorsMap = useAppSelector(
    (state) => state.stationControls.error
  );
  const maxErrorsToDisplay = 9;
  let numberOfErrors = 0;
  return (
    <>
      <div>List of Errors:</div>
      <ul>
        {Object.keys(userInputErrorsMap).map((entryName, index) => {
          if (
            userInputErrorsMap[entryName].hasError &&
            numberOfErrors <= maxErrorsToDisplay
          ) {
            numberOfErrors = numberOfErrors + 1;
            return (
              <li key={entryName}>{userInputErrorsMap[entryName].reason}</li>
            );
          }
          if (numberOfErrors === maxErrorsToDisplay) {
            numberOfErrors = numberOfErrors + 1;
            return (
              <>
                <div
                  className={classes.tooManyErrorsLine}
                  key={'TooManyErrorsLine'}
                ></div>
                <div key={'TooManyErrors'}>
                  Too many errors to display, please resolve some to see more
                </div>
              </>
            );
          }
          return undefined;
        })}
      </ul>
    </>
  );
};
