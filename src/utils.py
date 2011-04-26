import datetime
import os

def get_path(origin_path, relative_path):
    return os.path.join(os.path.dirname(origin_path), relative_path)

def path_getter(origin_path):
    def getter(relative_path):
        return get_path(origin_path, relative_path)
    return getter

def parse_date(date_string):
    return datetime.datetime.strptime(date_string, '%Y-%m-%d').date()
