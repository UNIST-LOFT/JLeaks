public static FileSet getTransferFiles(Transferable transferable) 
{
    FileSet files;
    AbstractFile file;
    try {
        // FileSet DataFlavor
        if (transferable.isDataFlavorSupported(FILE_SET_DATA_FLAVOR)) {
            files = (FileSet) transferable.getTransferData(FILE_SET_DATA_FLAVOR);
        } else // File list DataFlavor
        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            List fileList = (List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
            int nbFiles = fileList.size();
            files = new FileSet();
            for (int i = 0; i < nbFiles; i++) {
                file = FileFactory.getFile(((File) fileList.get(i)).getAbsolutePath());
                if (file != null)
                    files.add(file);
            }
        } else // Text plain DataFlavor: assume that lines designate file paths
        if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            BufferedReader br;
            br = null;
            try {
                br = new BufferedReader(DataFlavor.getTextPlainUnicodeFlavor().getReaderForText(transferable));
                // Read input line by line and try to create AbstractFile instances
                String path;
                files = new FileSet();
                while ((path = br.readLine()) != null) {
                    // Try to create an AbstractFile instance, returned instance may be null
                    file = FileFactory.getFile(path);
                    // Safety precaution: if at least one line doesn't resolve as a file, stop reading
                    // and return null. This is to avoid any nasty effect that could arise if a random
                    // piece of text (let's say an email contents) was inadvertently pasted or dropped to muCommander.
                    if (file == null)
                        return null;
                    files.add(file);
                }
            } finally // Documentation is not explicit on whether DataFlavor streams need to be closed, we might as well
            // do so just to be sure.
            {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                    }
                }
            }
        } else {
            return null;
        }
    } catch (Exception e) {
        // Catch UnsupportedFlavorException, IOException
        if (Debug.ON)
            Debug.trace("Caught exception while processing transferable: " + e);
        return null;
    }
    return files;
}