public List<ModelData> readDataFromFile(String fileName) throws JsonParseException, JsonMappingException, IOException 
{
    try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
        if (is == null) {
            throw new FileNotFoundException("Cannot find file " + fileName + " on classpath");
        }
        return Json.mapper().readValue(is, MODEL_DATA_TYPE);
    }
}