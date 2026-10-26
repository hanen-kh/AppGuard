# Local configuration

Set these environment variables before starting the main Spring Boot backend:

| Variable | Required | Purpose |
| --- | --- | --- |
| `DB_USERNAME` | Yes | MySQL username |
| `DB_PASSWORD` | Yes | MySQL password |
| `DB_URL` | No | MySQL JDBC URL; defaults to the local `appguard` database |
| `MONGODB_URI` | No | MongoDB connection URI; defaults to unauthenticated local MongoDB |
| `JWT_SECRET` | Yes | Base64 encoded JWT signing key; use at least 32 random bytes |
| `MAIL_USERNAME` | No | SMTP account username |
| `MAIL_PASSWORD` | No | SMTP account password or app password |
| `KAFKA_BOOTSTRAP_SERVERS` | No | Kafka broker address; defaults to `localhost:9092` |

Example for a local development shell (replace the database and mail values):

```sh
export DB_USERNAME='appguard'
export DB_PASSWORD='replace-with-your-local-database-password'
export JWT_SECRET="$(openssl rand -base64 32)"
export MONGODB_URI='mongodb://localhost:27017/log'
export KAFKA_BOOTSTRAP_SERVERS='localhost:9092'
```

The cron agent also requires `AGENT_API_KEY` when starting. Configure it with the key assigned to that server in the backend; the agent and backend values must match:

```sh
export AGENT_API_KEY='replace-with-the-server-agent-key'
```

Set these variables in the IDE run configuration or deployment environment for each process. Do not commit real values or paste them into source files. Rotate any credentials that were previously committed; removing a secret from the current files does not erase it from Git history.
