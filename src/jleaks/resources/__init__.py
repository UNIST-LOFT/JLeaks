import importlib.resources as pkg_resources
import json
from typing import Any, Dict
import sys

__data = None

def load_data() -> Dict[str, Any]:
    global __data
    if __data is None:
        if sys.version_info.minor <= 8:
            f = pkg_resources.open_text(__package__, 'dataset.json')
        elif 9 <= sys.version_info.minor:
            f = pkg_resources.files(__package__).joinpath('dataset.json').open('r', encoding='utf-8')
        
        with f:
            __data = json.load(f)
    return __data