public static void setLanguage(Context ctx, OsmandSettings settings) throws IOException 
{
    String lang = getPreferredLanguage(settings).getLanguage();
    m = new HashMap<String, String>();
    // The InputStream opens the resourceId and sends it to the buffer
    InputStream is = null;
    BufferedReader br = null;
    try {
        try {
            is = ctx.getAssets().open("specialphrases/specialphrases_" + lang + ".txt");
        } catch (IOException ex) {
            // second try: default to English, if this fails, the error is thrown outside
            is = ctx.getAssets().open("specialphrases/specialphrases_en.txt");
        }
        br = new BufferedReader(new InputStreamReader(is));
        String readLine = null;
        // While the BufferedReader readLine is not null
        while ((readLine = br.readLine()) != null) {
            String[] arr = readLine.split(",");
            if (arr != null && arr.length == 2) {
                m.put(arr[0], arr[1]);
            }
        }
        // Close the InputStream and BufferedReader
        is.close();
        br.close();
    } finally {
        Algoritms.closeStream(is);
        Algoritms.closeStream(br);
    }
}