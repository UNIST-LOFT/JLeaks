public static void main(String[] args) throws Exception 
{
    String usage = "Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage] [-knn_vector knnHits]\n\nSee http://lucene.apache.org/core/9_0_0/demo/ for details.";
    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
        System.out.println(usage);
        System.exit(0);
    }
    String index = "index";
    String field = "contents";
    String queries = null;
    int repeat = 0;
    boolean raw = false;
    int knnVectors = 0;
    String queryString = null;
    int hitsPerPage = 10;
    for (int i = 0; i < args.length; i++) {
        switch(args[i]) {
            case "-index":
                index = args[++i];
                break;
            case "-field":
                field = args[++i];
                break;
            case "-queries":
                queries = args[++i];
                break;
            case "-query":
                queryString = args[++i];
                break;
            case "-repeat":
                repeat = Integer.parseInt(args[++i]);
                break;
            case "-raw":
                raw = true;
                break;
            case "-paging":
                hitsPerPage = Integer.parseInt(args[++i]);
                if (hitsPerPage <= 0) {
                    System.err.println("There must be at least 1 hit per page.");
                    System.exit(1);
                }
                break;
            case "-knn_vector":
                knnVectors = Integer.parseInt(args[++i]);
                break;
            default:
                System.err.println("Unknown argument: " + args[i]);
                System.exit(1);
        }
    }
    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
    IndexSearcher searcher = new IndexSearcher(reader);
    Analyzer analyzer = new StandardAnalyzer();
    KnnVectorDict vectorDict = null;
    if (knnVectors > 0) {
        vectorDict = new KnnVectorDict(Paths.get(index).resolve("knn-dict"));
    }
    BufferedReader in;
    if (queries != null) {
        in = Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8);
    } else {
        in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    }
    QueryParser parser = new QueryParser(field, analyzer);
    while (true) {
        if (queries == null && queryString == null) {
            // prompt the user
            System.out.println("Enter query: ");
        }
        String line = queryString != null ? queryString : in.readLine();
        if (line == null || line.length() == -1) {
            break;
        }
        line = line.trim();
        if (line.length() == 0) {
            break;
        }
        Query query = parser.parse(line);
        if (knnVectors > 0) {
            query = addSemanticQuery(query, vectorDict, knnVectors);
        }
        System.out.println("Searching for: " + query.toString(field));
        if (repeat > 0) {
            // repeat & time as benchmark
            Date start = new Date();
            for (int i = 0; i < repeat; i++) {
                searcher.search(query, 100);
            }
            Date end = new Date();
            System.out.println("Time: " + (end.getTime() - start.getTime()) + "ms");
        }
        doPagingSearch(in, searcher, query, hitsPerPage, raw, queries == null && queryString == null);
        if (queryString != null) {
            break;
        }
    }
    if (vectorDict != null) {
        vectorDict.close();
    }
    reader.close();
}