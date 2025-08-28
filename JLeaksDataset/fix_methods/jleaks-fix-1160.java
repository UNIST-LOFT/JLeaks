private synchronized List<String> sendRawCommand(boolean expectAnswer,String command) throws MPDServerException {
		if (!isConnected())
			throw new MPDServerException("No connection to server");

		try {
			ArrayList<String> result = new ArrayList<String>();

			// send command
			outputStream.write(command);
			outputStream.flush();
			if (!expectAnswer) {
				return result;
			}
			// wait for answer
			BufferedReader in = new BufferedReader(inputStream, 1024);
			boolean anyResponse = false;
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				anyResponse = true;
				if (line.startsWith(MPD_RESPONSE_OK))
					break;

				if (line.startsWith(MPD_RESPONSE_ERR))
					throw new MPDServerException("Server error: " + line.substring(MPD_RESPONSE_ERR.length()));

				result.add(line);
			}

			// Close socket if there is no response... Something is wrong (e.g. MPD shutdown..)
			if (!anyResponse) {
				sock.close();
				throw new MPDConnectionException("Connection lost");
			}

			return result;
		} catch (SocketException e) {
			//this.sock = null; // isn't it too dangerous ?
			try{
				this.sock.close();//trying to close nicely
			}catch (IOException er) {
				throw new MPDServerException(e.getMessage(), er);
			}
			throw new MPDConnectionException("Connection lost", e);
		} catch (IOException e) {
			throw new MPDServerException(e.getMessage(), e);
		}
	}