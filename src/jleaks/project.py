from enum import Enum
import subprocess
import os
import shutil
import sys


class Project:
    class RootCause(Enum):
        NO_CLOSE_E_PATH = 'noCloseEPath'
        NO_CLOSE_R_PATH = 'noCloseRPath'
        NOT_PRO_CLOSE = 'notProClose'
        NO_CLOSE_C_PATH = 'noCloseCPath'

        def from_str(label: str):
            if label == 'noCloseEPath':
                return Project.RootCause.NO_CLOSE_E_PATH
            elif label == 'noCloseRPath':
                return Project.RootCause.NO_CLOSE_R_PATH
            elif label == 'notProClose':
                return Project.RootCause.NOT_PRO_CLOSE
            elif label == 'noCloseCPath':
                return Project.RootCause.NO_CLOSE_C_PATH
            else:
                raise ValueError(f'Unknown RootCause: {label}')
            
        def description(self):
            if self == Project.RootCause.NO_CLOSE_E_PATH:
                return 'Resource not closed on except path (i.e. except in try-except)'
            elif self == Project.RootCause.NO_CLOSE_R_PATH:
                return 'Resource not closed on regular path'
            elif self == Project.RootCause.NOT_PRO_CLOSE:
                return 'close() not called'
            elif self == Project.RootCause.NO_CLOSE_C_PATH:
                return 'Resource not closed in every path'
            else:
                return 'Unknown RootCause'
            
        def __str__(self):
            return f'{self.value}: {self.description()}'
            
    class PatchPattern(Enum):
        TRY_WITH = 'try-with'
        CLOSE_IN_FINALLY = 'CloseInFinally'
        CLOSE_ON_E_PATH = 'CloseOnEPath'
        CLOSE_ON_R_PATH = 'CloseOnRPath'
        AOR_CLOSE = 'AoRClose'

        def from_str(label: str):
            if label == 'try-with':
                return Project.PatchPattern.TRY_WITH
            elif label == 'CloseInFinally':
                return Project.PatchPattern.CLOSE_IN_FINALLY
            elif label == 'CloseOnEPath' or label == 'CloseOnExcep':
                return Project.PatchPattern.CLOSE_ON_E_PATH
            elif label == 'CloseOnRPath':
                return Project.PatchPattern.CLOSE_ON_R_PATH
            elif label == 'AoRClose':
                return Project.PatchPattern.AOR_CLOSE
            else:
                raise ValueError(f'Unknown PatchPattern: {label}')
            
        def description(self):
            if self == Project.PatchPattern.TRY_WITH:
                return 'Use try-with-resources statement'
            elif self == Project.PatchPattern.CLOSE_IN_FINALLY:
                return 'Call close() in finally block'
            elif self == Project.PatchPattern.CLOSE_ON_E_PATH:
                return 'Call close() on except path (i.e. except in try-except)'
            elif self == Project.PatchPattern.CLOSE_ON_R_PATH:
                return 'Call close() on regular path'
            elif self == Project.PatchPattern.AOR_CLOSE:
                return 'Add or rewrite close() in all paths'
            else:
                return 'Unknown PatchPattern'
            
        def __str__(self):
            return f'{self.value}: {self.description()}'
            
    class LeakType(Enum):
        FILE = 'file'
        SOCKET = 'socket'
        THREAD = 'thread'

        def from_str(label: str):
            if label == 'file':
                return Project.LeakType.FILE
            elif label == 'socket':
                return Project.LeakType.SOCKET
            elif label == 'thread':
                return Project.LeakType.THREAD
            else:
                raise ValueError(f'Unknown LeakType: {label}')
            
        def __str__(self):
            return self.value
        
    class BuildTool(Enum):
        MAVEN = 'maven'
        GRADLE = 'gradle'
        ANT = 'ant'
        UNKNOWN = 'unknown'

        def from_str(label: str):
            if label == 'maven':
                return Project.BuildTool.MAVEN
            elif label == 'gradle':
                return Project.BuildTool.GRADLE
            elif label == 'thread':
                return Project.BuildTool.ANT
            else:                
                return Project.BuildTool.UNKNOWN

        def __str__(self):
            return self.value
            
    @staticmethod
    def from_data_dict(data: dict):
        file, method = data['method'].split(':')
        return Project(
            project=data['project'],
            id=data['id'],
            jleaks_id=data['overall_id'],
            url=data['url'],
            fixed_commit=data['fixed_commit'],
            buggy_file=file,
            buggy_method=method,
            leak_type=Project.LeakType.from_str(data['leak_type']),
            root_cause=Project.RootCause.from_str(data['root_cause']),
            patch_pattern=Project.PatchPattern.from_str(data['patch_pattern']),
            var_attr=data['var_attr'],
            std_lib=data['std_lib'],
            third_party_lib=data['third_party_lib'],
            is_interprocedural=data['is_interprocedural'],
            build_tool=Project.BuildTool.from_str(data['build_tool'])
        )

    def __init__(self, project:str, id:int, jleaks_id:int, url:str, fixed_commit:str, buggy_file:str, buggy_method:str,
                 leak_type:str, root_cause:str, patch_pattern:str, var_attr:str, *,
                 std_lib:str = '', third_party_lib:str = '', is_interprocedural:bool = False, build_tool:str = ''):
        self.project = project
        self.id = id
        self.jleaks_id = jleaks_id
        self.url = url
        self.fixed_commit = fixed_commit
        self.buggy_method = buggy_method
        self.buggy_file = buggy_file
        self.leak_type = leak_type
        self.root_cause = root_cause
        self.patch_pattern = patch_pattern
        self.std_lib = std_lib
        self.third_party_lib = third_party_lib
        self.var_attr = var_attr
        self.is_interprocedural = is_interprocedural
        self.build_tool = build_tool

    def get_info(self):
        return f"""Leak ID:\t{self}
Project:\t{self.project}
ID:\t{self.id}
ID in JLeaks Dataset:\t{self.jleaks_id}
Type of Leak:\t{self.leak_type}
Root Cause:\t{self.root_cause}
Fix Pattern:\t{self.patch_pattern}
Github URL:\t{self.url}
Fixed Commit:\t{self.fixed_commit}
Buggy File:\t{self.buggy_file}
Buggy Method:\t{self.buggy_method}
Used Standard Library:\t{self.std_lib if self.std_lib != '' else '-'}
Used 3rd-Party Library:\t{self.third_party_lib if self.third_party_lib != '' else '-'}
Variable Attribute:\t{self.var_attr}
Inter-procedural?:\t{self.is_interprocedural}
Build-Tool:\t{self.build_tool}
"""
    
    def __str__(self):
        return f'{self.project}_{self.id}'
    
    def checkout(self, workdir:str, buggy:bool = False, remove_if_exists:bool = False, log_file: str = None):
        # Check directory is exist
        if os.path.exists(workdir) and os.path.isdir(workdir) and len(os.listdir(workdir)) > 0:
            if remove_if_exists:
                print(f'Warning: {workdir} already exists and is not empty, removing it!')
                shutil.rmtree(workdir)
                os.makedirs(workdir, exist_ok=True)
            else:
                print(f'Error: {workdir} already exists and is not empty. Use -f to force remove it!')
                return False

        # Clone and checkout the repo
        log = open(log_file, 'w') if log_file else sys.stdout
        print(f'Cloning {self.url} to {workdir} ...')
        shutil.rmtree(workdir, ignore_errors=True)
        res = subprocess.run(['git', 'clone', self.url, workdir], text=True, stdout=log, stderr=subprocess.STDOUT)
        if res.returncode != 0:
            print(f'Error: git clone failed!', file=sys.stderr)
            return False
        print(f'Cloning finished.')

        print(f'Checking out commit...')
        res = subprocess.run(['git', 'checkout', self.fixed_commit], text=True, stdout=log, stderr=subprocess.STDOUT,
                             cwd=workdir)
        if res.returncode != 0:
            print(f'Error: git checkout {self.fixed_commit} failed!', file=sys.stderr)
            return False
        print(f'Checkout finished.')

        # If buggy version, copy buggy file
        if buggy:
            print(f'Checking out buggy version...')
            buggy_file_path = os.path.join(os.path.dirname(__file__), 'datasets', 'bug_files', f'jleaks-bug-{self.jleaks_id}.java')
            shutil.copy(buggy_file_path, os.path.join(workdir, self.buggy_file))
            print(f'Buggy version checked out.')
        
        if log_file:
            log.close()
        return True