protected Class loadClass(String name, boolean resolve){
    if (!name.equals("com.sun.enterprise.iiop.IIOPHandleDelegate")) {
        return super.loadClass(name, resolve);
    }
    Class handleDelClass = findLoadedClass(name);
    if (handleDelClass != null) {
        return handleDelClass;
    }
    InputStream is = null;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        // read the bytes for IIOPHandleDelegate.class
        ClassLoader resCl = Thread.currentThread().getContextClassLoader();
        if (Thread.currentThread().getContextClassLoader() == null) {
            resCl = getSystemClassLoader();
        }
        is = resCl.getResourceAsStream("org/glassfish/enterprise/iiop/impl/IIOPHandleDelegate.class");
        // currently IIOPHandleDelegate is < 4k
        byte[] buf = new byte[4096];
        int nread = 0;
        while ((nread = is.read(buf, 0, buf.length)) != -1) {
            baos.write(buf, 0, nread);
        }
        byte[] buf2 = baos.toByteArray();
        handleDelClass = defineClass("org.glassfish.enterprise.iiop.impl.IIOPHandleDelegate", buf2, 0, buf2.length);
    } catch (Exception ex) {
        throw (ClassNotFoundException) new ClassNotFoundException(ex.getMessage()).initCause(ex);
    } finally {
        if (is != null) {
            try {
                is.close();
            } catch (IOException ioe) {
                // ignore
            }
        }
    }
    if (resolve) {
        resolveClass(handleDelClass);
    }
    return handleDelClass;
}