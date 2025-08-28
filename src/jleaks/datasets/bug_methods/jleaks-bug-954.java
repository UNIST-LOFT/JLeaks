    private void writeDppResult(DppResult dppResult) throws Exception {
        String outputPath = etlJobConfig.getOutputPath();
        String resultFilePath = outputPath + "/" + DPP_RESULT_FILE;
        URI uri = new URI(outputPath);
        FileSystem fs = FileSystem.get(uri, serializableHadoopConf.value());
        Path filePath = new Path(resultFilePath);
        FSDataOutputStream outputStream = fs.create(filePath);
        Gson gson = new Gson();
        outputStream.write(gson.toJson(dppResult).getBytes());
        outputStream.write('\n');
        outputStream.close();
    }
