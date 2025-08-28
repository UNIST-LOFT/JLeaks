/*package*/ static Map<String,Long> parseRevisionFile(AbstractBuild build) throws IOException {
    Map<String,Long> revisions = new HashMap<String,Long>(); // module -> revision
    {// read the revision file of the last build
        File file = getRevisionFile(build);
        if(!file.exists())
            // nothing to compare against
            return revisions;

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while((line=br.readLine())!=null) {
            int index = line.lastIndexOf('/');
            if(index<0) {
                continue;   // invalid line?
            }
            try {
                revisions.put(line.substring(0,index), Long.parseLong(line.substring(index+1)));
            } catch (NumberFormatException e) {
                // perhaps a corrupted line. ignore
            }
        }
    }

    return revisions;
}