import argparse
import sys

if __name__ == "__main__":
    major_parser = argparse.ArgumentParser(prog='jleaks',
                                           usage='python3 -m jleaks [-h|--help] <cmd> [options...]',
                                           description="JLeaks: A Dataset of Resource Leaks in Java Projects")
    major_parser.add_argument('subcommand', choices=['info'], metavar='cmd', help='Subcommand to run (info)')
    args = major_parser.parse_args(sys.argv[1:2])

    # Add subcommand options
    if args.subcommand == 'info':
        cmd = 'info'
        sub_parser = argparse.ArgumentParser(prog=f'jleaks {cmd}',
                                             usage=f'python3 -m jleaks info [-h|--help] [leak_id]',
                                             description="Get information about the JLeaks dataset")
        sub_parser.add_argument('-i','--leak-id', required=None, default=None, metavar='leak_id',
                                help='Get information about a specific leak by its ID (e.g. sql2o_1).'
                                        'If not provided, print # of leaks per project.')
        sub_args = sub_parser.parse_args(sys.argv[2:])

    # Execute subcommand
    if cmd == 'info':
        from . import info
        if sub_args.leak_id is None:
            info.print_projects()
        else:
            info.print_leak_info(sub_args.leak_id)