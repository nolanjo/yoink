# imports
import json
import os

CONSTANT_OUTPUT = "/parsed.json"


def delete_file(result_path):
    print(result_path)
    full_file = result_path + CONSTANT_OUTPUT
    if os.path.isfile(full_file):
        print("Before Writing Deleting File: {}".format(full_file))
        os.remove(full_file)
    else:
        print("Does not exist: {}".format(full_file))


def dump_file(json_dict, result_path):
    print("Writing to : {}{}".format(result_path, CONSTANT_OUTPUT))
    with open(result_path + CONSTANT_OUTPUT, "w+") as output_file:
        json.dump(json_dict, output_file, indent=4)
