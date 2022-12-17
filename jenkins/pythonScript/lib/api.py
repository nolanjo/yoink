# imports
import requests
from lib.utility import retry


@retry(TimeoutError)
def send_to_open_search(json):
    print(json)
    resp = requests.post(url="https://logs.workvivo.qa/automation-index-1/_doc", json=json, verify=False)
    print("Response Code: {}".format(resp.status_code))
    print("Response Body: {}".format(resp.text))
    print("Response Expected: {}".format(resp.status_code == 201))
