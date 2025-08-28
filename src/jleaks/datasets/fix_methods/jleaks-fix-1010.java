public static File writeTestFile(String fullFile) throws IOException 
{
    File f = new File("Test.java");
    try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)))) {
        out.println(fullFile);
    }
    return f;
}