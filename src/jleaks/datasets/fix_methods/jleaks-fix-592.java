/*package*/ static Map<String,Long> parseRevisionFile(AbstractBuild build) throws IOException 
{
    // module -> revision
    Map<String, Long> revisions = new HashMap<String, Long>();
    {
        // read the revision file of the last build
        File file = getRevisionFile(build);
        if (!file.exists())
            // nothing to compare against
            return revisions;
        BufferedReader br = new BufferedReader(new FileReader(file));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                int index = line.lastIndexOf('/');
                if (index < 0) {
                    // invalid line?
                    continue;
                }
                try {
                    revisions.put(line.substring(0, index), Long.parseLong(line.substring(index + 1)));
                } catch (NumberFormatException e) {
                    // perhaps a corrupted line. ignore
                }
            }
        } finally {
            br.close();
        }
    }
    return revisions;
}