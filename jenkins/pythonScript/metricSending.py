# imports
import json
from lib.api import send_to_open_search
from lib.setting import process_command_line_send


# send the passed json data
def main(args):
    send_to_open_search(json.loads(args))


if __name__ == '__main__':
    main(process_command_line_send())
