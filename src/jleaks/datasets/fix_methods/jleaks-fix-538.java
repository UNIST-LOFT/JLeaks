static String[] readClassPath(File... classPathFiles) throws IOException 
{
    List<String> classpathElements = new ArrayList<>();
    // Read the content of spoon.classpath.tmp
    for (File classPathFile : classPathFiles) {
        try (BufferedReader br = new BufferedReader(new FileReader(classPathFile))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            if (!"".equals(sb.toString())) {
                String[] classpath = sb.toString().split(File.pathSeparator);
                for (String cpe : classpath) {
                    if (!classpathElements.contains(cpe)) {
                        classpathElements.add(cpe);
                    }
                }
            }
        }
    }
    return classpathElements.toArray(new String[0]);
}