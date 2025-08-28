public void importKFF(File kffDir) throws IOException 
{
    // $NON-NLS-1$
    File NSRLProd = new File(kffDir, "NSRLProd.txt");
    try (BufferedReader reader = new BufferedReader(new FileReader(NSRLProd))) {
        String line = reader.readLine();
        while ((line = reader.readLine()) != null) {
            int idx = line.indexOf(',');
            String key = line.substring(0, idx);
            // $NON-NLS-1$
            String[] values = line.substring(idx + 2).split("\",\"");
            String[] prod = { values[0], values[1] };
            products.put(Integer.valueOf(key), prod);
        }
    }
    for (File kffFile : kffDir.listFiles()) {
        Boolean isZip;
        if (kffFile.getName().equals("NSRLFile.txt")) {
            // $NON-NLS-1$
            isZip = false;
        } else if (kffFile.getName().equals("NSRLFile.txt.zip")) {
            // $NON-NLS-1$
            isZip = true;
        } else {
            continue;
        }
        BufferedReader reader = null;
        long length;
        if (!isZip) {
            length = kffFile.length();
            reader = new BufferedReader(new FileReader(kffFile));
        } else {
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(Files.newInputStream(kffFile.toPath())));
            ZipEntry entry = zis.getNextEntry();
            length = entry.getSize();
            reader = new BufferedReader(new InputStreamReader(zis, StandardCharsets.ISO_8859_1));
        }
        JFrame jframe = new JFrame();
        jframe.setUndecorated(true);
        jframe.setLocationRelativeTo(null);
        jframe.setVisible(true);
        int i = 0;
        long progress = 0;
        ProgressMonitor monitor = new // $NON-NLS-1$ //$NON-NLS-2$
        ProgressMonitor(// $NON-NLS-1$ //$NON-NLS-2$
        jframe, // $NON-NLS-1$ //$NON-NLS-2$
        "", // $NON-NLS-1$ //$NON-NLS-2$
        "Importing " + kffFile.getName(), // $NON-NLS-1$ //$NON-NLS-2$
        0, (int) (length / 1000));
        try {
            String line = reader.readLine();
            // $NON-NLS-1$ //$NON-NLS-2$
            String[] ignoreStrs = { "\"\"", "\"D\"" };
            while ((line = reader.readLine()) != null) {
                // $NON-NLS-1$
                String[] values = line.split(",");
                KffAttr attr = new KffAttr();
                attr.group = Integer.valueOf(values[values.length - 3]);
                if (values[values.length - 1].equals(ignoreStrs[0]) || values[values.length - 1].equals(ignoreStrs[1])) {
                    attr.group *= -1;
                }
                // else
                // System.out.println(line);
                HashValue md5 = new HashValue(values[1].substring(1, 33));
                HashValue sha1 = new HashValue(values[0].substring(1, 41));
                Integer value = md5Map.get(md5);
                if (value == null || (value > 0 && attr.group < 0)) {
                    md5Map.put(md5, attr.group);
                    sha1Map.put(sha1, attr.group);
                }
                progress += line.length() + 2;
                if (progress > i * length / 1000) {
                    if (monitor.isCanceled()) {
                        System.out.println("Canceling import...");
                        return;
                    }
                    monitor.setProgress((int) (progress / 1000));
                    i++;
                }
            }
            System.out.println("Commiting to database...");
            db.commit();
        } finally {
            reader.close();
            db.close();
            monitor.close();
            jframe.setVisible(false);
        }
    }
}