#!/bin/bash

# grab the post-commit hooks
curl -f http://linode.twdata.org/commit-msg -o .git/hooks/commit-msg
chmod a+x .git/hooks/commit-msg

# add remotes beac and branch
git remote add beac https://bamboo.extranet.atlassian.com/plugins/servlet/p/remotable-plugins

git fetch

echo "You're all set!"
echo ""
echo " * branch master tracks remote origin git@bitbucket.org:atlassian/remotable-plugins.git"
echo " * remote beac (https://bamboo.extranet.atlassian.com/plugins/servlet/p/remotable-plugins) has been added"
echo ""
echo "To push, run:"
echo "  git push beac HEAD:refs/for/master"
echo ""
