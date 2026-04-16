import { gql } from '@apollo/client';

export const GET_VEHICLE_LOCATION = gql`
  query GetVehicleLocation($vehicle_id: ID!) {
    vehicleLocation(vehicle_id: $vehicle_id) {
      latitude
      longitude
      speed_kmh
      heading_deg
      recorded_at
    }
  }
`;

export const WATCH_VEHICLE_LOCATION = gql`
  subscription WatchVehicleLocation($vehicle_id: ID!) {
    vehicleLocationUpdated(vehicle_id: $vehicle_id) {
      latitude
      longitude
      speed_kmh
      heading_deg
      recorded_at
    }
  }
`;