const { db, customers } = require('../models/db');
const { eq } = require('drizzle-orm');

class CustomerService {
  async findOrCreateByKeycloakId(keycloakId) {
    let customer = await db.query.customers.findFirst({
      where: eq(customers.keycloakId, keycloakId),
    });

    if (!customer) {
      const inserted = await db.insert(customers).values({ keycloakId }).returning();
      customer = inserted[0];
    }
    return customer;
  }

  async findByStripeCustomerId(stripeCustomerId) {
    return await db.query.customers.findFirst({
      where: eq(customers.stripeCustomerId, stripeCustomerId),
    });
  }
}

module.exports = new CustomerService();
