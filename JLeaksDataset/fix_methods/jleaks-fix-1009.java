private static void writeImpl(ASceneWrapper scene, String filename, BaseTypeChecker checker) 
{
    // Sort by package name first so that output is deterministic and default package
    // comes first; within package sort by class name.
    // scene-lib bytecode lacks signature annotations
    @SuppressWarnings("signature")
    List<String> classes = new ArrayList<>(scene.getAScene().getClasses().keySet());
    Collections.sort(classes, new Comparator<String>() {

        @Override
        public int compare(String o1, String o2) {
            return ComparisonChain.start().compare(packagePart(o1), packagePart(o2), Comparator.nullsFirst(Comparator.naturalOrder())).compare(basenamePart(o1), basenamePart(o2)).result();
        }
    });
    boolean anyClassPrintable = false;
    // The writer is not initialized until it is certain that at
    // least one class can be written, to avoid empty stub files.
    // An alternate approach would be to delete the file after it is closed, if the file is empty.
    // It's not worth rewriting this code, since .stub files are obsolescent.
    FileWriter fileWriter = null;
    PrintWriter printWriter = null;
    try {
        // For each class
        for (String clazz : classes) {
            if (isPrintable(clazz, scene.getAScene().getClasses().get(clazz))) {
                if (!anyClassPrintable) {
                    try {
                        if (fileWriter != null || printWriter != null) {
                            throw new Error("This can't happen");
                        }
                        fileWriter = new FileWriter(filename);
                        printWriter = new PrintWriter(fileWriter);
                    } catch (IOException e) {
                        throw new BugInCF("error writing file during WPI: " + filename);
                    }
                    // Write out all imports
                    ImportDefWriter importDefWriter;
                    try {
                        importDefWriter = new ImportDefWriter(scene, printWriter);
                    } catch (DefException e) {
                        throw new BugInCF(e);
                    }
                    importDefWriter.visit();
                    printWriter.println("import org.checkerframework.framework.qual.AnnotatedFor;");
                    printWriter.println();
                    anyClassPrintable = true;
                }
                printClass(clazz, scene.getAScene().getClasses().get(clazz), checker, printWriter);
            }
        }
    } finally {
        if (printWriter != null) {
            // does not throw IOException
            printWriter.close();
        }
        try {
            if (fileWriter != null) {
                fileWriter.close();
            }
        } catch (IOException e) {
            // Nothing to do since exceptions thrown from a finally block have no effect.
        }
    }
}