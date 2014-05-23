#!/usr/bin/env python

import requests
import getpass
from optparse import OptionParser

def main():
    default_zones = ['ash1', 'sc1', 'syd1']

    parser = OptionParser()
    parser.add_option('-n', '--lines', type="int", default=20)
    parser.add_option('-a', '--app', type="string", default="jira")
    parser.add_option('-u', '--user', type="string")
    parser.add_option('-z', '--zone', type="string")
    (options, args) = parser.parse_args()

    if options.user is None:
        print "You must specify a user (-u)"
        return

    if options.zone:
        zones = [options.zone]
    else:
        print "Please provide at least one zone from this list %s" % default_zones
        return

    passwd = getpass.getpass("Password for %s: " % options.user)

    for host in args:
        for zone in zones:
            url = "https://pycmrest.%s.uc-inf.net/pycm/v1.0/logs/tail/%s/%s?lines=%d" % (zone, host, options.app, options.lines)
            r = requests.get(url, auth=(options.user, passwd), verify=False)
            if r.status_code == 401:
                print "Authentication problem - please check username or password provided"
                return
            elif r.status_code == 200:
                print "------- %s ------\n%s\n ----------------\n" % (host, r.json()['result'])
            else:
                print "------- %s (%s) ------\n%s\n ----------------\n" % (host, r.status_code, r.content)


if __name__ == "__main__":
    main()
