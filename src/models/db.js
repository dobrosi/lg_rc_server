const { drizzle } = require('drizzle-orm/node-postgres');
const { Pool } = require('pg');
const schema = require('./schema');
require('dotenv').config();

const connectionString =
  process.env.POSTGRES_URL ||
  process.env.POSTGRES_PRISMA_URL ||
  process.env.POSTGRES_JDBC_URL;

if (!connectionString) {
  throw new Error('Missing POSTGRES_URL: configure the Supabase Postgres connection string.');
}

const databaseUrl = new URL(connectionString);
if (databaseUrl.searchParams.get('sslmode') === 'require') {
  databaseUrl.searchParams.set('uselibpqcompat', 'true');
}

const pool = new Pool({
  connectionString: databaseUrl.toString(),
});

const db = drizzle(pool, { schema });

module.exports = {
  db,
  ...schema,
};
