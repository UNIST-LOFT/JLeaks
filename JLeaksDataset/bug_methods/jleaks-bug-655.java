    public  String request(String method, String url, String req,
			int userTimeout) throws Exception {
		String result = "";
		BufferedReader in = null;
		try{
            if (this.url != url)
                this.newHttpConnection(url);

            this.connection.setConnectTimeout(timeout+userTimeout);
            this.connection.setReadTimeout(timeout+userTimeout);
	
			if (method.equals("POST")) {
				((HttpURLConnection)this.connection).setRequestMethod("POST");
	
				this.connection.setDoOutput(true);
				this.connection.setDoInput(true);
				DataOutputStream out = new DataOutputStream(this.connection.getOutputStream());
				out.writeBytes(req);
				out.flush();
				out.close();
			}

			this.connection.connect();
			int status = ((HttpURLConnection)this.connection).getResponseCode();
			if(status != 200)
				throw new CMQServerException(status);
	
			in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
	
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		}catch(Exception e){
			throw e;
		}finally{
			try {
				if (in != null) 
					in.close();
			} catch (Exception e2) {
				throw e2;
			}
		}
		
		return result;
	}
