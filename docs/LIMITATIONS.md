# Operating and integration boundaries

The earlier missing-module backlog has been implemented in this continuation.
These boundaries describe what the application actually does and the
remaining work required for a particular live hospital deployment.

## External systems

- Public online registration payment is deliberately a simulation.
  It is stored outside real revenue. Cash/Card/UPI/Bank receipts require staff
  verification; the application does not debit an account or contact a gateway.
- Salary payment is reconciled with a verified bank reference; no bank transfer
  is initiated. Basic/allowances/deductions are entered by HR. Organization-specific
  tax, statutory payroll and attendance-to-pay rules require configuration and
  independent validation rather than assumed automatic legal calculations.
- Insurance preauthorization/enhancement/final decisions are entered from TPA
  responses. No live insurer connection is configured. Cashless receipts are
  allocated/reconciled against invoices; reimbursement receipts are not hospital
  revenue. Post-settlement insurer refund/reopening automation is not supplied.
  Credits that would refund insurer money are blocked for authorized reconciliation.
- Notifications are in-application records. Email/SMS/WhatsApp delivery, live PACS,
  laboratory instrument interfaces and external report ingestion are not connected.
  Outsourced results are entered and verified through the tracked workflow.

## Clinical and operational controls

- Patient identity recovery is performed by reception after explicit identity
  verification. Public duplicate detection uses matching mobile/name; it is not
  a national identity registry or an OTP identity-verification service.
- Medical content, reference ranges, diagnostic terminology, medication schedules,
  professional credentialing and local clinical policy need hospital validation.
  The application records orders/outcomes; it does not provide clinical decisions.
- Reports use immutable issued snapshots and a verification QR. The verification
  endpoint confirms issued-record metadata, not a cryptographic digital signature
  on an externally modified PDF. Bundled fonts primarily target the English UI.
- Bed stay charges use recorded rate snapshots and elapsed-minute proration,
  minimum one minute per segment. Configure prices and review that billing policy
  against hospital rules. Demonstration beds start at zero rate.
- Some list/selector interfaces use bounded recent-record options, while inherited
  read-only lists are not yet fully paginated. Large-volume production use requires
  pagination/search and load testing for the hospital's actual record volumes.
- Helpdesk supports routing, due times, escalation status and conversation. It does
  not include binary attachments or an external service-management integration.

## Deployment notes

- Automated checks cover the main synthetic workflows (JUnit, API smoke tests,
  Playwright browser tests). They do not establish exhaustive edge-case coverage,
  performance, regulatory compliance or clinical acceptance.
- Production TLS, domain names, database backups/restores, monitoring, privacy
  policy, hospital branding and production secrets are deployment responsibilities.
- Sign-in throttling is in-process and resets with the application. Cross-instance
  distributed rate limiting, MFA and centralized identity-provider integration are
  not configured. Tokens expire after one hour; revocation is checked per request.

No legacy database/data migration is included. Applied Flyway files are
forward-only schema history for the enterprise database.
