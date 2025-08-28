public synchronized static ArrayList<String> loadArrayList(String fileName,
boolean cache) {
    ArrayList<String> array = new ArrayList<>();
    if (!map.containsKey(fileName)) {
        try {
            File file = new File(fileName);
            if (// doesn't exist, return empty
            !file.exists())
                return array;
            try (FileReader fr = new FileReader(file);
                BufferedReader reader = new BufferedReader(fr)) {
                String add;
                while ((add = reader.readLine()) != null) array.add(add);
            }
            if (cache)
                map.put(fileName, array);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } else {
        array = map.get(fileName);
    }
    return array;
}