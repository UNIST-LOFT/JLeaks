    public void importKFF(File kffDir) throws IOException {

        File NSRLProd = new File(kffDir, "NSRLProd.txt"); //$NON-NLS-1$
        BufferedReader reader = new BufferedReader(new FileReader(NSRLProd));
        String line = reader.readLine();
        while ((line = reader.readLine()) != null) {
            int idx = line.indexOf(',');
            String key = line.substring(0, idx);
            String[] values = line.substring(idx + 2).split("\",\""); //$NON-NLS-1$
            String[] prod = { values[0], values[1] };
            products.put(Integer.valueOf(key), prod);
        }
        reader.close();
        for (File kffFile : kffDir.listFiles()) {
            if (!kffFile.getName().equals("NSRLFile.txt")) { //$NON-NLS-1$
                continue;
            }
            long length = kffFile.length();
            long progress = 0;
            int i = 0;
            ProgressMonitor monitor = new ProgressMonitor(null, "", "Importing " + kffFile.getName(), 0, //$NON-NLS-1$ //$NON-NLS-2$
                    (int) (length / 1000));
            reader = new BufferedReader(new FileReader(kffFile));
            line = reader.readLine();
            String[] ignoreStrs = { "\"\"", "\"D\"" }; //$NON-NLS-1$ //$NON-NLS-2$
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(","); //$NON-NLS-1$
                KffAttr attr = new KffAttr();
                attr.group = Integer.valueOf(values[values.length - 3]);
                if (values[values.length - 1].equals(ignoreStrs[0])
                        || values[values.length - 1].equals(ignoreStrs[1])) {
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
                        return;
                    }
                    monitor.setProgress((int) (progress / 1000));
                    i++;
                }

            }
            reader.close();
            db.commit();
            db.close();
            monitor.close();
        }
    }
