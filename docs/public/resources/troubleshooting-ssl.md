# Troubleshooting SSL issues

In production, the communication between the host application and add-ons uses SSL in order to ensure secure transfer of
customer data. This is enforced by the add-on base URL needing to have the `https` scheme.

It is common for add-on
developers to experience problems with these SSL connections. We recommend the use of
[Qualys SSL Labs - SSL Server Test](https://www.ssllabs.com/ssltest) for debugging.

## Self-signed certificates

The host application will not trust self-signed SSL certificates. The SSL certificate of your add-on server must be
issued by a trusted authority.

## Server Name Indication

The use of Server Name Indication was a problem in the past with Atlassian Connect in JIRA and Confluence, but should
now be working properly.