public int cat(String argv[]) throws IOException 
{
    if (argv.length != 2) {
        System.out.println("Usage: tfs cat <path>");
    }
    String path = argv[1];
    String file = Utils.getFilePath(path);
    TachyonFS tachyonClient = TachyonFS.get(Utils.validatePath(path));
    try {
        TachyonFile tFile = tachyonClient.getFile(file);
        if (tFile == null) {
            System.out.println(file + " does not exist.");
            return -1;
        }
        if (tFile.isFile()) {
            InStream is = tFile.getInStream(ReadType.NO_CACHE);
            try {
                byte[] buf = new byte[512];
                int read = is.read(buf);
                while (read != -1) {
                    System.out.write(buf, 0, read);
                    read = is.read(buf);
                }
            } finally {
                is.close();
            }
            return 0;
        } else {
            System.out.println(file + " is not a file.");
            return -1;
        }
    } finally {
        tachyonClient.close();
    }
}