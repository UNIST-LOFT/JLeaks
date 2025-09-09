import argparse
import sys

if __name__ == "__main__":
    major_parser = argparse.ArgumentParser(prog='jleaks',
                                           usage='python3 -m jleaks [-h|--help] <cmd> [options...]',
                                           description="JLeaks: A Dataset of Resource Leaks in Java Projects")
    major_parser.add_argument('subcommand', choices=['info', 'checkout'], metavar='cmd', help='Subcommand to run (info)')
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

    elif args.subcommand == 'checkout':
        cmd = 'checkout'
        sub_parser = argparse.ArgumentParser(prog=f'jleaks {cmd}',
                                             usage=f'python3 -m jleaks checkout [-h|--help] leak_id workdir [options...]',
                                             description="Checkout the project at the workdir")
        sub_parser.add_argument('leak_id', metavar='leak_id', help='The ID of the leak to checkout (e.g. sql2o_1)')
        sub_parser.add_argument('workdir', metavar='workdir', help='The directory to checkout the project')
        sub_parser.add_argument('-b','--buggy', action='store_true', default=False,
                                help='Checkout the buggy version (default: fixed version)')
        sub_parser.add_argument('-f','--force', action='store_true', default=False,
                                help='Force remove the workdir if it already exists and is not empty')
        sub_parser.add_argument('-l','--log-file', required=None, default=None, metavar='log_file',
                                help='Log file to store the git clone and checkout logs (default: stdout)')
        sub_args = sub_parser.parse_args(sys.argv[2:])

    # Execute subcommand
    if cmd == 'info':
        from . import info
        if sub_args.leak_id is None:
            info.print_projects()
        else:
            info.print_leak_info(sub_args.leak_id)

    elif cmd == 'checkout':
        from . import resources
        try:
            data = resources.get_single_data(sub_args.leak_id)
        except ValueError:
            print(f"No leak found with ID: {sub_args.leak_id}", file=sys.stderr)
            sys.exit(1)
        
        result = data.checkout(sub_args.workdir, buggy=sub_args.buggy, remove_if_exists=sub_args.force, log_file=sub_args.log_file)
        if not result:
            sys.exit(1)