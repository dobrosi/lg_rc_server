const express = require('express');
const router = express.Router();
const deviceService = require('../services/deviceService');

router.get('/list', async (req, res) => {
  try {
    const keycloakId = req.auth.payload.sub;
    const devices = await deviceService.getDevices(keycloakId);
    res.json(devices);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.post('/save', async (req, res) => {
  try {
    const keycloakId = req.auth.payload.sub;
    const deviceDtos = req.body;
    await deviceService.saveDevices(keycloakId, deviceDtos);
    res.status(200).send();
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.post('/add', async (req, res) => {
  try {
    const keycloakId = req.auth.payload.sub;
    const deviceDto = req.body;
    await deviceService.add(keycloakId, deviceDto);
    res.status(200).send();
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
