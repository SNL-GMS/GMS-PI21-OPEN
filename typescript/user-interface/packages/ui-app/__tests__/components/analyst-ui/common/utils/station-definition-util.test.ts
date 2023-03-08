import type { ChannelTypes } from '@gms/common-model';
import clone from 'lodash/clone';

import { sortStationDefinitionChannels } from '../../../../../src/ts/components/analyst-ui/common/utils/station-definition-util';

const threeCOrientations = [
  ['Z', 'N', 'E'],
  ['Z', '1', '2'],
  ['U', 'V', 'W'],
  ['L', 'Q', 'T'],
  ['Z', 'R', 'T']
];

function buildChannels(
  channelGroupName: string,
  channelOrientations: string[]
): Partial<ChannelTypes.Channel>[] {
  return channelOrientations.map(co => {
    return {
      name: `${channelGroupName}${co}`
    };
  });
}

function validateChannelOrder(
  channels: ChannelTypes.Channel[],
  channelOrientationOrder: string[]
): boolean {
  // Should be 3 channels with the names ending in the correct order
  if (
    channels[0].name.endsWith(channelOrientationOrder[0]) &&
    channels[1].name.endsWith(channelOrientationOrder[1]) &&
    channels[2].name.endsWith(channelOrientationOrder[2])
  ) {
    return true;
  }
  return false;
}
/**
 * Tests the ability to sort various 3C channels properly
 */
describe('Station Definition Util Tests', () => {
  test('Sort station definition 3C channels for valid orientation list', () => {
    threeCOrientations.forEach(orientations => {
      const reverseOrientations = clone(orientations).reverse();
      const channels = buildChannels('station.site.xy', reverseOrientations);
      const sortedChannels = sortStationDefinitionChannels(
        (channels as unknown) as ChannelTypes.Channel[]
      );
      expect(validateChannelOrder(sortedChannels, orientations)).toBeTruthy();
    });
  });

  test('Sort channels for invalid orientation list', () => {
    const badOrientations = ['A', 'B', 'C'];
    const reverseOrientations = clone(badOrientations).reverse();
    const channels = buildChannels('station.site.xy', reverseOrientations);
    const sortedChannels = sortStationDefinitionChannels(
      (channels as unknown) as ChannelTypes.Channel[]
    );
    expect(validateChannelOrder(sortedChannels, badOrientations)).toBeTruthy();
  });

  test('Sort station definition 3C channels for multiple groups', () => {
    const reverseOrientations = clone(threeCOrientations[0]).reverse();
    let channels = buildChannels('station.site3.xy', reverseOrientations);
    channels = channels.concat(buildChannels('station.site2.xy', reverseOrientations));
    channels = channels.concat(buildChannels('station.site1.xy', reverseOrientations));
    const sortedChannels = sortStationDefinitionChannels(
      (channels as unknown) as ChannelTypes.Channel[]
    );
    expect(sortedChannels).toMatchSnapshot();
  });

  test('Sort station definition 3C channels for multiple stations and groups', () => {
    const reverseOrientations = clone(threeCOrientations[0]).reverse();
    let channels = buildChannels('station3.site.xy', reverseOrientations);
    channels = channels.concat(buildChannels('station1.site1.xy', reverseOrientations));
    channels = channels.concat(buildChannels('station2.site.xy', reverseOrientations));
    channels = channels.concat(buildChannels('station1.site2.xy', reverseOrientations));
    channels = channels.concat(buildChannels('station3.site1.xy', reverseOrientations));
    channels = channels.concat(buildChannels('station1.site.xy', reverseOrientations));
    const sortedChannels = sortStationDefinitionChannels(
      (channels as unknown) as ChannelTypes.Channel[]
    );
    expect(sortedChannels).toMatchSnapshot();
  });
});
