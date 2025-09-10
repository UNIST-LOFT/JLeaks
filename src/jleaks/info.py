import sys
from . import resources
from .project import Project

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
    print(f'Total projects:\t{len(projects)}')
    print(f'\nTotal Leaks:\t{len(data)}')

def print_leak_info(leak_id: str):
    try:
        data = resources.get_single_data(leak_id)
    except ValueError:
        print(f"No leak found with ID: {leak_id}", file=sys.stderr)
        return
    
    print(data.get_info())

def type_leak_projects(leak_type:str):
    type_total = 0
    type_projects = dict()
    data = resources.load_data()
    lt = Project.LeakType.from_str(leak_type)
    for d in data.values():
        if d.leak_type == lt:
            # checking buildable
            # if d.build_tool == Project.BuildTool.UNKNOWN:
            #     continue
            type_total += 1
            if d.project not in type_projects:
                type_projects[d.project] = 1
            else:
                type_projects[d.project] += 1
        else:
            continue
    print(f"{leak_type}_leak Projects")    
    for project, count in type_projects.items():
        print(f"{project}:\t{count}")
    print(f"Projects with {leak_type} leaks : {len(type_projects)} projects")
    print(f"Total - {type_total} {leak_type}_leak")