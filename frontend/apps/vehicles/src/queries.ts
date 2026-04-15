import { gql } from '@apollo/client';

// 1. Requête pour récupérer la liste des véhicules
export const GET_VEHICLES = gql`
  query GetVehicles($status: VehicleStatus, $limit: Int, $offset: Int) {
    vehicles(status: $status, limit: $limit, offset: $offset) {
      id
      plate_number
      brand
      model
      fuel_type
      status
      mileage_km
    }
  }
`;

// 2. Mutation pour supprimer un véhicule (on la prépare pour plus tard)
export const DELETE_VEHICLE = gql`
  mutation DeleteVehicle($id: ID!) {
    deleteVehicle(id: $id)
  }
`;

// 2. Mutation de Création
export const CREATE_VEHICLE = gql`
  mutation CreateVehicle(
    $plate_number: String!, 
    $brand: String!, 
    $model: String!, 
    $fuel_type: FuelType!, 
    $mileage_km: Int!,
    $payload_capacity_kg: Int,
    $cargo_volume_m3: Float
  ) {
    createVehicle(
      plate_number: $plate_number,
      brand: $brand,
      model: $model,
      fuel_type: $fuel_type,
      mileage_km: $mileage_km,
      payload_capacity_kg: $payload_capacity_kg,
      cargo_volume_m3: $cargo_volume_m3
    ) {
      id
      plate_number
      brand
      model
      status
    }
  }
`;

export const UPDATE_VEHICLE = gql`
  mutation UpdateVehicle($id: ID!, $brand: String, $model: String, $mileage_km: Int, $vin: String) {
    updateVehicle(id: $id, brand: $brand, model: $model, mileage_km: $mileage_km, vin: $vin) {
      id
      brand
      model
    }
  }
`;