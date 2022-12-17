# imports
import time
from functools import wraps


def retry(exception_check, tries=2, delay=1, backoff=2, logger=None):
    def deco_retry(f):
        @wraps(f)
        def f_retry(*args, **kwargs):
            num_tries, num_delay = tries, delay
            while num_tries > 1:
                try:
                    return f(*args, **kwargs)
                except exception_check:
                    msg = "{}, Retrying in {} seconds...".format(str(exception_check), num_delay)
                    if logger:
                        # logger.exception(msg) # would print stack trace
                        logger.warning(msg)
                    else:
                        print(msg)
                    time.sleep(num_delay)
                    num_tries -= 1
                    num_delay *= backoff
            return f(*args, **kwargs)

        return f_retry  # true decorator

    return deco_retry
