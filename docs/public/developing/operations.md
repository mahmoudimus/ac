# Add-ons operations guide

When you release an add-on publicly on the [Atlassian Marketplace](https://marketplace.atlassian.com), 
administrators of an Atlassian OnDemand application can install it in their instance, meaning they rely on your service to 
deliver content to their users. Therefore, ensuring consistent operation of your add-on is critical. 
You can avoid many potential service disruptions by planning carefully.
There are some important considerations to take into account:

- Our customers are all around the world, covering all timezones.
- While some instances have a handful of users, others have thousands of users depending on our products to run their business.
- We have designed our cloud-based products to be both secure and reliable, boasting 99.9% uptime and 24x7x365 support.

Below, we discuss strategies for running your Atlassian Connect add-on as scalable, reliable software as a service.
Some of these aspects need to be addressed very early in the design, as implementing them after the fact can be really difficult.

## 1. Defining your Service Level Agreement (SLA)

You should define your service level targets, which you can validate during [performance testing](#perftesting), use as a basis to 
[monitor your add-ons at runtime](#monitoring), and guarantee by [scaling your deployment](#scalability).
The following table lists some examples of indicators you could track:

<table class="aui">
	<thead>
		<tr>
			<td>
				**Category**
			</td>
			<td>
				**Indicator**
			</td>
			<td>
				**Description**
			</td>
		</tr>
	</thead>
	<tr>
		<td rowspan="3">
			[Performance](#performance)
		</td>
		<td>
			Uptime
		</td>
		<td>
			Time during which the add-on is operational, outside of your documented [maintenance windows](#maintenance) (e.g. 99%)
		</td>
	</tr>
	<tr>
		<td>
			User interface response times
		</td>
		<td>
			e.g. average response time, mean response time, 90th or 95th percentile response time
		</td>
		<td>
			
		</td>
	</tr>
	<tr>
		<td>
			Service calls (e.g. REST) response times
		</td>
		<td>
			e.g. average response time, mean response time, 90th or 95th percentile response time
		</td>
	</tr>
	<tr>
		
	<tr>
		<td rowspan="2">
			[Business Continuity](#bcp)
		</td>
		<td>
			RTO (Recovery Time Objective)
		</td>
		<td>
			Duration of time within which the service must be restored after a major incident (e.g. 8h)
		</td>
	</tr>
	<tr>
		<td>
			RPO (Recovery Point Objective)
		</td>
		<td>
			Maximum tolerable period in which data might be lost due to a major incident (e.g. 24h)
		</td>
	</tr>
	<tr>
		<td rowspan="3">
			[Support](#support)
		</td>
		<td>
			Availability
		</td>
		<td>
			Hours of operation for the support team (e.g. 24x7x365, or 8 hours a day / 5 days a week in your timezone)
		</td>
	</tr>
	<tr>
		<td>
			Initial response time
		</td>
		<td>
			Time elapsed between the customer's first request and the initial support response. For example: <br/>
				- Level 1: 1 hour<br/>
				- Level 2: 4 hours<br/>
				- Level 3: 8 hours<br/>
				- Level 4: 24 hours<br/>
		</td>
	</tr>
	<tr>
		<td>
			Resolution time
		</td>
		<td>
			Time elapsed between the customer's first request and the issue being resolved. 
		</td>
	</tr>
</table>

<div class="aui-message success">
	    <p class="title">
	        <span class="aui-icon icon-success"></span>
	        <strong>Publish your SLA!</strong>
	    </p>
		<p>You should publish a Service Level Agreement outlining your support and service level terms online.</p>
</div>

<a name="performance"></a>
##2. Managing the Performance of your Add-ons
<a name="scalability"></a>
### Scalability

There are two ways to design your add-ons to scale with a growing number of installations and users:

- Vertical scaling: you scale by adding more resources (e.g. CPU, memory) to existing nodes
- Horizontal scaling: you scale by adding more nodes (e.g. servers) 

It may be difficult to predict exactly the resources your add-ons will need. For this reason, and because 
your add-ons will operate in a cloud environment targeting thousands of customers, we encourage you to design 
your add-ons to scale horizontally. 

Existing cloud providers can help you scale your implementations. One example of such providers is [Heroku](http://www.heroku.com), 
a cloud application platform that can host applications developed in Java, Node.js, Pyton, Ruby, Scala or Clojure. 
Heroku leverages [Amazon AWS](http://www.aws.amazon.com) (Amazon Web Services) technology, and mostly supports horizontal 
scaling. Other examples or world-class platforms include the [Google Cloud Platform](http://cloud.google.com) and 
[Salesforce One](http://www.salesforce.com/salesforce1/).

<a name="perftesting"></a>
### Performance testing
We recommend you run performance tests for your add-ons. This will help you define the resources your add-ons require 
when you first deploy them, and understand how new versions of your add-ons impact resource utilization. 
The following classes of tests are particularly useful:
<table class="aui">
	<thead>
		<tr>
			<td width="100">**Test Type**</td>
			<td>**Objectives**</td>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>
				Load testing
			</td>
			<td>
				Test the add-on under the load that is expected when the add-on is live, to validate that it is behaving 
				as expected.
			</td>
		</tr>
		<tr>
			<td>
				Stress testing
			</td>
			<td>
				Identify the limits of the add-on, and understand how the add-on behaves when the load is much higher 
				than the expected load.
			</td>
		</tr>
		<tr>
			<td>
				Soak testing
			</td>
			<td>
				Identify potential memory leaks, degrading performance because of poor database indexing, etc. 
				A soak test is the equivalent of a load test that runs over a long period of time. 
			</td>
		</tr>
		<tr>
			<td>
				Spike testing
			</td>
			<td>
				Understand how the add-on will react to a sudden burst of requests. 
			</td>
		</tr>
	</tbody>
</table>

You should run performance tests for your add-ons:

- In isolation, using mock implementations of Atlassian products REST APIs. This helps identify any issues (memory leaks, etc.) 
limited to your implementation.
- Using a real-life deployment environment for end-to-end performance tests. For this we recommend you use a performance testing 
environment that is as close as possible to the production environment. You should set up instances of Atlassian OnDemand products 
for this purpose. 

There are a number of tools to help you design and run performance tests for your add-ons. Examples of 
Load Testing Frameworks include [The Grinder](http://grinder.sourceforge.net) and [Locust](http://locust.io). They help you run 
distributed tests using many load injector machines.
<a name="monitoring"></a>
### Monitoring your SLA

You should have tools to monitor your add-on performance at runtime, and procedures in place to scale resources once specific 
thresholds are met. At a minimum, you should monitor utilization of resources by your add-ons (CPU, memory, disk space, etc.). 
When using a cloud provider, you can look at strategies to automatically scale the resources allocated to your add-ons based on load.

<a name="maintenance"></a>
## 3. Maintaining your Add-ons

### Versioning and Upgrading

We automatically detect updates to add-ons with a polling service. This way, you can easily release fixes and 
new features without having to manually create new version entries in the Marketplace. For more information on how to upgrade 
your add-ons and manage versions, you should read the [Upgrading your Add-on](upgrades.html) section. 

<div class="aui-message warning">
	    <p class="title">
	        <span class="aui-icon icon-warning"></span>
	        <strong>Test, test, and in doubt... Test some more!</strong>
	    </p>
		<p>Make sure you not only test new features, but also run regression tests to ensure existing functionality 
			is not broken when releasing new versions.</p>
</div>

### Maintenance Windows
Since your add-on and Atlassian products are decoupled, you can decide when to upgrade your add-ons independently from the 
[Atlassian OnDemand Maintenance Windows](https://confluence.atlassian.com/display/AOD/Atlassian+OnDemand+maintenance+windows).
Ideally, your solution should be architected in a way that ensures maintenance is transparent to end-users. If this is not possible, 
make sure you publish your maintenance windows online, and provide a meaningful error message to users trying to access your add-ons 
at this time.

<a name="bcp"></a>
## 4. Addressing Business Continuity Planning
You should address the following aspects when looking at potential major outages:

- Data backups: you should have a data backup strategy that ensures your RPO (Recovery Point Objective) is met. For example, 
for a RPO of 24h, you should do a full backup of all add-on data overnight, keeping the backups on a different site to the one 
that is running the add-on. 
- Recovery procedures: you should have procedures in place to restore your add-ons in the case of a major outage, 
and we suggest you do a few dry runs. Ideally, you should be testing your disaster recovery procedures regularly. 
Hope for the best, plan for the worst! 

Note that using an world-class cloud provider minimizes the risk of a major outage impacting the users of your add-ons. 
For example when using Heroku with Heroku Postgres, the platform automatically backs up deployed applications and data, and 
automatically brings the application back online in case of a data center outage, with minimum data loss.
<a name="support"></a>
## 5. Providing Support

First, check out the [Atlassian Support Offerings](https://confluence.atlassian.com/display/Support/Atlassian+Support+Offerings). 
We are well known for our great support! Here is what we recommend you focus on: 
<table class="aui">
	<thead>
		<tr>
			<td>**Recommendation**</td>
			<td>**Details**</td>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>Provide a support URL for all paid-via-Atlassian add-ons</td>
			<td>Your support URL clearly outlines the avenues a customer can take to get technical support.</td>
		</tr>
		<tr>
			<td>Offer support at least 8 hours a day, 5 days a week in your local time zone for all paid-via-Atlassian add-ons</td>
			<td>Support hours can be any time, relative to your local timezone.</td>
		</tr>
		<tr>
			<td>Use an issue tracker like JIRA to resolve and track customer-reported bugs and feature requests, 
				for all paid-via-Atlassian add-ons.</td>
			<td>You don't need to use an Atlassian product to track your issues, but use some kind of tracker to keep on top 
				of customer-reported bugs and improvement requests.</td>
		</tr>
		<tr>
			<td>Provide Atlassian with 24/7 emergency contact information</td>
			<td>Provide an email address or phone number to Atlassian just in case we need to contact you for emergency support issues, 
				such as those involving customer data loss or downtime. If something goes wrong, we should be able to reach you via this 
				contact information 24/7. </td>
		</tr>
	</tbody>
</table>




