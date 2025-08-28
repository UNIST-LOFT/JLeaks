	private HTMLNode getOverrideContent() {
		HTMLNode result = new HTMLNode("style", "type", "text/css");
		try {
			FileInputStream fis = new FileInputStream(override);
			BufferedInputStream bis = new BufferedInputStream(fis);
			DataInputStream dis = new DataInputStream(bis);
			
			result.addChild("#", DataInputStream.readUTF(dis));
			
			dis.close();
			bis.close();
			fis.close();
		} catch (IOException e) {
			Logger.error(this, "Got an IOE: " + e.getMessage(), e);
		}
		
		return result;
	}
