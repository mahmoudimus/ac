# Security

Connect security enables us to protect customer data from unauthorised access and from malicious or accidental changes,
thus allowing administrators to install and evaluate add-ons, and users to leverage installed add-ons in a secure manner.

Connect's security goals are to
* identify add-ons,
* work out what each add-on can do,
* accept allowed actions with minimal friction,
* clearly reject disallowed actions,
* help administrators installing add-ons to understand what the add-ons can do, and
* allow administrators to arbitrarily limit the actions that each add-on can take.

There are two main branches of security, [authentication](./authentication.html) and authorisation, with authorisation
further divided into [static](../scopes/scopes.html) and run-time authorisation.

## Authentication

Authentication tells the product the identity of the add-on and is the basis of all other security. On each incoming API
request the identity of the add-on is established, with requests that do not contain authentication information being
treated as anonymous.

Read more details in our [authentication documentation](./authentication.html).

## Authorisation

Now that the add-on sending each incoming request has been identified we need to work out if the add-on is allowed to
make this request, allowing authorised actions and rejecting unauthorised actions. We want to help
administrators installing add-ons to understand what each add-on can do and also allow them to arbitrarily limit the
potential actions of individual add-ons.

### Static Authorisation: Scopes

An add-on must declare the maximum set of actions that it may perform: read, write, etc. This security level is enforced
by Connect and cannot be bypassed by add-on implementations. Therefore administrators can be assured that the add-ons with
"read" permission can only read data and not modify it, and that add-ons with "write" permission cannot delete data, etc.

Add-on vendors cannot possibly know about the myriad product instances into which the add-on may be installed, so they
need a way of statically specifying the maximum set of actions that their add-on may perform. This is expressed in the
scopes in the add-on descriptor and is presented to the administrator during installation.

Read more details in our [scopes documentation](../scopes/scopes.html).

### Run-time Authorisation: Add-on Users

Some administrators have some content that they wish to protect from add-ons. For example, some companies store sensitive
details such as payroll and future business plans in product instances, and wish to be assured that it is accessible to
few add-ons or no add-ons at all. Without this assurance, such a company would likely install no add-ons.

This protected data varies across instances, as does its protections. Atlassian and add-on vendors have no way of knowing
in advance what individual administrators will decide must be protected from add-ons nor what the protections should be
(for example, perhaps one add-on is allowed read access to one particular super-secret-project while another is allowed
write access).

We enable administrators to arbitrarily limit add-on access to user data at run-time by assigning every add-on its own
user. Administrators can then permission these add-on users in very similar ways to how they permission normal users.
Every incoming server-to-server request from a Connect add-on is assigned the add-on's
user and authorisation proceeds as normal from that point onwards, with the add-on user's permissions limiting what API
features the incoming requests may target. Add-ons are not told which user they are assigned and do not need to specify
it: it is automatically mapped from the identity of the add-on itself.

Currently, in-browser requests are assigned the user at the browser.

### Combining Static and Run-time Authorisation

The set of actions that an add-on is capable of performing is the intersection of the static scopes and the permissions
of the user assigned to the request. It is entirely possible that any request may be rejected because the assigned user
lacks the necessary permission, so the add-on should always defensively detect
[HTTP 403 forbidden](http://en.wikipedia.org/wiki/HTTP_403) responses from the product and, if possible, display an
appropriate message to the user.


### Road Map

Currently, the set of actions that an add-on is capable of performing is limited to the intersection of its scopes and the
add-on user's permissions (for server-to-server) or to the intersection of its scopes and the in-browser user's
permissions (for in-browser requests).

We will ultimately move to a model that additionally respects the add-on user's permissions on in-browser requests and
helps add-ons to be good citizens in server-to-server requests by allowing add-ons to specify the user on whose behalf
they are acting (if acting on behalf of a specific user, which will not always be the case in server-to-server requests).

The set of allowed actions will then be limited to the intersection of the add-on's scopes, the add-on user's permissions
and (if there is a context user) the context user's permissions.