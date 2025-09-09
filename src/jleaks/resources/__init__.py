import importlib.resources as pkg_resources
import json
from typing import Any, Dict
import sys

from .. import project

__data = None

def load_data() -> Dict[str, project.Project]:
    global __data
    if __data is None:
        if sys.version_info.minor <= 8:
            f = pkg_resources.open_text(__package__, 'dataset.json')
        elif 9 <= sys.version_info.minor:
            f = pkg_resources.files(__package__).joinpath('dataset.json').open('r', encoding='utf-8')
        
        with f:
            __data = dict()
            data_file = json.load(f)
            for k, v in data_file.items():
                __data[k] = project.Project.from_data_dict(v)
    return __data

def get_single_data(leak_id: str) -> project.Project:
    global __data
    if __data is None:
        if sys.version_info.minor <= 8:
            f = pkg_resources.open_text(__package__, 'dataset.json')
        elif 9 <= sys.version_info.minor:
            f = pkg_resources.files(__package__).joinpath('dataset.json').open('r', encoding='utf-8')
        
        with f:
            data_file = json.load(f)
            if leak_id not in data_file:
                raise ValueError(f'No leak found with ID: {leak_id}')
            data = project.Project.from_data_dict(data_file[leak_id])
    else:
        if leak_id not in __data:
            raise ValueError(f'No leak found with ID: {leak_id}')
        data = __data[leak_id]
    return data