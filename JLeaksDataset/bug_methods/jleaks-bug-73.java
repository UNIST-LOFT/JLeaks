    protected Answer execute(ModifySshKeysCommand cmd) {
        File sshKeysDir = new File(SSHKEYSPATH);
        String result = null;
        if (!sshKeysDir.exists()) {
            // Change permissions for the 700
            Script script = new Script("mkdir", _timeout, s_logger);
            script.add("-m", "700");
            script.add(SSHKEYSPATH);
            script.execute();

            if (!sshKeysDir.exists()) {
                s_logger.debug("failed to create directory " + SSHKEYSPATH);
            }
        }

        File pubKeyFile = new File(SSHPUBKEYPATH);
        if (!pubKeyFile.exists()) {
            try {
                pubKeyFile.createNewFile();
            } catch (IOException e) {
                result = "Failed to create file: " + e.toString();
                s_logger.debug(result);
            }
        }

        if (pubKeyFile.exists()) {
            String pubKey = cmd.getPubKey();
            try {
                FileOutputStream pubkStream = new FileOutputStream(pubKeyFile);
                pubkStream.write(pubKey.getBytes());
                pubkStream.close();
            } catch (FileNotFoundException e) {
                result = "File" + SSHPUBKEYPATH + "is not found:" + e.toString();
                s_logger.debug(result);
            } catch (IOException e) {
                result = "Write file " + SSHPUBKEYPATH + ":" + e.toString();
                s_logger.debug(result);
            }
        }

        File prvKeyFile = new File(SSHPRVKEYPATH);
        if (!prvKeyFile.exists()) {
            try {
                prvKeyFile.createNewFile();
            } catch (IOException e) {
                result = "Failed to create file: " + e.toString();
                s_logger.debug(result);
            }
        }

        if (prvKeyFile.exists()) {
            String prvKey = cmd.getPrvKey();
            try {
                FileOutputStream prvKStream = new FileOutputStream(prvKeyFile);
                prvKStream.write(prvKey.getBytes());
                prvKStream.close();
            } catch (FileNotFoundException e) {
                result = "File" + SSHPRVKEYPATH + "is not found:" + e.toString();
                s_logger.debug(result);
            } catch (IOException e) {
                result = "Write file " + SSHPRVKEYPATH + ":" + e.toString();
                s_logger.debug(result);
            }

            Script script = new Script("chmod", _timeout, s_logger);
            script.add("600", SSHPRVKEYPATH);
            script.execute();
        }

        if (result != null) {
            return new Answer(cmd, false, result);
        } else {
            return new Answer(cmd, true, null);
        }
    }
