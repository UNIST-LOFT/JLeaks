from jleaks import info

def test_print_projects(capsys):
    info.print_projects()
    captured = capsys.readouterr()

    assert 'Apktool:\t5' in captured.out
    assert 'Total Leaks:\t1160' in captured.out
    assert 'triplea:\t21' in captured.out

def test_print_leak(capsys):
    info.print_leak_info('WALA_1')
    captured = capsys.readouterr()

    assert 'Leak ID:\tWALA_1' in captured.out
    assert 'Project:\tWALA' in captured.out
    assert 'ID:\t1' in captured.out
    assert 'ID in JLeaks Dataset:\t1061' in captured.out
    assert 'Type of Leak:\tfile' in captured.out
    assert 'Root Cause:\tnoCloseEPath: Resource not closed on except path (i.e. except in try-except)' in captured.out
    assert 'Buggy File:\tsource/com/ibm/wala/cast/js/ipa/callgraph/LoadFileTargetSelector.java'
    assert 'Buggy Method:\tgetCalleeTarget' in captured.out
    assert 'Used Standard Library:\tjava.io.InputStream' in captured.out
    assert 'Used 3rd-Party Library:\t-' in captured.out

    info.print_leak_info('non_existing_id')
    captured = capsys.readouterr()
    assert 'No leak found with ID: non_existing_id' in captured.err