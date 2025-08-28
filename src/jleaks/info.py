import sys
from . import resources

def print_projects():
    data = resources.load_data()
    projects = dict()
    for d in data.values():
        if d['project'] not in projects:
            projects[d['project']] = 1
        else:
            projects[d['project']] += 1

    print("Project\t# of Leaks")
    for project, count in projects.items():
        print(f"{project}:\t{count}")
    print(f'\nTotal Leaks:\t{len(data)}')

def print_leak_info(leak_id: str):
    data = resources.load_data()
    if leak_id not in data:
        print(f"No leak found with ID: {leak_id}", file=sys.stderr)
        return
    
    leak = data[leak_id]

    print(f"Leak ID:\t{leak_id}")
    print(f'Project:\t{leak["project"]}')
    print(f'ID:\t{leak["id"]}')
    print(f'ID in JLeaks Dataset:\t{leak["overall_id"]}')
    print(f'Type of Leak:\t{leak["leak_type"]}')

    if leak['root_cause'] == 'noCloseEPath':
        print('Root Cause:\tnoCloseEPath: Resource not closed on except path (i.e. except in try-except)')
    elif leak['root_cause'] == 'noCloseRPath':
        print('Root Cause:\noCloseRPath: Resource not closed on regular path')
    elif leak['root_cause'] == 'notProClose':
        print('Root Cause:\tnotProClose: close() not called')
    elif leak['root_cause'] == 'noCloseCPath':
        print('Root Cause:\tnoCloseCPath: Resource not closed in every path')
    else:
        print(f'Root Cause:\t{leak["root_cause"]}')

    if leak['patch_pattern'] == 'try-with':
        print('Fix Pattern:\ttry-with: Use try-with-resources statement')
    elif leak['patch_pattern'] == 'CloseInFinally':
        print('Fix Pattern:\tCloseInFinally: Call close() in finally block')
    elif leak['patch_pattern'] == 'CloseOnEPath':
        print('Fix Pattern:\tCloseOnEPath: Call close() on except path (i.e. except in try-except)')
    elif leak['patch_pattern'] == 'CloseOnRPath':
        print('Fix Pattern:\tCloseOnRPath: Call close() on regular path')
    elif leak['patch_pattern'] == 'AoRClose':
        print('Fix Pattern:\tAoRClose: Add or rewrite close() in all paths')
    else:
        print(f'Fix Pattern:\t{leak["patch_pattern"]}')

    print(f'Github URL:\t{leak["url"]}')
    print(f'Fixed Commit:\t{leak["fixed_commit"]}')
    print(f'Buggy File:\t{leak["method"].split(":")[0]}')
    print(f'Buggy Method:\t{leak["method"].split(":")[1]}')
    # TODO: Add description in the method to seperate same method name with different parameters

    if leak['std_lib'] != '':
        print(f'Used Standard Library:\t{leak["std_lib"]}')
    else:
        print('Used Standard Library:\t-')
    
    if leak['third_party_lib'] != '':
        print(f'Used 3rd-Party Library:\t{leak["third_party_lib"]}')
    else:
        print('Used 3rd-Party Library:\t-')
    
    print(f'Variable Attribute:\t{leak["var_attr"]}')
    print(f'Inter-procedural?:\t{leak["is_interprocedural"]}')