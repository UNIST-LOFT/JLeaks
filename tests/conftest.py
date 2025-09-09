import pytest


def pytest_addoption(parser):
    parser.addoption(
        "--run-checkout", action="store_true", default=False, help="Run slow git checkout tests"
    )

def pytest_configure(config):
    config.addinivalue_line("markers", "checkout: run slow git checkout tests")

def pytest_collection_modifyitems(config, items):
    if config.getoption("--run-checkout"):
        return
    skip_slow = pytest.mark.skip(reason="this test too slow, use --run-checkout to run")
    for item in items:
        if "checkout" in item.keywords:
            item.add_marker(skip_slow)