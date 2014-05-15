-- This script reduces the manual workload on OnDemand support in response to https://jira.atlassian.com/browse/UNIFIED-82.
-- To be run on the 'crowd' db in OnDemand instances where we have detected unexpected group membership.

begin transaction;
select * from cwd_membership where lower_parent_name = 'atlassian-addons' and lower_child_name not like 'addon_%';
delete from cwd_membership where lower_parent_name = 'atlassian-addons' and lower_child_name not like 'addon_%';
commit;