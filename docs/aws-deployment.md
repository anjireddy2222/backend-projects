# Optional AWS classroom deployment

This is a deployment exercise, not an already-provisioned AWS environment. No AWS resources are created by the repository. The same Java applications can run on EC2 and connect to RDS MySQL without an AWS SDK.

1. Build the JARs locally with `mvnw.cmd clean verify` (or `bash mvnw clean verify`). Find each JAR in its service's `target` folder.
2. Create a Linux EC2 instance with Java 17 and an RDS MySQL instance in the same VPC. Keep RDS private and permit its port 3306 from the EC2 instance's security group. AWS documents [EC2-to-RDS connectivity](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/ec2-rds-connect.html).
3. Connect to RDS from EC2 as the database administrator. Create `amazon_catalog` and `amazon_customer`; create separate application database users and grant each access only to its own database. Create `helpdesk` if deploying that project too. Use the local SQL as a schema-name reference, but choose new credentials.
4. Copy the JARs to EC2. Set each process's `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` and, for authenticated applications, a strong `JWT_SECRET`. Use a different JWT secret for each application. Configure a real SMTP server through the documented `MAIL_*` variables. RDS supports [TLS-protected MySQL connections](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/securing-mysql-connections.html); configure the RDS CA certificate and Connector/J verification for your environment.
5. Start the catalog and customer service separately. Set `CATALOG_URL=http://127.0.0.1:8081` if both run on the same instance; use the catalog's private hostname otherwise. Set up systemd services to restart each Java process automatically.
6. Put an HTTPS reverse proxy in front of the APIs. Permit public traffic only to that HTTPS entry point; restrict SSH to your own address. Keep database and internal service ports private. The local Compose file binds ports to loopback and is intended for local teaching, not a public deployment template.

For example, in the customer service process environment:

```bash
export DB_URL='jdbc:mysql://YOUR_RDS_ENDPOINT:3306/amazon_customer?sslMode=VERIFY_IDENTITY'
export DB_USERNAME='amazon_customer_app'
# Supply DB_PASSWORD and JWT_SECRET through protected environment configuration.
export CATALOG_URL='http://127.0.0.1:8081'
java -jar customer-service-1.0.0.jar
```

The JPA table creation setting is convenient for the first lesson. For a maintained deployment, replace it with versioned migrations and use `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`. Do not enable demo account seeding publicly. Budget for AWS usage and remove the exercise resources when finished.
