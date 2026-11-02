CREATE TABLE "Customers" (
	"keycloakId" varchar PRIMARY KEY NOT NULL,
	"stripeCustomerId" varchar,
	"isPremium" boolean DEFAULT false NOT NULL,
	CONSTRAINT "Customers_keycloakId_unique" UNIQUE("keycloakId"),
	CONSTRAINT "Customers_stripeCustomerId_unique" UNIQUE("stripeCustomerId")
);
--> statement-breakpoint
CREATE TABLE "Devices" (
	"id" serial PRIMARY KEY NOT NULL,
	"ip" varchar NOT NULL,
	"clientKey" varchar NOT NULL,
	"name" varchar,
	"model" varchar,
	"CustomerKeycloakId" varchar
);
--> statement-breakpoint
ALTER TABLE "Devices" ADD CONSTRAINT "Devices_CustomerKeycloakId_Customers_keycloakId_fk" FOREIGN KEY ("CustomerKeycloakId") REFERENCES "public"."Customers"("keycloakId") ON DELETE cascade ON UPDATE no action;