    TrueTypeFont(String platname, Object nativeNames, int fIndex,
                 boolean javaRasterizer)
        throws FontFormatException {
        super(platname, nativeNames);
        useJavaRasterizer = javaRasterizer;
        fontRank = Font2D.TTF_RANK;
        verify();
        init(fIndex);
        Disposer.addObjectRecord(this, disposerRecord);
    }
