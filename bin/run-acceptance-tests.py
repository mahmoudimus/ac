#!/usr/bin/env python
import os, sys, argparse
import xml.etree.ElementTree as ET
from contextlib import contextmanager
from subprocess import call

WRONG_CWD = 'Please run this script from the atlassian connect source root'
RUNNER_URL = 'git@bitbucket.org:atlassian/acceptance-tests-runner.git'
RUNNER_TMP_PATH = '/tmp/acceptance-tests-runner'

def get_version():
    if os.path.split(os.getcwd())[-1] == 'bin':
        os.chdir('..')

    if not os.path.exists('pom.xml'):
        print(WRONG_CWD)
        exit(2)

    pom = ET.parse('pom.xml').getroot()
    if not pom.find('{http://maven.apache.org/POM/4.0.0}artifactId').text == 'atlassian-connect-parent':
        print(WRONG_CWD)
        exit(2)

    return pom.find('{http://maven.apache.org/POM/4.0.0}version').text

def build(includeNpm):
    maven_args = ['mvn', 'install', '-DskipTests', '-Pfreezer-release-profile']
    if not includeNpm:
        maven_args.append('-DskipNpm')
    return call(maven_args)

def clone(path):
    if not os.path.exists(path):
        call(['git', 'clone', RUNNER_URL, path])

def run_ats(path, version, url, mpac_password):
    with cd(path):
        with co('0a8964d'):
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
                    '-v', version,
                    '--remote-url', url
                ])
            print('\n\n')
            call(['tail', '-14', os.path.join('logs', 'master.log')])
            print('\n\n')
            print('LOG AVAILABLE AT: {}'.format(os.path.join(path, 'logs', 'master.log')))

@contextmanager
def co(revision):
    call(['git', 'checkout', revision])
    try:
        yield
    finally:
        call(['git', 'checkout', '@{-1}'])

@contextmanager
def cd(path):
    cwd = os.getcwd()
    os.chdir(path)
    try:
        yield
    finally:
        os.chdir(cwd)

def options():
    parser = argparse.ArgumentParser(description="Build the freezer release profile then run" + \
        "the acceptance tests against the nominated instance")

    subparsers = parser.add_subparsers()

    run_via_runner = subparsers.add_parser('run', help='Run via the AT runner script')
    run_via_runner.add_argument('-s', '--skip-build', action='store_true', default=False,\
        help='Skip building the freezer profile; use the build already in your local maven repository')
    run_via_runner.add_argument('-n', '--npm', action='store_true', default=False,\
        help='Also build the npm stuff (only required if you haven\'t built it before in the working copy you are using)')
    run_via_runner.add_argument('-a', '--at-runner-path', default=RUNNER_TMP_PATH,\
    help="""The path to a local checkout of the
        acceptance test runner (git@bitbucket.org:atlassian/acceptance-tests-runner.git)')
        If not specified, the the acceptance test runner will be checked out in the /tmp/ directory""")
    run_via_runner.add_argument('-i', '--install-plugin', action='store_true',\
        help='Re-install the plugin onto the host instance')
    run_via_runner.add_argument('-c', '--host-credentials', default='admin:admin',\
        help='Credentials of the host we\'re installing connect onto, in the form username:password')
    run_via_runner.set_defaults(func=run)

    config = subparsers.add_parser('config', help='Add AT runner config to IDEA')
    config.set_defaults(func=configure)

    parser.add_argument('-p', '--mpac-password', required=True,\
        help='The ac-connect-bot@atlassian.com password, required to install the test add-on if it\'s missing')

    parser.add_argument('freezer_instance_url',\
        help="""The url of the freezer instance to run tests against.
        For best results, provision yoursef one here: https://jira-bamboo.internal.atlassian.com/browse/ATR-CREATE""")

    return parser

def run(args):
    version = get_version()

    if not args.skip_build:
        if build(args.npm) > 0:
            exit(3)

    path = args.at_runner_path
    if not os.path.exists(path):
        clone(path)

    run_ats(path, version, args.freezer_instance_url, args.mpac_password)

def configure(args):
    pass

if __name__ == "__main__":
    args = options().parse_args()
    args.func(args)
