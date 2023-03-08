import * as path from 'path';

import { readCsvData, readJsonData } from '../../src/ts/common-util';

const basePath = path.resolve(__dirname, '../__data__');
describe('file system utils', () => {
  test('readJsonData', () => {
    const filePath = path.resolve(basePath, 'file-system-test.json');
    const jsonRes = readJsonData(filePath);
    expect(jsonRes).toMatchSnapshot();
  });

  test('readCsvData', () => {
    const filePath = path.resolve(basePath, 'file-system-test.csv');
    const csvRes = readCsvData(filePath);
    expect(csvRes).toMatchSnapshot();
  });
});
