import { CommonTypes, LegacyEventTypes, SignalDetectionTypes } from '@gms/common-model';

export const mockDistanceToStations: any[] = [
  {
    stationName: 'fd3dbadc-72fd-36c6-a3cc-ca4f7a4f58be',
    distance: 5
  },
  {
    stationName: '7a481f10-e4d3-3687-9efa-622a82eb92cf',
    distance: 13
  }
];

export const eventHypothesisWithLocationSets: LegacyEventTypes.EventHypothesis = {
  id: '3a06fac7-46ad-337e-a8da-090a1cc801a1',
  rejected: false,
  event: {
    id: '3a06fac7-46ad-337e-a8da-090a1cc801a1',
    status: LegacyEventTypes.EventStatus.OpenForRefinement
  },
  preferredLocationSolution: {
    locationSolution: {
      id: '3a06fac7-46ad-337e-a8da-090a1cc801a1',
      locationType: 'standard',
      location: {
        latitudeDegrees: 44.94862,
        longitudeDegrees: -106.38442,
        depthKm: 0,
        time: 1274392890.238
      },
      locationToStationDistances: [
        {
          distance: {
            degrees: 10,
            km: 10
          },
          azimuth: 1,
          stationId: '3308666b-f9d8-3bff-a59e-928730ffa797'
        }
      ],
      snapshots: [
        {
          signalDetectionId: 'db31fbe6-322f-3e91-911c-578f22f4234b',
          signalDetectionHypothesisId: 'db31fbe6-322f-3e91-911c-578f22f4234b',
          stationName: 'PDAR',
          channelName: 'fkb',
          phase: CommonTypes.PhaseType.Pg,
          time: {
            defining: true,
            observed: 1274392950.85,
            residual: null,
            correction: null
          },
          slowness: {
            defining: false,
            observed: 18.65,
            residual: -0.4,
            correction: null
          },
          azimuth: {
            defining: true,
            observed: 52.56,
            residual: -1.4,
            correction: null
          }
        }
      ],
      featurePredictions: [],
      locationRestraint: {
        depthRestraintType: LegacyEventTypes.DepthRestraintType.UNRESTRAINED,
        depthRestraintKm: null,
        latitudeRestraintType: LegacyEventTypes.RestraintType.UNRESTRAINED,
        latitudeRestraintDegrees: null,
        longitudeRestraintType: LegacyEventTypes.RestraintType.UNRESTRAINED,
        longitudeRestraintDegrees: null,
        timeRestraintType: LegacyEventTypes.RestraintType.UNRESTRAINED,
        timeRestraint: null
      },
      locationUncertainty: {
        xy: -272.5689,
        xz: -1,
        xt: -33.5027,
        yy: 525.9998,
        yz: -1,
        yt: -30.6263,
        zz: -1,
        zt: -1,
        tt: 9.7391,
        stDevOneObservation: 0,
        ellipses: [
          {
            scalingFactorType: LegacyEventTypes.ScalingFactorType.CONFIDENCE,
            kWeight: 0,
            confidenceLevel: 0.9,
            majorAxisLength: '59.7138',
            majorAxisTrend: 137.57,
            minorAxisLength: '32.3731',
            minorAxisTrend: -1,
            depthUncertainty: -1,
            timeUncertainty: 'PT5.137S'
          }
        ],
        ellipsoids: []
      },
      locationBehaviors: [
        {
          residual: 0,
          weight: 1.01,
          defining: true,
          signalDetectionId: '00000000-0000-0000-0000-000000000000',
          featureMeasurementType: SignalDetectionTypes.FeatureMeasurementType.AMPLITUDE
        }
      ],
      networkMagnitudeSolutions: []
    }
  },
  associationsMaxArrivalTime: 1274392950.85,
  signalDetectionAssociations: [
    {
      id: '1522eea6-b188-421e-9c1f-e40c6066b841',
      rejected: false,
      eventHypothesisId: '3a06fac7-46ad-337e-a8da-090a1cc801a1',
      signalDetectionHypothesis: {
        id: 'db31fbe6-322f-3e91-911c-578f22f4234b',
        rejected: false,
        parentSignalDetectionId: 'db31fbe6-322f-3e91-911c-578f22f4234b'
      }
    }
  ],
  locationSolutionSets: [
    {
      id: '5fa862f1-2e2d-4d87-bc66-00177de18c5e',
      count: 0,
      locationSolutions: [
        {
          id: '3a06fac7-46ad-337e-a8da-090a1cc801a1',
          locationType: 'standard',
          location: {
            latitudeDegrees: 44.94862,
            longitudeDegrees: -106.38442,
            depthKm: 0,
            time: 1274392890.238
          },
          locationToStationDistances: [
            {
              distance: {
                degrees: 10,
                km: 10
              },
              azimuth: 1,
              stationId: '3308666b-f9d8-3bff-a59e-928730ffa797'
            }
          ],
          snapshots: [
            {
              signalDetectionId: 'db31fbe6-322f-3e91-911c-578f22f4234b',
              signalDetectionHypothesisId: 'db31fbe6-322f-3e91-911c-578f22f4234b',
              stationName: 'PDAR',
              channelName: 'fkb',
              phase: CommonTypes.PhaseType.Pg,
              time: {
                defining: true,
                observed: 1274392950.85,
                residual: null,
                correction: null
              },
              slowness: {
                defining: false,
                observed: 18.65,
                residual: -0.4,
                correction: null
              },
              azimuth: {
                defining: true,
                observed: 52.56,
                residual: -1.4,
                correction: null
              }
            }
          ],
          featurePredictions: [],
          locationRestraint: {
            depthRestraintType: LegacyEventTypes.DepthRestraintType.UNRESTRAINED,
            depthRestraintKm: null,
            latitudeRestraintType: LegacyEventTypes.RestraintType.UNRESTRAINED,
            latitudeRestraintDegrees: null,
            longitudeRestraintType: LegacyEventTypes.RestraintType.UNRESTRAINED,
            longitudeRestraintDegrees: null,
            timeRestraintType: LegacyEventTypes.RestraintType.UNRESTRAINED,
            timeRestraint: null
          },
          locationUncertainty: {
            xy: -272.5689,
            xz: -1,
            xt: -33.5027,
            yy: 525.9998,
            yz: -1,
            yt: -30.6263,
            zz: -1,
            zt: -1,
            tt: 9.7391,
            stDevOneObservation: 0,
            ellipses: [
              {
                scalingFactorType: LegacyEventTypes.ScalingFactorType.CONFIDENCE,
                kWeight: 0,
                confidenceLevel: 0.9,
                majorAxisLength: '59.7138',
                majorAxisTrend: 137.57,
                minorAxisLength: '32.3731',
                minorAxisTrend: -1,
                depthUncertainty: -1,
                timeUncertainty: 'PT5.137S'
              }
            ],
            ellipsoids: []
          },
          locationBehaviors: [
            {
              residual: 0,
              weight: 1.01,
              defining: true,
              signalDetectionId: '00000000-0000-0000-0000-000000000000',
              featureMeasurementType: SignalDetectionTypes.FeatureMeasurementType.AMPLITUDE
            }
          ],
          networkMagnitudeSolutions: []
        }
      ]
    }
  ]
};

export const event: LegacyEventTypes.Event = {
  id: '186f997b-7d7d-3151-8b4d-5609f7a8f31f',
  status: LegacyEventTypes.EventStatus.ReadyForRefinement,
  modified: false,
  hasConflict: false,
  conflictingSdIds: [],
  currentEventHypothesis: {
    eventHypothesis: eventHypothesisWithLocationSets,
    processingStage: {
      id: 'wowow'
    }
  }
};
