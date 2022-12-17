# imports
import json
from pathlib import Path
from junitparser import JUnitXml


def iterate_files(directory_in_str):
    count = 0
    output = []
    path_list = Path(directory_in_str).glob('*.json')
    print("Searching In {}".format(directory_in_str))
    for path in path_list:
        count += 1
        path_in_str = str(path)
        print("Found: {} in path".format(path_in_str))
        with open(path_in_str, "r") as test_results:
            out = json.load(test_results)
            # calculate the name and use it, calculate the type and put it in...
            split = out["file"].split("/")
            out["area"] = split[2]
            out["type"] = split[1]
            out["name"] = split[3].split(".", 1)[0]
            del out["file"]
            del out["start"]
            output.append(out)

    if not count:
        print("No Files Found")

    return output


def read_xml_junit(directory_in_str):
    count = 0
    output = []
    xml = []
    print("Searching In {}".format(directory_in_str))
    path_list = Path(directory_in_str).glob('*.xml')
    for path in path_list:
        print("Found: {} in path".format(path))
        count += 1
        xml = JUnitXml.fromfile(path)
    for suite in xml:
        # handle suites
        split = suite.name.split("/")
        result = {
            "name": suite.name.replace(" ", "").replace("/", "-"),
            "duration": suite.time,
            "tests": suite.tests,
            "failures": suite.failures,
            "errors": suite.errors,
            "passes": suite.tests - suite.failures,
            "type": "BE Critical",
            "area": split[1].strip() if len(split) > 1 else "na"
        }
        output.append(result)

    if not count:
        print("No Files Found")

    return output
