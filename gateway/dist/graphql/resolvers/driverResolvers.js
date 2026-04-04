export const driverResolvers = {
    Query: {
        drivers: async (_, args, ctx) => {
            const response = await ctx.driver.listDrivers(args);
            // If the service returns a direct array, we wrap it into a page object as expected by the schema
            if (Array.isArray(response)) {
                return {
                    items: response,
                    total_count: response.length,
                };
            }
            return response;
        },
        driver: async (_, args, ctx) => {
            return ctx.driver.getDriver(args.id);
        },
    },
    Mutation: {
        createDriver: async (_, args, ctx) => {
            return ctx.driver.createDriver(args);
        },
        updateDriverStatus: async (_, args, ctx) => {
            return ctx.driver.updateDriverStatus(args.id, args.status);
        },
    },
    Driver: {
        license: async (parent, _, ctx) => {
            const licenses = await ctx.driver.getLicenses(parent.id);
            return licenses && licenses.length > 0 ? licenses[0] : null;
        },
    }
};
