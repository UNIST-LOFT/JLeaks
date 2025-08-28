    public synchronized static ArrayList<String> loadArrayList(String fileName,
                                                               boolean cache) {
        ArrayList<String> array = new ArrayList<>();
        if (!map.containsKey(fileName)) {
            try {
                File file = new File(fileName);
                if (!file.exists()) // doesnt exist, return empty
                    return array;

                BufferedReader reader = new BufferedReader(new FileReader(file));
                String add;

                while ((add = reader.readLine()) != null)
                    array.add(add);

                reader.close();

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
