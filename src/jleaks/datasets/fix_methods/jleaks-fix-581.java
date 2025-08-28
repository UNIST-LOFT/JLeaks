private ByteArrayOutputStream encodeToBytes() throws IOException {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(buf));
    try {
        oos.writeObject(this);
    }
    finally {
        oos.close();
    }

    ByteArrayOutputStream buf2 = new ByteArrayOutputStream();

    DataOutputStream dos = new DataOutputStream(new Base64OutputStream(buf2,true,-1,null));
    try {
        buf2.write(PREAMBLE);
        dos.writeInt(buf.size());
        buf.writeTo(dos);
    }
    finally {
        dos.close();
    }
    buf2.write(POSTAMBLE);
    return buf2;
}