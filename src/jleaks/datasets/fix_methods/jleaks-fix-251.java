public Object source(URL url, NameSpace namespace)
throws EvalError, IOException {
Interpreter.debug("Sourcing file: ", url.toString());
try (Reader fileRead = new FileReader(url.openStream());
 Reader sourceIn = new BufferedReader(fileRead)) {
return eval( sourceIn, namespace, url.toString() );
}
}