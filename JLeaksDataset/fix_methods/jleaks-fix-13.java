public static void main(String argv[]) 
{
    TFsShell shell = new TFsShell();
    int ret;
    try {
        ret = shell.run(argv);
    } finally {
        shell.close();
    }
    System.exit(ret);
}