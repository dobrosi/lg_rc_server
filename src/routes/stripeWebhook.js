const express = require('express');
const router = express.Router();
const stripeService = require('../services/stripeService');

// A Stripe webhook nyers body-t igényel
router.post('/webhook', express.raw({ type: 'application/json' }), async (req, res) => {
  const sig = req.headers['stripe-signature'];

  try {
    await stripeService.handleWebhook(req.body, sig);
    res.json({ received: true });
  } catch (err) {
    console.error(`Webhook hiba: ${err.message}`);
    res.status(400).send(`Webhook Error: ${err.message}`);
  }
});

router.get('/hello', (req, res) => {
  res.send('Hello, World!');
});

module.exports = router;
