	private String readDefaultMarkdown(String file, String lc) {
		if (!StringUtils.isEmpty(lc)) {
			// convert to file_lc.mkd
			file = file.substring(0, file.lastIndexOf('.')) + "_" + lc + file.substring(file.lastIndexOf('.'));
		}
		String message;
		try {			
		    InputStream is = GitBlit.self().getResourceAsStream(file);
			InputStreamReader reader = new InputStreamReader(is, Constants.CHARACTER_ENCODING);
			message = MarkdownUtils.transformMarkdown(reader);
			reader.close();
		} catch (ResourceStreamNotFoundException t) {
			if (lc == null) {
				// could not find default language resource
				message = MessageFormat.format(getString("gb.failedToReadMessage"), file);
				error(message, t, false);
			} else {
				// ignore so we can try default language resource
				message = null;
			}
		} catch (Throwable t) {
			message = MessageFormat.format(getString("gb.failedToReadMessage"), file);
			error(message, t, false);
		}
		return message;
	}
