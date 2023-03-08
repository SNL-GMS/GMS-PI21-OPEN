import parse from 'csv-parse/lib/sync';
import fs from 'fs';

import { jsonPretty } from './json-util';
import { Logger } from './logger';

const logger = Logger.create('GMS_LOG_FILE_SYSTEM_UTIL', process.env.GMS_LOG_FILE_SYSTEM_UTIL);

/**
 * Reads the provided source JSON file into memory
 *
 * @param jsonFilePath The JSON filename from which to read the JSON content
 */
// !FIX: This should be generic and certainly should not always return an array
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function readJsonData<T = any>(jsonFilePath: string): T[] {
  const fileContents = fs.readFileSync(jsonFilePath, 'utf8');
  return JSON.parse(fileContents);
}

/**
 * Write the formatted JSON to file used by other write to file functions
 *
 * @param filePath
 * @param json
 */
export function writeJsonToFile(filePath: string, json: string): void {
  fs.writeFile(filePath, json, err => {
    if (err) {
      logger.warn(err.message);
    }
  });
}
/**
 * Writes provided object to file stringify and pretty
 *
 * @param object object to stringify and written to a file
 * @param fileName filename doNOT include extension
 */
export function writeJsonPretty(object: unknown, fileName: string): void {
  writeJsonToFile(`${fileName}.json`, jsonPretty(object));
}

/**
 * Saves given object to file
 * This will save us time from copying data from the terminal
 *
 * @param object object to save to file
 * @param filePath file path including file name
 */
export function writeObjectToJsonFile(object: unknown, filePath: string): void {
  writeJsonToFile(filePath, JSON.stringify(object, undefined, 2));
}

/** Utility functions for handling CSV files and other file related utils */

/**
 * Reads the provided source CSV file into memory
 *
 * @param csvFilePath
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function readCsvData(csvFilePath: string): any[] {
  const fileContents = fs.readFileSync(csvFilePath, 'utf8');
  return parse(fileContents, { columns: true, delimiter: '\t' });
}

/**
 * Resolves the home value in the config and returns the path
 *
 * @param configString
 */
export function resolveHomeDataPath(configString: string): string[] {
  // Resolve the ${HOME} value or ${SOMETHING} value
  const regex = new RegExp(''.concat('\\$\\{', '([^}]+)', '\\}'), 'g');
  return [configString.replace(regex, (_, v) => process.env[v])];
}
