    void implClose() throws IOException {
        try (ch; out) {
            flushLeftoverChar(null, true);
            for (;;) {
                CoderResult cr = encoder.flush(bb);
                if (cr.isUnderflow())
                    break;
                if (cr.isOverflow()) {
                    assert bb.position() > 0;
                    writeBytes();
                    continue;
                }
                cr.throwException();
            }

            if (bb.position() > 0)
                writeBytes();
            if (out != null)
                out.flush();
        } catch (IOException x) {
            encoder.reset();
            throw x;
        }
    }
