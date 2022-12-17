# imports
import os
from argparse import ArgumentParser

CONSTANT_DIR = "../../../tests"


# converts the command line args to a dict
def process_command_line_gather():
    parser = ArgumentParser()
    parser.add_argument("-c", "--critical", dest="critical", default=False,
                        help="Are there critical path testing logs")
    parser.add_argument("-d", "--deepDive", dest="deepDive", default=False,
                        help="Are there deep dive path testing logs")
    parser.add_argument("-r", "--regression", dest="regression", default=False,
                        help="Are there regression path testing logs")
    parser.add_argument("-b", "--backEnd", dest="backEnd", default=False,
                        help="Are there back end testing logs")
    parser.add_argument("-o", "--outPutDir", dest="output_dir", default=CONSTANT_DIR,
                        help="The directory to use when writing to file")

    return vars(parser.parse_args())


# converts the command line args to a dict
def process_command_line_send():
    parser = ArgumentParser()
    parser.add_argument("-j", "--json", dest="json", default={},
                        help="JSON file to send")

    return vars(parser.parse_args())["json"]
