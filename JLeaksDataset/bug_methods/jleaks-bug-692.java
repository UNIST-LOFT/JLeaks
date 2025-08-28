    public CheckSum generateCheckSum() {
        if (this.path == null) {
            return super.generateCheckSum();
        }

        InputStream stream = null;
        try {
            stream = openSqlStream();
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        }

        String procedureText = this.procedureText;
        if (stream == null && procedureText == null) {
            procedureText = "";
        }

        if (procedureText != null) {
            try {
                stream = new ByteArrayInputStream(procedureText.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError("UTF-8 is not supported by the JVM, this should not happen according to the JavaDoc of the Charset class");
            }
        }

        CheckSum checkSum = CheckSum.compute(new AbstractSQLChange.NormalizingStream(";", false, false, stream), false);

        return CheckSum.compute(super.generateCheckSum().toString()+":"+checkSum.toString());

    }
