#!/usr/bin/env python
import os, sys
import xml.etree.ElementTree as ET
from contextlib import contextmanager
from subprocess import call

WRONG_CWD = 'Please run this script from the atlassian connect source root'
RUNNER_URL = 'git@bitbucket.org:atlassian/acceptance-tests-runner.git'

def parseArgs(args):
    parsed = {}

    parsed['url'] = args.pop()
    if parsed['url'][0:8].lower() != 'https://':
        return usage()

    if not '-p' in args:
        return usage()

    parsed['mpac-password'] = args.pop(args.index('-p') + 1)
    args.remove('-p')

    if '--skip-build' in args:
        parsed['skip-build'] = True
        args.remove('--skip-build')

    if '-s' in args:
        parsed['skip-build'] = True
        args.remove('-s')

    if '-n' in args:
        parsed['npm'] = True
        args.remove('-n')

    if '--npm' in args:
        parsed['npm'] = True
        args.remove('--npm')


    if '-a' in args:
        parsed['runner-path'] = args[args.index('-a') + 1]

    return parsed

def get_version():
    if os.path.split(os.getcwd())[-1] == 'bin':
        os.chdir('..')

    if not os.path.exists('pom.xml'):
        print(WRONG_CWD)
        return

    pom = ET.parse('pom.xml').getroot()
    if not pom.find('{http://maven.apache.org/POM/4.0.0}artifactId').text == 'atlassian-connect-parent':
        print(WRONG_CWD)
        return

    return pom.find('{http://maven.apache.org/POM/4.0.0}version').text

def build(includeNpm):
    args = ['mvn', 'install', '-DskipTests', '-Pfreezer-release-profile']
    if not includeNpm:
        args.append('-DskipNpm')
    call(args)

def clone(path):
    if not os.path.exists(path):
        call(['git', 'clone', RUNNER_URL, path])

def run_ats(path, url, mpac_password):
    clone(path)
    with cd(path):
        call(['git', 'checkout', '0a8964d'])
        call(['mvn', 'clean'])
        call([
                'env',
                'bamboo_mpac_staging_username=atlassian-connect-bot@atlassian.com',
                'bamboo_mpac_staging_password=' + mpac_password,
                './prepare-and-run-artifact-od-tests.py',
                '--force-java-version', '8',
                '--force-mvn-version', '3',
                '-g', 'com.atlassian.plugins',
                '-a', 'atlassian-connect-integration-tests',
                '-v', '1.1.55-SNAPSHOT',
                '--remote-url', url
            ])
        print('\n\n')
        call(['tail', '-14', os.path.join('logs', 'master.log')])
        print('\n\n')
        print('LOG AVAILABLE AT: {}'.format(os.path.join(path, 'logs', 'master.log')))

def usage():
     print("""
        Builds the freezer release profile then runs the acceptance tests against the nominated instance

        USAGE:

        {file} [-a /path/to/at/runner] [ -s | --skip-build ] [ -n | --npm ] -p <mpac-password> <https://freezer.instance.url>

        OPTIONS:

        -p              The mpac staging credentials for atlassian-connect-bot@atlassian.com, needed to install the test add-on

        -a              The path to a local checkout of the acceptance test runner (git@bitbucket.org:atlassian/acceptance-tests-runner.git)
                        this seems not to 'just work' after revision 0a8964d, so it is recommended you have that revision checked out.
                        If -a is not specified, the acceptance test runner will be checked out in the /tmp/ directory

        -s              Skip building the freezer profile; use the one already in your local maven repository
        --skip-build

        -n              Also build the npm stuff (only required if you haven't built it before in the working copy you are using)
        --npm

        <https://freezer.instance.url>  The url of the freezer instance to run tests against.
                                        For best results, provision yoursef one here: https://jira-bamboo.internal.atlassian.com/browse/ATR-CREATE

     """.format(file=__file__))

@contextmanager
def cd(path):
    cwd = os.getcwd()
    os.chdir(path)
    try:
        yield
    finally:
        os.chdir(cwd)

if __name__ == "__main__":
    args = parseArgs(sys.argv)

    if args is None:
        exit(1)

    version = get_version()
    if version is None:
        exit(1)

    if not 'skip-build' in args:
        build('npm' in args)

    runner_path = args.get('runner-path', '/tmp/acceptance-tests-runner')
    run_ats(runner_path, args['url'], args['mpac-password'])

