	public String getCharset(Bucket bucket, String parseCharset) throws DataFilterException, IOException {
		logMINOR = Logger.shouldLog(Logger.MINOR, this);		
		if(logMINOR) Logger.minor(this, "getCharset(): default="+parseCharset);
		InputStream strm = bucket.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(strm, 4096);
		Writer w = new NullWriter();
		Reader r;
		r = new BufferedReader(new InputStreamReader(bis, parseCharset), 4096);
		HTMLParseContext pc = new HTMLParseContext(r, w, null, new NullFilterCallback());
		try {
			pc.run(null);
		} catch (IOException e) {
			throw e;
		} catch (Throwable t) {
			// Ignore ALL errors
			if(logMINOR) Logger.minor(this, "Caught "+t+" trying to detect MIME type with "+parseCharset);
		}
		try {
			r.close();
		} catch (IOException e) {
			throw e;
		} catch (Throwable t) {
			if(logMINOR) Logger.minor(this, "Caught "+t+" closing stream after trying to detect MIME type with "+parseCharset);
		}
		if(logMINOR) Logger.minor(this, "Returning charset "+pc.detectedCharset);
		return pc.detectedCharset;
	}

	class HTMLParseContext {
		Reader r;
		Writer w;
		String charset;
		String detectedCharset;
		final FilterCallback cb;

		HTMLParseContext(Reader r, Writer w, String charset, FilterCallback cb) {
			this.r = r;
			this.w = w;
			this.charset = charset;
			this.cb = cb;
		}

		Bucket run(Bucket temp) throws IOException, DataFilterException {

			/**
			 * TOKENIZE Modes:
			 * <p>0) in text transitions: '<' ->(1) 1) in tag, not in
			 * quotes/comment/whitespace transitions: whitespace -> (4) (save
			 * current element) '"' -> (2) '--' at beginning of tag -> (3) '>' ->
			 * process whole tag 2) in tag, in quotes transitions: '"' -> (1)
			 * '>' -> grumble about markup in quotes in tag might confuse older
			 * user-agents (stay in current state) 3) in tag, in comment
			 * transitions: '-->' -> save/ignore comment, go to (0) '<' or '>' ->
			 * grumble about markup in comments 4) in tag, in whitespace
			 * transitions: '"' -> (2) '>' -> save tag, (0) anything else not
			 * whitespace -> (1)
			 * </p>
			 */
			StringBuffer b = new StringBuffer(100);
			StringBuffer balt = new StringBuffer(4000);
			Vector splitTag = new Vector();
			String currentTag = null;
			char pprevC = 0;
			char prevC = 0;
			char c = 0;
			mode = INTEXT;

			while (true) {
				int x = r.read();
				if (x == -1) {
					switch (mode) {
						case INTEXT :
							saveText(b, currentTag, w, this);
							break;
						default :
							// Dump unfinished tag
							break;
					}
					break;
				} else {
					pprevC = prevC;
					prevC = c;
					c = (char) x;
					switch (mode) {
						case INTEXT :
							if (c == '<') {
								saveText(b, currentTag, w, this);
								b.setLength(0);
								balt.setLength(0);
								mode = INTAG;
							} else {
								b.append(c);
							}
							break;
						case INTAG :
							balt.append(c);
							if (HTMLDecoder.isWhitespace(c)) {
								splitTag.add(b.toString());
								mode = INTAGWHITESPACE;
								b.setLength(0);
							} else if ((c == '<') && Character.isWhitespace(balt.charAt(0))) {
								// Previous was an un-escaped < in a script.
								saveText(b, currentTag, w, this);

								balt.setLength(0);
								b.setLength(0);
								splitTag.clear();
							} else if (c == '>') {
								splitTag.add(b.toString());
								b.setLength(0);
								processTag(splitTag, w, this);
								currentTag = (String)splitTag.get(0);
								splitTag.clear();
								balt.setLength(0);
								mode = INTEXT;
							} else if (
								(b.length() == 2)
									&& (c == '-')
									&& (prevC == '-')
									&& (pprevC == '!')) {
								mode = INTAGCOMMENT;
								b.append(c);
							} else if (c == '"') {
								mode = INTAGQUOTES;
								b.append(c);
							} else if (c == '\'') {
								mode = INTAGSQUOTES;
								b.append(c);
							} else if (c == '/') { /* Probable end tag */
								currentTag = null; /* We didn't remember what was the last tag, so ... */
								b.append(c);
							} else {
								b.append(c);
							}
							break;
						case INTAGQUOTES :
							if (c == '"') {
								mode = INTAG;
								b.append(c); // Part of the element
							} else if (c == '>') {
								b.append("&gt;");
							} else if (c == '<') {
								b.append("&lt;");
							} else {
								b.append(c);
							}
							break;
						case INTAGSQUOTES :
							if (c == '\'') {
								mode = INTAG;
								b.append(c); // Part of the element
							} else if (c == '<') {
								b.append("&lt;");
							} else if (c == '>') {
								b.append("&gt;");
							} else {
								b.append(c);
							}
							break;
							/*
							 * Comments are often used to temporarily disable
							 * markup; I shall allow it. (avian) White space is
							 * not permitted between the markup declaration
							 * open delimiter ("
							 * <!") and the comment open delimiter ("--"), but
							 * is permitted between the comment close delimiter
							 * ("--") and the markup declaration close
							 * delimiter (">"). A common error is to include a
							 * string of hyphens ("---") within a comment.
							 * Authors should avoid putting two or more
							 * adjacent hyphens inside comments. However, the
							 * only browser that actually gets it right is IE
							 * (others either don't allow it or allow other
							 * chars as well). The only safe course of action
							 * is to allow any and all chars, but eat them.
							 * (avian)
							 */
						case INTAGCOMMENT :
							if ((b.length() >= 4) && (c == '-') && (prevC == '-')) {
								b.append(c);
								mode = INTAGCOMMENTCLOSING;
							} else
								b.append(c);
							break;
						case INTAGCOMMENTCLOSING :
							if (c == '>') {
								saveComment(b, w, this);
								b.setLength(0);
								mode = INTEXT;
							} else {
								b.append(c);
								if(c != '-')
									mode = INTAGCOMMENT;
							}
							break;
						case INTAGWHITESPACE :
							if (c == '"') {
								mode = INTAGQUOTES;
								b.append(c);
							} else if (c == '\'') {
								// e.g. <div align = 'center'> (avian)
								mode = INTAGSQUOTES;
								b.append(c);
							} else if (c == '>') {
								if (!killTag)
									processTag(splitTag, w, this);
								killTag = false;
								currentTag = (String)splitTag.get(0);
								splitTag.clear();
								b.setLength(0);
								balt.setLength(0);
								mode = INTEXT;
							} else if ((c == '<') && Character.isWhitespace(balt.charAt(0))) {
								// Previous was an un-escaped < in a script.
								saveText(balt, currentTag, w, this);
								balt.setLength(0);
								b.setLength(0);
								splitTag.clear();
								mode = INTAG;
							} else if (HTMLDecoder.isWhitespace(c)) {
								// More whitespace, what fun
							} else {
								mode = INTAG;
								b.append(c);
							}
					}
				}
			}
			return temp;
		}

		int mode;
		static final int INTEXT = 0;
		static final int INTAG = 1;
		static final int INTAGQUOTES = 2;
		static final int INTAGSQUOTES = 3;
		static final int INTAGCOMMENT = 4;
		static final int INTAGCOMMENTCLOSING = 5;
		static final int INTAGWHITESPACE = 6;
		boolean killTag = false; // just this one
		boolean writeStyleScriptWithTag = false; // just this one
		boolean expectingBadComment = false;
		// has to be set on or off explicitly by tags
		boolean inStyle = false; // has to be set on or off explicitly by tags
		boolean inScript = false; // has to be set on or off explicitly by tags
		boolean killText = false; // has to be set on or off explicitly by tags
		boolean killStyle = false;
		int styleScriptRecurseCount = 0;
		String currentStyleScriptChunk = "";
		StringBuffer writeAfterTag = new StringBuffer(1024);
	}
