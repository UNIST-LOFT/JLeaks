	public void handlePost(URI uri, Bucket data, ToadletContext ctx) throws ToadletContextClosedException, IOException, RedirectException {
		if(data.size() > 1024*1024) {
			this.writeReply(ctx, 400, "text/plain", "Too big", "Too much data, darknet toadlet limited to 1MB");
			return;
		}
		HTTPRequest request;
		request = new HTTPRequest(uri, data, ctx);
		
		if (request.isPartSet("connect")) {
			// connect to a new node
			String urltext = request.getPartAsString("url", 100);
			urltext = urltext.trim();
			String reftext = request.getPartAsString("ref", 2000);
			reftext = reftext.trim();
			if (reftext.length() < 200) {
				reftext = request.getPartAsString("reffile", 2000);
			}
			reftext.trim();
			
			String ref = new String("");
			
			if (urltext.length() > 0) {
				// fetch reference from a URL
				try {
					URL url = new URL(urltext);
					URLConnection uc = url.openConnection();
					BufferedReader in = new BufferedReader(
							new InputStreamReader(uc.getInputStream()));
					String line;
					while ( (line = in.readLine()) != null) {
						ref += line+"\n";
					}
				} catch (Exception e) {
					this.sendErrorPage(ctx, 200, "OK", "Failed to add node: Unable to retrieve node reference from "+urltext+".");
				}
			} else if (reftext.length() > 0) {
				// read from post data or file upload
				// this slightly scary looking regexp chops any extra characters off the beginning or ends of lines and removes extra line breaks
				ref = reftext.replaceAll(".*?((?:[\\w,\\.]+\\=[^\r\n]+)|(?:End)).*(?:\\r?\\n)*", "$1\n");
				if (ref.endsWith("\n")) {
					ref = ref.substring(0, ref.length() - 1);
				}
			} else {
				this.sendErrorPage(ctx, 200, "OK", "Failed to add node: Could not detect either a node reference or a URL. Please <a href=\".\">Try again</a>.");
				request.freeParts();
				return;
			}
			
			request.freeParts();
			// we have a node reference in ref
			SimpleFieldSet fs;
			
			try {
				fs = new SimpleFieldSet(ref, false);
			} catch (IOException e) {
				this.sendErrorPage(ctx, 200, "Failed to add node", "Unable to parse the given text: <pre>"+ref+"</pre> as a node reference: "+e+" Please <a href=\".\">Try again</a>.");
				return;
			}
			PeerNode pn;
			try {
				pn = new PeerNode(fs, this.node);
			} catch (FSParseException e1) {
				this.sendErrorPage(ctx, 200, "Failed to add node", "Unable to parse the given text: <pre>"+ref+"</pre> as a node reference: "+e1+". Please <a href=\".\">Try again</a>.");
				return;
			} catch (PeerParseException e1) {
				this.sendErrorPage(ctx, 200, "Failed to add node", "Unable to parse the given text: <pre>"+ref+"</pre> as a node reference: "+e1+". Please <a href=\".\">Try again</a>.");
				return;
			}
			if(!this.node.addDarknetConnection(pn)) {
				this.sendErrorPage(ctx, 200, "Failed to add node", "We already have the given reference. Return to the connections page <a href=\".\">here</a>.");
				return;
			}
		} else if (request.isPartSet("disconnect")) {
			//int hashcode = Integer.decode(request.getParam("node")).intValue();
			
			PeerNode[] peerNodes = node.getDarknetConnections();
			for(int i = 0; i < peerNodes.length; i++) {
				if (request.isPartSet("delete_node_"+peerNodes[i].hashCode())) {
					this.node.removeDarknetConnection(peerNodes[i]);
				}
			}
		}
		this.handleGet(uri, ctx);
	}
