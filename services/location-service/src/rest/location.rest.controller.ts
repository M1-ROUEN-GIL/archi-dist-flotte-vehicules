// src/rest/location.rest.controller.ts
import { Controller, Get, Param, Query } from '@nestjs/common';
import { LocationService } from '../location/location.service';

@Controller('locations')
export class LocationRestController {

    constructor(private readonly locationService: LocationService) {}

    // GET /locations/:vehicleId/latest
    @Get(':vehicleId/latest')
    getLatest(@Param('vehicleId') vehicleId: string) {
        return this.locationService.getLatestPosition(vehicleId);
    }

    // GET /locations/:vehicleId/history
    @Get(':vehicleId/history')
    getHistory(
        @Param('vehicleId') vehicleId: string,
        @Query('from') from: string,
        @Query('to') to: string,
        @Query('limit') limit = 100,
        @Query('offset') offset = 0,
    ) {
        const fromDate = from ? new Date(from) : new Date(Date.now() - 24 * 60 * 60 * 1000);
        const toDate   = to   ? new Date(to)   : new Date();
        return this.locationService.getHistory(vehicleId, fromDate, toDate, +limit, +offset);
    }

    // GET /locations/:vehicleId/stats
    @Get(':vehicleId/stats')
    getStats(
        @Param('vehicleId') vehicleId: string,
        @Query('from') from: string,
        @Query('to') to: string,
    ) {
        const fromDate = from ? new Date(from) : new Date(Date.now() - 24 * 60 * 60 * 1000);
        const toDate   = to   ? new Date(to)   : new Date();
        return this.locationService.getStats(vehicleId, fromDate, toDate);
    }
}