private void
findResolvConf(String file) {
	InputStream in;
	try {
		in = new FileInputStream(file);
	}
	catch (FileNotFoundException e) {
		return;
	}
	InputStreamReader isr = new InputStreamReader(in);
	BufferedReader br = new BufferedReader(isr);
	List<String> lserver = new ArrayList<>(0);
	List<Name> lsearch = new ArrayList<>(0);
	int lndots = -1;
	try {
		String line;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("nameserver")) {
				StringTokenizer st = new StringTokenizer(line);
				st.nextToken(); /* skip nameserver */
				addServer(st.nextToken(), lserver);
			}
			else if (line.startsWith("domain")) {
				StringTokenizer st = new StringTokenizer(line);
				st.nextToken(); /* skip domain */
				if (!st.hasMoreTokens())
					continue;
				if (lsearch.isEmpty())
					addSearch(st.nextToken(), lsearch);
			}
			else if (line.startsWith("search")) {
				if (!lsearch.isEmpty())
					lsearch.clear();
				StringTokenizer st = new StringTokenizer(line);
				st.nextToken(); /* skip search */
				while (st.hasMoreTokens())
					addSearch(st.nextToken(), lsearch);
			}
			else if(line.startsWith("options")) {
				StringTokenizer st = new StringTokenizer(line);
				st.nextToken(); /* skip options */
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					if (token.startsWith("ndots:")) {
						lndots = parseNdots(token);
					}
				}
			}
		}
		br.close();
	}
	catch (IOException e) {
	}

	configureFromLists(lserver, lsearch);
	configureNdots(lndots);
}
