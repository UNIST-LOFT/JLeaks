	public Area loadPolygon() {
		try {
			Area resultArea;
			BufferedReader bufferedReader;
			// Create a new area.
			resultArea = new Area();

            // Open the polygon file.
            bufferedReader = new BufferedReader(fileReader);

            // Read the file header.
            myPolygonName = bufferedReader.readLine();
            if (myPolygonName == null || myPolygonName.trim().length() == 0) {
                 throw new OsmosisRuntimeException("The file must begin with a header naming the polygon file.");
            }

			// We now loop until no more sections are available.
			while (true) {
				String sectionHeader;
				boolean positivePolygon;
				Area sectionArea;
				
				// Read until a non-empty line is obtained.
				do {
					// Read the section header.
					sectionHeader = bufferedReader.readLine();
					
					// It is invalid for the file to end without a global "END" record.
					if (sectionHeader == null) {
						throw new OsmosisRuntimeException("File terminated prematurely without a section END record.");
					}
					
					// Remove any whitespace.
					sectionHeader = sectionHeader.trim();
					
				} while (sectionHeader.length() == 0);
				
				// Stop reading when the global END record is reached.
				if ("END".equals(sectionHeader)) {
					break;
				}
				
				// If the section header begins with a ! then the polygon is to
				// be subtracted from the result area.
				positivePolygon = (sectionHeader.charAt(0) != '!');
				
				// Create an area for this polygon.
				sectionArea = loadSectionPolygon(bufferedReader);
				
				// Add or subtract the section area from the overall area as
				// appropriate.
				if (positivePolygon) {
					resultArea.add(sectionArea);
				} else {
					resultArea.subtract(sectionArea);
				}
			}
			
			return resultArea;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read from polygon file " + polygonFile + ".", e);
		} finally {
			cleanup();
		}
	}
