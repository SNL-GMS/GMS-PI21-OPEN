import flatMap from 'lodash/flatMap';
import max from 'lodash/max';
import min from 'lodash/min';

/**
 * Defines a simple Range with a start and end value.
 */
export interface Range {
  start: number;
  end: number;
}

/**
 * Merges any overlapping of the provided ranges.
 * For example,
 * `mergeRanges([3-5, 2-7, 1-2, 9-10]) would return [1-7, 9-10]`.
 *
 * @param ranges the ranges to merge
 * @returns the merged ranges
 */
export const mergeRanges = (ranges: Range[]): Range[] => {
  if (!(ranges && ranges.length > 0)) {
    return [];
  }

  // stack of final ranges
  const stack: Range[] = [];

  // sort according to start value
  ranges.sort((a, b) => a.start - b.start);

  // add first range to stack
  stack.push(ranges[0]);

  ranges.slice(1).forEach(range => {
    const top = stack[stack.length - 1];

    if (top.end < range.start) {
      // no overlap, push range onto stack
      stack.push(range);
    } else if (top.end < range.end) {
      // update previous range
      top.end = range.end;
    }
  });
  return stack;
};

/**
 * Determines the excluded ranges for the provided range.
 * For the provided `range`, determine any new ranges that are not contained within the
 * provided `ranges`. For example,
 * `determineExcludedRanges([3-5, 5-7], [1-9]) would return [1-3, 7-9]`.
 *
 * @param ranges the ranges to compare `range` to when determine any excluded ranges
 * @param range the range to check
 * @returns any excluded ranges contained by `range`
 */
export const determineExcludedRanges = (ranges: Range[], range: Range): Range[] => {
  if (!range) {
    return [];
  }

  if (!(ranges && ranges.length > 0)) {
    return [range];
  }

  // merge the ranges
  const mergedRanges = mergeRanges(ranges);

  let stack = [range];
  mergedRanges.forEach(r => {
    stack = stack.filter(
      s =>
        min([s.start, s.end]) < min([r.start, r.end]) ||
        max([s.start, s.end]) > max([r.start, r.end])
    );

    stack.forEach((s, i) => {
      if (s.start <= r.start && s.end <= r.end) {
        stack[i].end = r.start;
      } else if (s.start >= r.start && s.end >= r.end) {
        stack[i].start = r.end;
      } else if (s.start <= r.start && s.end >= r.end) {
        stack.push({
          start: r.end,
          end: s.end
        });
        stack[i].end = r.start;
      }
    });
  });
  return stack;
};

/**
 * Chunks up a range (start and end) into multiple small ranges where
 * each range is no larger than the maximum size.
 *
 * @param range the range to chunk
 * @param maxSize the maximum range size to return (the chunk size)
 * @returns an array of ranges where the size is <= the maximum size
 */
export const chunkRange = (range: Range, maxSize: number): Range[] => {
  const chunkedRanges: Range[] = [];

  if (range === undefined) {
    return [];
  }

  if (maxSize === undefined || maxSize <= 0) {
    return [range];
  }

  let position = range.start;
  let duration = range.end - range.start;
  while (duration > 0) {
    const chunkSize = duration < maxSize ? duration : maxSize;
    chunkedRanges.push({ start: position, end: position + chunkSize });
    position += chunkSize;
    duration -= chunkSize;
  }
  return chunkedRanges;
};

/**
 * Chunks up the ranges into multiple small ranges where
 * each range is no larger than the maximum size.
 *
 * @param ranges the ranges to chunk
 * @param maxSize the maximum range size to return (the chunk size)
 * @returns an array of ranges where the size is <= the maximum size
 */
export const chunkRanges = (ranges: Range[], maxSize: number): Range[] => {
  const chunkedRanges: Range[] = [];

  if (ranges === undefined || ranges.length === 0) {
    return [];
  }

  if (maxSize === undefined || maxSize <= 0) {
    return ranges;
  }

  ranges.forEach(range => {
    chunkedRanges.push(...chunkRange(range, maxSize));
  });
  return flatMap(chunkedRanges);
};
