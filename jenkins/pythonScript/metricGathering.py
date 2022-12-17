# imports
from lib.file_parser import iterate_files, read_xml_junit
from lib.file_dump import dump_file, delete_file
from lib.setting import process_command_line_gather, CONSTANT_DIR


# runs the
def main(args):
    output = []
    if args['critical']:
        print("Gathering Critical Tests")
        output.extend(iterate_files(CONSTANT_DIR + "/cypress/reports/critical"))
    if args['deepDive']:
        print("Gathering Deeper Dive Tests")
        output.extend(iterate_files(CONSTANT_DIR + "/cypress/reports/integration"))
    if args['regression']:
        print("Gathering Regression Tests")
        output.extend(iterate_files(CONSTANT_DIR + "/cypress/reports/regression"))
    if args['backEnd']:
        print("Gathering Server Tests")
        output.extend(read_xml_junit(CONSTANT_DIR + "/postman/newman"))
    delete_file(args["output_dir"])
    dump_file(output, args["output_dir"])


if __name__ == '__main__':
    main(process_command_line_gather())
