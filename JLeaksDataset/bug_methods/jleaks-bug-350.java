        private void dumpBand() throws IOException {
            assert(optDumpBands);
            PrintStream ps = new PrintStream(getDumpStream(this, ".txt"));
            String irr = (bandCoding == regularCoding) ? "" : " irregular";
            ps.print("# length="+length+
                     " size="+outputSize()+
                     irr+" coding="+bandCoding);
            if (metaCoding != noMetaCoding) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < metaCoding.length; i++) {
                    if (i == 1)  sb.append(" /");
                    sb.append(" ").append(metaCoding[i] & 0xFF);
                }
                ps.print(" //header: "+sb);
            }
            printArrayTo(ps, values, 0, length);
            ps.close();
            OutputStream ds = getDumpStream(this, ".bnd");
            bandCoding.writeArrayTo(ds, values, 0, length);
            ds.close();
        }
