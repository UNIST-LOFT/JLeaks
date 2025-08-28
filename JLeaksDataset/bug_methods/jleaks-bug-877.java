   static Object find(final String factoryId, final String fallbackClassName) throws ClassNotFoundException {
       ClassLoader classLoader = getContextClassLoader();

       String serviceId = "META-INF/services/" + factoryId;
       // try to find services in CLASSPATH
       try {
           InputStream is;
           if (classLoader == null) {
               is = ClassLoader.getSystemResourceAsStream(serviceId);
           } else {
               is = classLoader.getResourceAsStream(serviceId);
           }

           if (is != null) {
               BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));

               String factoryClassName = rd.readLine();
               rd.close();

               if (factoryClassName != null && !"".equals(factoryClassName)) {
                   return newInstance(factoryClassName, classLoader);
               }
           }
       } catch (Exception ex) {
           LOGGER.log(Level.FINER, "Failed to load service " + factoryId + " from " + serviceId, ex);
       }


       // try to read from $java.home/lib/jaxrs.properties
       try {
           String javah = System.getProperty("java.home");
           String configFile = javah + File.separator
                   + "lib" + File.separator + "jaxrs.properties";
           File f = new File(configFile);
           if (f.exists()) {
               Properties props = new Properties();
               props.load(new FileInputStream(f));
               String factoryClassName = props.getProperty(factoryId);
               return newInstance(factoryClassName, classLoader);
           }
       } catch (Exception ex) {
           LOGGER.log(Level.FINER, "Failed to load service " + factoryId
                   + " from $java.home/lib/jaxrs.properties", ex);
       }


       // Use the system property
       try {
           String systemProp = System.getProperty(factoryId);
           if (systemProp != null) {
               return newInstance(systemProp, classLoader);
           }
       } catch (SecurityException se) {
           LOGGER.log(Level.FINER, "Failed to load service " + factoryId
                   + " from a system property", se);
       }

       ClassLoader moduleClassLoader = getModuleClassLoader();
       if (moduleClassLoader != null) {
          try {
             InputStream is = moduleClassLoader.getResourceAsStream(serviceId);
         
             if( is!=null ) {
                 BufferedReader rd =
                     new BufferedReader(new InputStreamReader(is, "UTF-8"));
         
                 String factoryClassName = rd.readLine();
                 rd.close();

                 if (factoryClassName != null &&
                     ! "".equals(factoryClassName)) {
                     return newInstance(factoryClassName, moduleClassLoader);
                 }
             }
         } catch( Exception ex ) {
         }
       }

       if (fallbackClassName == null) {
           throw new ClassNotFoundException(
                   "Provider for " + factoryId + " cannot be found", null);
       }

       return newInstance(fallbackClassName, classLoader);
   }
