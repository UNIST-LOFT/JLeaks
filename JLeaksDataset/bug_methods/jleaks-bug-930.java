private void redirectOutput(Process process) {
	final BufferedReader reader = new BufferedReader(
			new InputStreamReader(process.getInputStream()));
	new Thread(() -> {
		try {
			String line = reader.readLine();
			while (line != null) {
				System.out.println(line);
				line = reader.readLine();
				System.out.flush();
			}
			reader.close();
		}
		catch (Exception ex) {
			// Ignore
		}
	}).start();
}
