    public long getTemplateVirtualSize(String templatePath, String templateName) throws InternalErrorException {
        long virtualSize = 0;
        String templateFileFullPath = templatePath.endsWith(File.separator) ? templatePath : templatePath + File.separator;
        templateFileFullPath += templateName.endsWith(ImageFormat.VMDK.getFileExtension()) ? templateName : templateName + "." + ImageFormat.VMDK.getFileExtension();
        try {
            FileReader fileReader = new FileReader(templateFileFullPath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            Pattern regex = Pattern.compile("(RW|RDONLY|NOACCESS) (\\d+) (FLAT|SPARSE|ZERO|VMFS|VMFSSPARSE|VMFSDRM|VMFSRAW)");
            String line = null;
            while((line = bufferedReader.readLine()) != null) {
                Matcher m = regex.matcher(line);
                if (m.find( )) {
                    long sectors = Long.parseLong(m.group(2));
                    virtualSize = sectors * 512;
                    break;
                }
            }
            bufferedReader.close();
        } catch(FileNotFoundException ex) {
            String msg = "Unable to open file '" + templateFileFullPath + "' " + ex.toString();
            s_logger.error(msg);
            throw new InternalErrorException(msg);
        } catch(IOException ex) {
            String msg = "Unable read open file '" + templateFileFullPath + "' " + ex.toString();
            s_logger.error(msg);
            throw new InternalErrorException(msg);
        }

        s_logger.debug("vmdk file had size="+virtualSize);
        return virtualSize;
    }
