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
        with revision('stable_1_x'):
            call(['mvn', 'clean'])
            call(['pip', 'install', '-r', 'requirements.txt'])
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
def revision(ref):
    changes = call(['git', 'diff', '--exit-code']) > 0 or call(['git', 'diff', '--cached', '--exit-code']) > 0
    stashed = False
    if(changes):
        call(['git', 'stash'])
        stashed = True
    call(['git', 'checkout', ref])
    try:
        yield
    finally:
        call(['git', 'checkout', '@{-1}'])
        if stashed:
            call(['git', 'stash', 'pop'])

@contextmanager
def cd(path):
    cwd = os.getcwd()
    os.chdir(path)
    try:
        yield
    finally:
        os.chdir(cwd)

def write_idea_config(url, mpac_password):
    path = os.path.join('.idea','workspace.xml')
    if os.path.split(os.getcwd())[-1] == 'bin':
        path = os.path.join('..', path)

    if not os.path.exists(path):
        print('Could not find IDEA configuration file. Make sure you\'re in the connect root and have created an IDEA project')
        exit(4)

    config_fragment = config_fragment_template.format(vm_args=config_vm_arguments(url, mpac_password))

    config_file_tree = ET.parse(path)
    test_run_config_tree = config_file_tree.getroot().find("component[@name='RunManager']")
    existing_at_config = test_run_config_tree.find("configuration[@name='Acceptance Tests']")
    if existing_at_config is not None:
        test_run_config_tree.remove(existing_at_config) 
    test_run_config_tree.insert(0,ET.XML(config_fragment))
    config_file_tree.write(path, encoding='UTF-8', xml_declaration=True)

config_fragment_template = """
    <configuration default="false" name="Acceptance Tests" type="JUnit" factoryName="JUnit" singleton="true">
      <module name="" />
      <option name="ALTERNATIVE_JRE_PATH_ENABLED" value="false" />
      <option name="ALTERNATIVE_JRE_PATH" value="" />
      <option name="PACKAGE_NAME" value=""/>
      <option name="MAIN_CLASS_NAME" value="" />
      <option name="METHOD_NAME" value="" />
      <option name="TEST_OBJECT" value="pattern" />
      <option name="VM_PARAMETERS" value="{vm_args}" />
      <option name="PARAMETERS" value="" />
      <option name="WORKING_DIRECTORY" value="file://$PROJECT_DIR$" />
      <option name="ENV_VARIABLES" />
      <option name="PASS_PARENT_ENVS" value="true" />
      <option name="TEST_SEARCH_SCOPE">
        <value defaultName="wholeProject" />
      </option>
      <envs />
      <patterns>
        <pattern testClass="at.*" />
      </patterns>
      <method />
    </configuration>
"""

def config_vm_arguments(url, password):
    return ' '.join(s.format(instance=url[8:], pw=password) for s in [
        '-Dmpac.username=atlassian-connect-bot@atlassian.com',
        '-Dmpac.password={pw}',
        '-Dserver={instance}',
        '-Dhttp.jira.url=https://{instance}',
        '-Dhttp.jira.hostname={instance}',
        '-Dbaseurl.jira=https://{instance}',
        '-Djira.host={instance}',
        '-Dhttp.confluence.hostname={instance}',
        '-Dhttp.confluence.url=https://{instance}/wiki',
        '-Dbaseurl.confluence=https://{instance}/wiki',
        '-Dindra.baseurl=https://{instance}/wiki',
        '-Dhostname={instance}',
        '-Dbaseurl=https://{instance}/wiki',
        '-Djira.baseurl=https://{instance}/wiki',
        '-Dbaseurl.bamboo=https://{instance}/builds',
        '-Dhttp.bamboo.url=https://{instance}/builds',
        '-Dhttp.bamboo.hostname={instance}',
        '-Dacceptance.test.bamboo.host=https://{instance}/builds',
        '-Dondemand.acceptance.tests',
        '-Dmaven.test.unit.skip',
        '-Dinstall.plugin=false',
        '-Dgroups=com.atlassian.jira.categories.OnDemandWideSuiteTest,com.atlassian.test.categories.OnDemandAcceptanceTest',
        '-Dtest.ondemand',
        '-Djira.xml.data.location=.',
        '-Dconfluence.stateless.skip.plugins=true',
        '-Dno.webapp',
        '-Dcontext.path=',
        '-Duse.https',
        '-Dhttp.port=443',
        '-Dhttp.jira.port=443',
        '-Dcontext.jira.path=',
        '-Dhttp.jira.protocol=https',
        '-Djira.port=443',
        '-Djira.context=',
        '-Djira.protocol=https',
        '-Dcontext.confluence.path=/wiki',
        '-Dhttp.confluence.protocol=https',
        '-Dhttp.confluence.port=443',
        '-DwebappContext=/wiki',
        '-DonDemandMode=True',
        '-DldapMode=EXTERNAL_CROWD',
        '-Dhttp.bamboo.protocol=https',
        '-Dhttp.bamboo.port=443',
        '-Dcontext.bamboo.path=/builds',
        '-Dacceptance.test.webapp.context=/builds',
        '-Dacceptance.test.webapp.port=443',
        '-Dbamboo.acceptance.ondemand.mode',
    ])

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
    write_idea_config(args.freezer_instance_url, args.mpac_password)
    print('\nAn "Acceptance Tests" run configuration has been added to your IDEA settings. (We deleted any we found by the same name)')
    print('\nTo run it, go to [Run] > [Run...] > [Acceptance Tests]\n')

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
    run_via_runner.add_argument('-c', '--host-credentials', default='admin:admin',\
        help='Credentials of the host we\'re installing connect onto, in the form username:password')
    run_via_runner.set_defaults(func=run)

    config = subparsers.add_parser('configure', help='Add acceptance test run configuration to IDEA')
    config.set_defaults(func=configure)

    parser.add_argument('-p', '--mpac-password', required=True,\
        help='The ac-connect-bot@atlassian.com password, required to install the test add-on if it\'s missing')

    parser.add_argument('freezer_instance_url',\
        help="""The url of the freezer instance to run tests against.
        For best results, provision yoursef one here: https://jira-bamboo.internal.atlassian.com/browse/ATR-CREATE""")

    return parser

if __name__ == "__main__":
    args = options().parse_args()
    args.func(args)
