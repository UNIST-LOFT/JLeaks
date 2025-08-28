public String unzipContent(String str) 
{
    if (str != null) {
        if (str.startsWith(" gz ")) {
            try {
                int ind = 4;
                byte[] bytes = new byte[str.length() - ind];
                for (int i = ind; i < str.length(); i++) {
                    char ch = str.charAt(i);
                    bytes[i - ind] = (byte) ((int) ch - 128 - 32);
                }
                GZIPInputStream gzn = new GZIPInputStream(new ByteArrayInputStream(bytes));
                BufferedReader br = new BufferedReader(new InputStreamReader(gzn, "UTF-8"));
                StringBuilder bld = new StringBuilder();
                String s;
                while ((s = br.readLine()) != null) {
                    bld.append(s);
                }
                br.close();
                str = bld.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    return str;
}