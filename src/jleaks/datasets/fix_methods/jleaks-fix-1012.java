private void writeAjavaFile(String outputPath, CompilationUnitAnnos root) 
{
    try (Writer writer = new FileWriter(outputPath)) {
        // JavaParser can output using lexical preserving printing, which writes the file such that
        // its formatting is close to the original source file it was parsed from as
        // possible. Currently, this feature is very buggy and crashes when adding annotations in
        // certain locations. This implementation could be used instead if it's fixed in JavaParser.
        // LexicalPreservingPrinter.print(root.declaration, writer);
        // Do not print invisible qualifiers, to avoid cluttering the output.
        Set<String> invisibleQualifierNames = getInvisibleQualifierNames(this.atypeFactory);
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter() {

            @Override
            public String print(Node node) {
                VoidVisitor<Void> visitor = new DefaultPrettyPrinterVisitor(getConfiguration()) {

                    @Override
                    public void visit(final MarkerAnnotationExpr n, final Void arg) {
                        if (invisibleQualifierNames.contains(n.getName().toString())) {
                            return;
                        }
                        super.visit(n, arg);
                    }

                    @Override
                    public void visit(final SingleMemberAnnotationExpr n, final Void arg) {
                        if (invisibleQualifierNames.contains(n.getName().toString())) {
                            return;
                        }
                        super.visit(n, arg);
                    }

                    @Override
                    public void visit(final NormalAnnotationExpr n, final Void arg) {
                        if (invisibleQualifierNames.contains(n.getName().toString())) {
                            return;
                        }
                        super.visit(n, arg);
                    }
                };
                node.accept(visitor, null);
                return visitor.toString();
            }
        };
        writer.write(prettyPrinter.print(root.compilationUnit));
    } catch (IOException e) {
        throw new BugInCF("Error while writing ajava file " + outputPath, e);
    }
}