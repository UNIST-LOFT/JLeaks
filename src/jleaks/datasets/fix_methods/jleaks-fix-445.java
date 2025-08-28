private static FormDef deserializeFormDef(File serializedFormDef) 
{
    FormDef fd;
    try (DataInputStream dis = new DataInputStream(new FileInputStream(serializedFormDef))) {
        // create new form def
        fd = new FormDef();
        // read serialized formdef into new formdef
        fd.readExternal(dis, ExtUtil.defaultPrototypes());
    } catch (Exception e) {
        Timber.e(e);
        fd = null;
    }
    return fd;
}