public byte[] write(String classname){
    CtClass clazz = (CtClass) getCached(classname);
    if (callback && translator != null && (clazz == null || !clazz.isFrozen())) {
        translator.onWrite(this, classname);
        // The CtClass object might be overwritten.
        clazz = (CtClass) getCached(classname);
    }
    if (clazz == null || !clazz.isModified()) {
        if (clazz != null)
            clazz.freeze();
        source.write(classname, out);
    } else
        clazz.toBytecode(out);
}