const express = require('express');
const router = express.Router();
const stripeService = require('../services/stripeService');

// A Java projektben ez a végpont volt megadva a Stripe-ban a webhook eseményekhez
router.post('/webhook-success', express.raw({ type: 'application/json' }), async (req, res) => {
  const sig = req.headers['stripe-signature'];

  try {
    await stripeService.handleWebhook(req.body, sig);
    res.json({ received: true });
  } catch (err) {
    console.error(`Webhook hiba (webhook-success): ${err.message}`);
    res.status(400).send(`Webhook Error: ${err.message}`);
  }
});

router.get('/hello', (req, res) => {
  res.send('Hello, World!');
});

// Visszatérési URL-ek a böngésző számára (GET)
router.get('/webhook-success', (req, res) => {
  res.redirect(302, `${process.env.SERVER_URL}/public/index.html?success=true`);
});

router.get('/webhook-cancel', (req, res) => {
  res.redirect(302, `${process.env.SERVER_URL}/public/index.html?cancel=true`);
});

module.exports = router;
