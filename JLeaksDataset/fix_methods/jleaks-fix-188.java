public void run(Result result, SchedulerEvent event) 
{
    if (!(event instanceof CursorMovedSchedulerEvent)) {
        return;
    }
    CursorMovedSchedulerEvent evt = (CursorMovedSchedulerEvent) event;
    final CompilationInfo[] info = new CompilationInfo[] { CompilationInfo.get(result) };
    int start = evt.getMarkOffset();
    int end = evt.getCaretOffset();
    if (info[0] == null) {
        TokenSequence<TestTokenId> ts = result.getSnapshot().getTokenHierarchy().tokenSequence(TestTokenId.language());
        if (ts == null)
            return;
        ts.move(evt.getCaretOffset());
        if (!ts.moveNext() || ts.token().id() != TestTokenId.JAVA_CODE)
            return;
        int tokenStart = ts.offset();
        int tokenEnd = ts.offset() + ts.token().length();
        if (evt.getCaretOffset() < tokenStart || tokenEnd < evt.getCaretOffset())
            return;
        if (evt.getMarkOffset() < tokenStart || tokenEnd < evt.getMarkOffset())
            return;
        start -= ts.offset();
        end -= ts.offset();
        try {
            FileObject file = FileUtil.createMemoryFileSystem().getRoot().createData("Test.java");
            try (Writer out = new OutputStreamWriter(file.getOutputStream(), StandardCharsets.UTF_8)) {
                out.write(ts.token().text().toString());
            }
            ClasspathInfo cpInfo = ClasspathInfo.create(file);
            JavaSource.create(cpInfo, file).runUserActionTask((CompilationController parameter) -> {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                info[0] = parameter;
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
    }
    if (info[0] == null) {
        // ??
        return;
    }
    Document[] documents = ToggleDebuggingAction.debuggingEnabled.toArray(new Document[0]);
    for (final Document doc : documents) {
        assert doc != null;
        List<int[]> passed = new LinkedList<>();
        List<int[]> failed = new LinkedList<>();
        final String[] text = new String[1];
        doc.render(() -> text[0] = DocumentUtilities.getText(doc).toString());
        Collection<? extends HintWrapper> hints = HintWrapper.parse(NbEditorUtilities.getFileObject(doc), text[0]);
        computeHighlights(info[0], start, end, hints, passed, failed);
        OffsetsBag bag = new OffsetsBag(doc);
        for (int[] span : passed) {
            bag.addHighlight(span[0], span[1], PASSED);
        }
        for (int[] span : failed) {
            bag.addHighlight(span[0], span[1], FAILED);
        }
        DebuggingHighlightsLayerFactory.getBag(doc).setHighlights(bag);
    }
}