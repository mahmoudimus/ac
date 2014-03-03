# Add-on Operations Guide

Before you deploy an add-on as a Public Listing on the MarketPlace, enabling administrators to install it for their users, 
there are some important considerations to take into account:

- Our customers are all around the world, covering all timezones.
- While some instances have a handful of users, others have thousands of users depending on our products to run their business.
- We have designed our cloud-based products to be both secure and reliable, boasting 99.9% uptime and 24x7x365 support.

This section lists some of the aspects we suggest you look at when defining the operational aspects for running your add-ons.
Some of these aspects need to be addressed very early in the design, as implementing them after the fact can be really difficult.

Atlassian introduced the [Verified Program](https://developer.atlassian.com/display/MARKET/The+Atlassian+Verified+program) 
to reward vendors who provide exemplary customer experiences. If you intend to sell your add-ons, you should definitely check it out!

## Defining your Service Level Agreement (SLA)

To start with, you should define your targets, which you can validate during [performance testing](#perftesting), use as a basis to 
[monitor your add-ons at runtime](#monitoring), and guarantee by [scaling your deployment](#scalability).
The following table lists some examples of indicators you could track:

<table class="aui">
	<thead>
		<tr>
			<td width="100">
				**Category**
			</td>
			<td width="140">
				**Indicator**
			</td>
			<td width="250">
				**Description**
			</td>
			<td>
				**Example Target**
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
			Time during which the add-on is operational, outside of documented [maintenance windows](#maintenance)
		</td>
		<td>
			99.9%
		</td>
	</tr>
	<tr>
		<td>
			User Interface Response Times
		</td>
		<td>
			e.g. average response time, mean response time, 90th or 95th percentile response time
		</td>
		<td>
			
		</td>
	</tr>
	<tr>
		<td>
			Service calls (e.g. REST) Response Times
		</td>
		<td>
			e.g. average response time, mean response time, 90th or 95th percentile response time
		</td>
		<td>
			
		</td>
	</tr>
	<tr>
		
	<tr>
		<td rowspan="2">
			[Business Continuity](#bcp)
		</td>
		<td>
			RTO - Recovery Time Objective
		</td>
		<td>
			Duration of time within which the service must be restored after a major incident
		</td>
		<td>
			8h
		</td>
	</tr>
	<tr>
		<td>
			RPO - Recovery Point Objective
		</td>
		<td>
			Maximum tolerable period in which data might be lost due to a major incident
		</td>
		<td>
			24h
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
			Hours of operation for the support team
		</td>
		<td>
			24x7x365, or 8 hours a day, 5 days a week in your timezone
		</td>
	</tr>
	<tr>
		<td>
			Initial response time
		</td>
		<td>
			Time elapsed between the customer's first request and the initial support response. 
		</td>
		<td>
			- Level 1: 1 hour<br/>
			- Level 2: 4 hours<br/>
			- Level 3: 8 hours<br/>
			- Level 4: 24 hours<br/>
		</td>
	</tr>
	<tr>
		<td>
			Resolution Time
		</td>
		<td>
			Time elapsed between the customer's first request and the issue being resolved. 
		</td>
		<td>
		</td>
	</tr>
</table>

<div class="aui-message success">
	    <p class="title">
	        <span class="aui-icon icon-success"></span>
	        <strong>Publish your SLA!</strong>
	    </p>
		<p>You should publish a Service Level Agreement outlining your support and service level terms online. 
This is one step towards becoming Atlassian Verified.</p>
</div>

<a name="performance"></a>
##Managing Performance
<a name="perftesting"></a>
### Performance testing
We recommend you run performance tests for your add-ons. This will help you define the resources required 
by your add-ons when you first deploy them, and the impact of new versions on these resources. 
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

There are a number of tools to help you design and run performance tests for your add-ons. One example of 
Java Load Testing Framework is [The Grinder](http://grinder.sourceforge.net), that helps you run a distributed 
test using many load injector machines.
<a name="scalability"></a>
### Scalability

There are two ways to design your add-ons to scale with a growing number of installations and users:

- Vertical scaling: you scale by adding more resources (e.g. CPU, memory) to existing nodes
- Horizontal scaling: you scale by adding more nodes (e.g. servers) 

It may be difficult to predict exactly how much resources your add-ons will need. For this reason, and because 
your add-ons will be operating in a cloud environment targetting thousands of customers, we encourage you to design 
your add-ons to scale horizontally. This will of course depend on your hosting strategy.

Existing cloud providers can help you scale your implementations. One example of such providers is [Heroku](www.heroku.com), 
a cloud application platform that can host applications developed in Java, Node.js, Pyton, Ruby, Scala or Closure. 
Heroku leverages [Amazon AWS](http://www.aws.amazon.com) (Amazon Web Services) technology, and mostly supports horizontal 
scaling.
<a name="monitoring"></a>
### Monitoring your SLA

You should have tools to monitor your add-on performance at runtime, and procedures in place to scale resources once specific 
thresholds are met. To start with, you should at least monitor the utilization of the resources allocated to your add-ons 
(CPU, memory, disk space, etc.).
<a name="maintenance"></a>
## Maintenance Windows

For more information on how to upgrade your add-on, you should read the [Upgrading your Add-on](upgrades.html) section.
Although you can decide when to upgrade your add-ons, as much as possible you should aim to align with the 
[Atlassian OnDemand Maintenance Windows](https://confluence.atlassian.com/display/AOD/Atlassian+OnDemand+maintenance+windows) 
as it will minimize disruption for your customers. 

<div class="aui-message success">
	    <p class="title">
	        <span class="aui-icon icon-success"></span>
	        <strong>Become compatible early!</strong>
	    </p>
		<p>Average Atlassian product compatibility in 14 days or less across all your paid-via-Atlassian add-ons to become Atlassian 
Verified.</p></div>

<a name="bcp"></a>
## Business Continuity Planning
You should address the following aspects when looking at potential major outages:

- Data backups: you should have a data backup strategy that ensures your RPO (Recovery Point Objective) is met. For example, 
for a RPO of 24h, you should do a full backup of all add-on data overnight, keeping the backups on a different site to the one 
that is running the add-on. 
- Recovery procedures: you should have procedures in place to restore your add-ons in the case of a major outage, 
and we suggest you do a few dry runs. Ideally, you should be testing your disaster recovery procedures regularly. 
Hope for the best, plan for the worst! 

Note that using an enterprise-class cloud provider minimises the risk of a major outage impacting your add-ons.

<a name="support"></a>
## Support

First, check out the [Atlassian Support Offerings](https://confluence.atlassian.com/display/Support/Atlassian+Support+Offerings). 
We are well known for our great support! 
Then, have a look at the [Atlassian Verified Program](https://developer.atlassian.com/display/MARKET/The+Atlassian+Verified+program) 
which lists some great recommendations for you to provide support to your customers for add-ons.



