    public byte[] write(String classname)
        throws NotFoundException, IOException, CannotCompileException
    {
        ByteArrayOutputStream barray = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(barray);
        write(classname, out, true);
        out.close();
        return barray.toByteArray();
    }
