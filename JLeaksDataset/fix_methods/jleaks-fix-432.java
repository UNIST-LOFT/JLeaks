private void populateDemoList() 
{
    List demoList = new ArrayList();
    URL url = BrowserMenuBar.class.getResource("/demos/file-list.txt");
    InputStream is = null;
    LineNumberReader lnr = null;
    if (url != null) {
        try {
            is = url.openStream();
            InputStreamReader reader = new InputStreamReader(is);
            lnr = new LineNumberReader(reader);
            try {
                String line;
                while ((line = lnr.readLine()) != null) {
                    demoList.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    lnr.close();
                } catch (IOException e) {
                    // swallow
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // swallow
                }
            }
        }
        for (Iterator itr = demoList.iterator(); itr.hasNext(); ) {
            String s = (String) itr.next();
            String[] s1 = s.split(",");
            allDemos.put(s1[0], s1[1]);
        }
    }
}