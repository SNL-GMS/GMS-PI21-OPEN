package gms.shared.utilities.coidataloader;

import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class CoiLoaderTests {

  @Mock
  private OsdRepositoryInterface osd;

  private CoiLoader loader;

  @BeforeEach
  void init() {
    loader = CoiLoader.create(osd);
  }

  @Test
  void testLoadEmptyDataSet() {
    loader.load(CoiDataSet.builder().build());
    verifyNoInteractions(osd);
  }

  @Test
  void testLoadFullDataSet() {
    final CoiDataSet data = MockData.createDataSet();
    loader.load(data);

    // check that some data is done in a particular order.
    // storing station groups should happen first since waveforms may be on channels in those station groups.
    // storing waveforms should come before storing e.g. signal detections
    // since those detections may have feature measurements that refer to channels
    // stored implicitly in storing those waveforms.
    final InOrder inOrder = Mockito.inOrder(osd);
    inOrder.verify(osd).storeStationGroups(data.getStationGroups());

    final StationReference staRef = data.getStationReference();
    verify(osd).storeReferenceNetwork(staRef.getNetworks());
    verify(osd).storeReferenceStation(staRef.getStations());
    verify(osd).storeReferenceSites(staRef.getSites());
    verify(osd).storeReferenceChannels(staRef.getChannels());
    verify(osd).storeReferenceSensors(staRef.getSensors());
    verify(osd).storeReferenceResponses(staRef.getResponses());
    verify(osd).storeNetworkMemberships(staRef.getNetworkMemberships());
    verify(osd).storeStationMemberships(staRef.getStationMemberships());
    verify(osd).storeSiteMemberships(staRef.getSiteMemberships());
    verifyNoMoreInteractions(osd);
  }
}
