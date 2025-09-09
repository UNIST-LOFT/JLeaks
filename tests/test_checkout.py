import os
from jleaks import project, resources
import pytest

@pytest.mark.checkout
def test_checkout_fixed(tmp_path):
    id = 'SpongeAPI_1'
    workdir = tmp_path / id
    data = resources.get_single_data(id)
    assert isinstance(data, project.Project)

    result = data.checkout(str(workdir), buggy=False, remove_if_exists=True)
    assert result is True
    assert os.path.exists(workdir)
    assert os.path.isfile(os.path.join(workdir, data.buggy_file))

@pytest.mark.checkout
def test_checkout_buggy(tmp_path):
    id = 'seatunnel_1'
    workdir = tmp_path / id
    data = resources.get_single_data(id)
    assert isinstance(data, project.Project)

    log_file = tmp_path / f'jleaks-test-checkout.log'
    result = data.checkout(str(workdir), buggy=True, remove_if_exists=True, log_file=str(log_file))
    assert result is True
    assert os.path.exists(workdir)
    assert os.path.isfile(os.path.join(workdir, data.buggy_file))
    assert os.path.isfile(log_file)