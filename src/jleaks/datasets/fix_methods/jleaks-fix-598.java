public long writeLogTo(long start, Writer w) throws IOException 
{
    CountingOutputStream os = new CountingOutputStream(new WriterOutputStream(w));
    try (Session f = source.open()) {
        f.skip(start);
        if (completed) {
            // write everything till EOF
            byte[] buf = new byte[1024];
            int sz;
            while ((sz = f.read(buf)) >= 0) os.write(buf, 0, sz);
        } else {
            ByteBuf buf = new ByteBuf(null, f);
            HeadMark head = new HeadMark(buf);
            TailMark tail = new TailMark(buf);
            while (tail.moveToNextLine(f)) {
                head.moveTo(tail, os);
            }
            head.finish(os);
        }
    }
    os.flush();
    return os.getCount() + start;
}