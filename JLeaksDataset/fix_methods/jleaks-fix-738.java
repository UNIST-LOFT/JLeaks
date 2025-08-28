public static void runScript(String scriptFile) 
{
    try {
        DatabaseUtil.executeScript(FileUtils.readFileToString(scriptFile), false);
    } catch (Exception e) {
        e.printStackTrace();
    }
}