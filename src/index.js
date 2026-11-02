require('dotenv').config();
const express = require('express');
const cors = require('cors');
const morgan = require('morgan');
const path = require('path');
const { auth } = require('express-oauth2-jwt-bearer');
const { db } = require('./models/db');

const customerRoutes = require('./routes/customerRoutes');
const deviceRoutes = require('./routes/deviceRoutes');
const stripeWebhook = require('./routes/stripeWebhook');

const app = express();
const PORT = process.env.PORT || 8085;

// Middleware-ek
app.use(cors());
app.use(morgan('dev'));

// Stripe webhook (külön router, mert nyers body kell neki)
app.use('/stripe', stripeWebhook);

// Általános JSON parser a többi route-hoz
app.use(express.json());

// Statikus fájlok kiszolgálása
app.use('/public', express.static(path.join(__dirname, 'static/public')));

// OAuth2 védelem (Keycloak)
const jwtCheck = auth({
  audience: process.env.AUDIENCE || 'account',
  issuerBaseURL: process.env.ISSUER_BASE_URL || 'https://auth.pgy.no-ip.hu/realms/pgy',
  tokenSigningAlg: 'RS256'
});

// Védett route-ok
app.use('/customer', jwtCheck, customerRoutes);
app.use('/device', jwtCheck, deviceRoutes);

// Egyszerű egészségügyi ellenőrzés
app.get('/health', (req, res) => res.send('OK'));

app.listen(PORT, () => {
  console.log(`LG RC Server (Node.js) fut a ${PORT} porton.`);
});
