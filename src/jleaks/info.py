import sys
from . import resources

def print_projects():
    data = resources.load_data()
    projects = dict()
    for d in data.values():
        if d.project not in projects:
            projects[d.project] = 1
        else:
            projects[d.project] += 1

    print("Project\t# of Leaks")
    for project, count in projects.items():
        print(f"{project}:\t{count}")
    print(f'\nTotal Leaks:\t{len(data)}')

def print_leak_info(leak_id: str):
    try:
        data = resources.get_single_data(leak_id)
    except ValueError:
        print(f"No leak found with ID: {leak_id}", file=sys.stderr)
        return
    
    print(data.get_info())