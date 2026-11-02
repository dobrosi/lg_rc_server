const { db, devices } = require('../models/db');
const { eq } = require('drizzle-orm');
const customerService = require('./customerService');

class DeviceService {
  async getDevices(keycloakId) {
    const customer = await customerService.findOrCreateByKeycloakId(keycloakId);
    const results = await db.query.devices.findMany({
      where: eq(devices.customerKeycloakId, keycloakId),
    });
    return results.map(d => ({
      ip: d.ip,
      clientKey: d.clientKey,
      name: d.name,
      model: d.model
    }));
  }

  async add(keycloakId, deviceDto) {
    const customer = await customerService.findOrCreateByKeycloakId(keycloakId);
    await db.insert(devices).values({
      ip: deviceDto.ip,
      clientKey: deviceDto.clientKey,
      name: deviceDto.name,
      model: deviceDto.model,
      customerKeycloakId: keycloakId
    });
  }

  async saveDevices(keycloakId, deviceDtos) {
    await db.transaction(async (tx) => {
      await tx.delete(devices).where(eq(devices.customerKeycloakId, keycloakId));
      
      if (deviceDtos.length > 0) {
        await tx.insert(devices).values(
          deviceDtos.map(dto => ({
            ip: dto.ip,
            clientKey: dto.clientKey,
            name: dto.name,
            model: dto.model,
            customerKeycloakId: keycloakId
          }))
        );
      }
    });
  }
}

module.exports = new DeviceService();
