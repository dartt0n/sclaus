CREATE TYPE languages AS ENUM ('RUS', 'ENG');
CREATE TYPE states AS ENUM (
    'READY',
    'LATECOMER',
    'GREETING_ANSWERED',
    'RULES_ANSWERED',
    'TIMELINE_ANSWERED',
    'PREFERENCES_ANSWERED',
    'REGISTRATION_COMPLETE',
    'TARGET_RECEIVED'
);

CREATE TABLE users
(
    "id" BIGINT NOT NULL,
    "createdAt" TIMESTAMP WITH TIME ZONE NOT NULL,
    "updatedAt" TIMESTAMP WITH TIME ZONE NOT NULL,
    "deletedAt" TIMESTAMP WITH TIME ZONE,
    "firstName" TEXT,
    "lastName" TEXT,
    "username" TEXT,
    "language" languages,
    "preferences" TEXT[],
    "state" states,
    "target" BIGINT,
    CONSTRAINT users_pkey PRIMARY KEY ("id"),
    CONSTRAINT fk_target FOREIGN KEY ("target") REFERENCES users("id")
);

CREATE INDEX users_deletedAt_idx ON users ("deletedAt") WHERE "deletedAt" IS NOT NULL;
