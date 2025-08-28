    public static void save(Fingerprint fp, File file) throws IOException {
        if (fp.getPersistedFacets().isEmpty()) {
            file.getParentFile().mkdirs();
            // JENKINS-16301: fast path for the common case.
            AtomicFileWriter afw = new AtomicFileWriter(file);
            try {
                PrintWriter w = new PrintWriter(afw);
                w.println("<?xml version='1.1' encoding='UTF-8'?>");
                w.println("<fingerprint>");
                w.print("  <timestamp>");
                w.print(DATE_CONVERTER.toString(fp.getTimestamp()));
                w.println("</timestamp>");
                if (fp.getOriginal() != null) {
                    w.println("  <original>");
                    w.print("    <name>");
                    w.print(Util.xmlEscape(fp.getOriginal().getName()));
                    w.println("</name>");
                    w.print("    <number>");
                    w.print(fp.getOriginal().getNumber());
                    w.println("</number>");
                    w.println("  </original>");
                }
                // TODO(oleg_nenashev): Consider renaming the field: https://issues.jenkins-ci.org/browse/JENKINS-25808
                w.print("  <md5sum>");
                w.print(fp.getHashString());
                w.println("</md5sum>");
                w.print("  <fileName>");
                w.print(Util.xmlEscape(fp.getFileName()));
                w.println("</fileName>");
                w.println("  <usages>");
                for (Map.Entry<String, Fingerprint.RangeSet> e : fp.getUsages().entrySet()) {
                    w.println("    <entry>");
                    w.print("      <string>");
                    w.print(Util.xmlEscape(e.getKey()));
                    w.println("</string>");
                    w.print("      <ranges>");
                    w.print(Fingerprint.RangeSet.ConverterImpl.serialize(e.getValue()));
                    w.println("</ranges>");
                    w.println("    </entry>");
                }
                w.println("  </usages>");
                w.println("  <facets/>");
                w.print("</fingerprint>");
                w.flush();
                afw.commit();
            } finally {
                afw.abort();
            }
        } else {
            // Slower fallback that can persist facets.
            getConfigFile(file).write(fp);
        }
    }
