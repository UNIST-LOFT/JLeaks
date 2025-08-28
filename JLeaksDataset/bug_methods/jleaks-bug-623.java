  public void loadPropertyFile(String file, Properties target)
  {
    try
    {
      // Use SecuritySupport class to provide priveleged access to property file
      InputStream is = SecuritySupport.getResourceAsStream(ObjectFactory.findClassLoader(),
                                              file);

      // get a buffered version
      BufferedInputStream bis = new BufferedInputStream(is);

      target.load(bis);  // and load up the property bag from this
      bis.close();  // close out after reading
    }
    catch (Exception ex)
    {
      // ex.printStackTrace();
      throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(ex);
    }
  }
