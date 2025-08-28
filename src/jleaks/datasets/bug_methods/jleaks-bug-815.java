    protected Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        if (!name.equals("com.sun.enterprise.iiop.IIOPHandleDelegate")) {
            return super.loadClass(name, resolve);
        }
        
        Class handleDelClass = findLoadedClass(name);
        if (handleDelClass != null) {
            return handleDelClass;
        }
        
        try {
            // read the bytes for IIOPHandleDelegate.class
            ClassLoader resCl = Thread.currentThread().getContextClassLoader();
            if (Thread.currentThread().getContextClassLoader() == null)  {
                resCl = getSystemClassLoader();
            }
            InputStream is = resCl.getResourceAsStream("org/glassfish/enterprise/iiop/impl/IIOPHandleDelegate.class");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            byte[] buf = new byte[4096]; // currently IIOPHandleDelegate is < 4k
            int nread = 0;
            while ( (nread = is.read(buf, 0, buf.length)) != -1 ) {
                baos.write(buf, 0, nread);
            }
            baos.close();
            is.close();

            byte[] buf2 = baos.toByteArray();
            
            handleDelClass = defineClass(
            "org.glassfish.enterprise.iiop.impl.IIOPHandleDelegate",
            buf2, 0, buf2.length);
            
        } catch ( Exception ex ) {
            throw (ClassNotFoundException)new ClassNotFoundException(ex.getMessage()).initCause(ex);
        }
        
        if (resolve) {
            resolveClass(handleDelClass);
        }
        
        return handleDelClass;
    }
