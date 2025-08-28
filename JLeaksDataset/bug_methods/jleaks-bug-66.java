    public boolean createVmdataFiles(final String vmName, final List<String[]> vmDataList, final String configDriveLabel) {

        // add vm iso to the isolibrary
        final String isoPath = "/tmp/"+vmName+"/configDrive/";
        final String configDriveName = "cloudstack/";

        //create folder for the VM
        //Remove the folder before creating it.

        try {
            deleteLocalFolder("/tmp/"+isoPath);
        } catch (final IOException e) {
            s_logger.debug("Failed to delete the exiting config drive for vm "+vmName+ " "+ e.getMessage());
        } catch (final Exception e) {
            s_logger.debug("Failed to delete the exiting config drive for vm "+vmName+ " "+ e.getMessage());
        }


        if (vmDataList != null) {
            for (final String[] item : vmDataList) {
                final String dataType = item[0];
                final String fileName = item[1];
                final String content = item[2];

                // create file with content in folder

                if (dataType != null && !dataType.isEmpty()) {
                    //create folder
                    final String  folder = isoPath+configDriveName+dataType;
                    if (folder != null && !folder.isEmpty()) {
                        final File dir = new File(folder);
                        final boolean result = true;

                        try {
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }
                        }catch (final SecurityException ex) {
                            s_logger.debug("Failed to create dir "+ ex.getMessage());
                            return false;
                        }

                        if (result && content != null && !content.isEmpty()) {
                            try {
                                final File file = new File(folder+"/"+fileName+".txt");
                                final OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()),"UTF-8");
                                final BufferedWriter bw = new BufferedWriter(fw);
                                bw.write(content);
                                bw.close();
                                s_logger.debug("created file: "+ file + " in folder:"+folder);
                            } catch (final IOException ex) {
                                s_logger.debug("Failed to create file "+ ex.getMessage());
                                return false;
                            }
                        }
                    }
                }
            }
            s_logger.debug("Created the vm data in "+ isoPath);
        }

        String s = null;
        try {

            final String cmd =  "mkisofs -iso-level 3 -V "+ configDriveLabel +" -o "+ isoPath+vmName +".iso " + isoPath;
            final Process p = Runtime.getRuntime().exec(cmd);

            final BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream(),Charset.defaultCharset()));

            final BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream(),Charset.defaultCharset()));

            // read the output from the command
            while ((s = stdInput.readLine()) != null) {
                s_logger.debug(s);
            }

            // read any errors from the attempted command
            while ((s = stdError.readLine()) != null) {
                s_logger.debug(s);
            }
            s_logger.debug(" Created config drive ISO using the command " + cmd +" in the host "+ _host.getIp());
        } catch (final IOException e) {
            s_logger.debug(e.getMessage());
            return false;
        }

        return true;
    }
