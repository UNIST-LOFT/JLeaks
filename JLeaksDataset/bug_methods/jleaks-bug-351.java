        private void dumpBand() throws IOException {
            assert(optDumpBands);
            OutputStream ds = getDumpStream(this, ".bnd");
            if (bytesForDump != null)
                bytesForDump.writeTo(ds);
            else
                bytes.writeTo(ds);
            ds.close();
        }
