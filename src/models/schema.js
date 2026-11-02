const { pgTable, serial, varchar, boolean } = require('drizzle-orm/pg-core');
const { relations } = require('drizzle-orm');

const customers = pgTable('Customers', {
  keycloakId: varchar('keycloakId').primaryKey().notNull().unique(),
  stripeCustomerId: varchar('stripeCustomerId').unique(),
  isPremium: boolean('isPremium').default(false).notNull(),
});

const devices = pgTable('Devices', {
  id: serial('id').primaryKey(),
  ip: varchar('ip').notNull(),
  clientKey: varchar('clientKey').notNull(),
  name: varchar('name'),
  model: varchar('model'),
  customerKeycloakId: varchar('CustomerKeycloakId').references(() => customers.keycloakId, { onDelete: 'cascade' }),
});

const customersRelations = relations(customers, ({ many }) => ({
  devices: many(devices),
}));

const devicesRelations = relations(devices, ({ one }) => ({
  customer: one(customers, {
    fields: [devices.customerKeycloakId],
    references: [customers.keycloakId],
  }),
}));

module.exports = {
  customers,
  devices,
  customersRelations,
  devicesRelations,
};
