#!/usr/bin/env python

import requests
import os
import xml.etree.ElementTree as ET
import operator
import re
from collections import namedtuple

WARN_PATH = "Please run this script from the atlassian connect source root"
maven_pom_prepend = "{http://maven.apache.org/POM/4.0.0}"

manifest_base_url = "https://manifesto.atlassian.io/api/summary/"

product_names = ["jira", "confluence"]

VersionTuple = namedtuple("Version", ["major", "minor", "patch", "additional"])

def product_dictionary(product_name):
    dict = {"name":product_name, "old_version":None, "new_version":None}
    return dict

def old_version_for_product(product_dict, properties_xml):
    product_version_xml = properties_xml.find(maven_pom_prepend + "atlassian." + product_dict["name"] + ".version")
    product_dict["old_version"] = product_version_xml.text

def new_version_for_product(product_name, env_json):
    product_list = filter(lambda x: x["name"] == product_name, env_json["products"])
    if len(product_list) != 1:
        print "Expected 1 "+product_name+" to be found, got" + len(product_list)
        exit(2)
    return product_list[0]["version"]

def get_versions_from_all_envs(product_name, envs_json):
    version_list = []
    for env in envs_json:
        version_str = new_version_for_product(product_name, env)
        version = create_version_tuple(version_str)
        version_list.append(version)
    return version_list

def get_product_version_from_manifest(product_dict, envs_json):
    version_list = get_versions_from_all_envs(product_dict["name"], envs_json)
    version_tuple = get_latest_version(version_list)
    product_dict["new_version"] = version_tuple_to_str(version_tuple)

def get_latest_version(version_list):
    latest = version_list.pop()
    for version in version_list:
        if not is_first_version_greater(latest, version):
            latest = version
    return latest

def is_first_version_greater(version_a, version_b):
    if version_a.major != version_b.major:
        return version_a.major > version_b.major
    if version_a.minor != version_b.minor:
        return version_a.minor > version_b.minor
    if version_a.patch != version_b.patch:
        return version_a.patch > version_b.patch
    return version_a.additional > version_b.additional

def version_tuple_to_str(version):
    return str(version.major) + "." + str(version.minor) + "." + str(version.patch) + "-" + version.additional

def create_version_tuple(version_str):
    match = re.match(r'(\d+)\.(\d+)\.(\d+)(\-(.*))?', version_str)
    return VersionTuple(major=int(match.group(1)), minor=int(match.group(2)), patch=int(match.group(3)), additional=match.group(5))

def set_new_versions(product_dict):
    new_file_name = "pom.xml.new"
    inner_tag = "atlassian." + product_dict["name"] + ".version"
    start_tag = "<" + inner_tag + ">"
    end_tag = "<\/" + inner_tag + ">"

    sed_system_call = "sed 's/"+ start_tag + product_dict["old_version"] + end_tag +"/"+ start_tag + product_dict["new_version"] + end_tag +"/' pom.xml > " + new_file_name
    os.system(sed_system_call)
    os.system("mv " + new_file_name + " pom.xml")
    print product_dict["name"] + ": " + product_dict["old_version"] + " -> " + product_dict["new_version"]

def main():
    if not os.path.exists('pom.xml'):
        print WARN_PATH
        exit(2)

    tree = ET.parse('pom.xml')
    root = tree.getroot()

    if root.findtext(maven_pom_prepend+"artifactId") != "atlassian-connect-parent":
        print WARN_PATH
        exit(2)

    products = map(product_dictionary, product_names)

    xml_properties = root.find(maven_pom_prepend + "properties")

    response = requests.get(manifest_base_url)
    json_body = response.json()
    envs_json = json_body['environments']

    map(lambda x: old_version_for_product(x, xml_properties), products)
    map(lambda x: get_product_version_from_manifest(x, envs_json), products)
    map(lambda x: set_new_versions(x), products)

if __name__ == "__main__":
    main()