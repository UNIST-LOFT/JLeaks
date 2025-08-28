    private static HashMap<String, Object> getProject(File file) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] key = "sketchwaresecure".getBytes();
            cipher.init(2, new SecretKeySpec(key, "AES"), new IvParameterSpec(key));
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            byte[] encrypted = new byte[(int) raf.length()];
            raf.readFully(encrypted);
            byte[] decrypted = cipher.doFinal(encrypted);
            String decryptedString = new String(decrypted);

            return new Gson().fromJson(decryptedString.trim(), Helper.TYPE_MAP);
        } catch (Exception e) {
            return null;
        }
    }
