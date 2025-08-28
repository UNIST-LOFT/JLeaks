        protected Object doInBackground(Object... params) {
            mode = (int) params[0];
            switch (mode) {
                case 0:
                    List<String> logList = Utils.readFile(MAGISK_LOG);

                    StringBuilder llog = new StringBuilder(15 * 10 * 1024);
                    for (String s : logList) {
                        llog.append(s).append("\n");
                    }

                    return llog.toString();

                case 1:
                    Shell.su("echo > " + MAGISK_LOG);
                    Snackbar.make(txtLog, R.string.logs_cleared, Snackbar.LENGTH_SHORT).show();
                    return "";

                case 2:
                case 3:
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                        }
                        return false;
                    }

                    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                        return false;

                    Calendar now = Calendar.getInstance();
                    String filename = String.format(
                            "magisk_%s_%04d%02d%02d_%02d%02d%02d.log", "error",
                            now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1,
                            now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE), now.get(Calendar.SECOND));

                    targetFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MagiskManager/" + filename);

                    if ((!targetFile.getParentFile().exists() && !targetFile.getParentFile().mkdirs())
                            || (targetFile.exists() && !targetFile.delete()))
                        return false;

                    List<String> in = Utils.readFile(MAGISK_LOG);

                    try {
                        FileWriter out = new FileWriter(targetFile);
                        for (String line : in) {
                            out.write(line + "\n");
                        }
                        out.close();


                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
            }
            return null;
        }
