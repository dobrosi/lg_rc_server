/** @type { import("drizzle-kit").Config } */
module.exports = {
  schema: "./src/models/schema.js",
  out: "./drizzle",
  dialect: 'postgresql',
  dbCredentials: {
    url: process.env.POSTGRES_URL || process.env.POSTGRES_JDBC_URL,
  },
};
