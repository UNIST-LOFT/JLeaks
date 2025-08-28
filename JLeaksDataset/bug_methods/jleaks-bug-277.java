  private void init() throws JarResourceException {
    try {
      // extracts just sizes only.
      ZipFile zf=new ZipFile(jarFileName);
      Enumeration e=zf.entries();

      while (e.hasMoreElements()) {
        ZipEntry ze=(ZipEntry)e.nextElement();
        LOG.trace(dumpZipEntry(ze));
        htSizes.put(ze.getName(),new Integer((int)ze.getSize()));
      }
      zf.close();

      // extract resources and put them into the hashtable.
      FileInputStream fis=new FileInputStream(jarFileName);
      BufferedInputStream bis=new BufferedInputStream(fis);
      ZipInputStream zis=new ZipInputStream(bis);
      ZipEntry ze=null;
      while ((ze=zis.getNextEntry())!=null) {
        if (ze.isDirectory()) {
          continue;
        }

        LOG.trace("ze.getName()="+ze.getName()+
              ","+"getSize()="+ze.getSize() );

        int size=(int)ze.getSize();
        // -1 means unknown size.
        if (size==-1){
          size=((Integer)htSizes.get(ze.getName())).intValue();
        }

        byte[] b=new byte[(int)size];
        int rb=0;
        int chunk=0;
        while (((int)size - rb) > 0){
          chunk=zis.read(b,rb,(int)size - rb);
          if (chunk==-1){
            break;
          }
          rb+=chunk;
        }

        // add to internal resource hashtable
        htJarContents.put(ze.getName(),b);
        LOG.trace(ze.getName() + " rb=" + rb + ",size=" + size + ",csize="
                    + ze.getCompressedSize());
      }
    } catch (NullPointerException e){
      LOG.warn("Error during initialization resource. Reason {}", e.getMessage());
      throw new JarResourceException("Null pointer while loading jar file " + jarFileName);
    } catch (FileNotFoundException e) {
      LOG.warn("File {} not found. Reason : {}", jarFileName, e.getMessage());
      throw new JarResourceException("Jar file " + jarFileName + " requested to be loaded is not found");
    } catch (IOException e) {
      LOG.warn("Error while reading file {}. Reason : {}", jarFileName, e.getMessage());
      throw new JarResourceException("Error reading file " + jarFileName + ".");
    }
  }
