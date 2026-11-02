const express = require('express');
const router = express.Router();
const customerService = require('../services/customerService');
const stripeService = require('../services/stripeService');

router.get('/', async (req, res) => {
  try {
    const keycloakId = req.auth.payload.sub;
    const customer = await customerService.findOrCreateByKeycloakId(keycloakId);
    res.json({
      keycloakId: customer.keycloakId,
      premium: customer.isPremium
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.post('/checkout', async (req, res) => {
  try {
    const keycloakId = req.auth.payload.sub;
    const email = req.auth.payload.email;
    const { planId } = req.query;

    const checkoutUrl = await stripeService.createCheckoutSession(keycloakId, email, planId);
    res.json({ url: checkoutUrl });
  } catch (err) {
    console.error(`Stripe hiba: ${err.message}`);
    res.status(500).json({ error: `Stripe hiba: ${err.message}` });
  }
});

router.post('/customer-portal', async (req, res) => {
  try {
    const keycloakId = req.auth.payload.sub;
    const { queryParts } = req.query;
    const portalUrl = await stripeService.createCustomerPortalUrl(keycloakId, queryParts);
    res.redirect(302, portalUrl);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
