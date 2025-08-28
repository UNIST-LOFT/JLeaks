private void copyFilesToFatJar(List<File> libs, List<File> classes, File target) throws IOException 
{
    String fileName = opts.get("-f");
    String supersetFileName = opts.get("-s");
    try {
        J9DDRStructureStore store = new J9DDRStructureStore(fileName, supersetFileName);
        System.out.println("superset directory name : " + fileName);
        System.out.println("superset file name : " + store.getSuperSetFileName());
        try (InputStream inputStream = store.getSuperset()) {
            structureReader = new StructureReader(inputStream);
        }
    } catch (IOException e) {
        errorCount += 1;
        System.out.println("Problem with file: " + fileName);
        e.printStackTrace();
        return;
    }
    outputDir = getOutputDir("-p");
    if (opts.get("-h") != null) {
        // where to write the helpers to if the option is set
        outputDirHelpers = getOutputDir("-h");
    }
    typeManager = new StructureTypeManager(structureReader.getStructures());
    for (StructureDescriptor structure : structureReader.getStructures()) {
        try {
            if (FlagStructureList.isFlagsStructure(structure.getName())) {
                generateBuildFlags(structure);
            } else {
                generateClass(structure);
            }
        } catch (FileNotFoundException e) {
            errorCount += 1;
            String error = String.format("File not found processing: %s: %s", structure.getPointerName(), e.getMessage());
            System.out.println(error);
        } catch (IOException e) {
            errorCount += 1;
            String error = String.format("IOException processing: %s: %s", structure.getPointerName(), e.getMessage());
            System.out.println(error);
        }
    }
}