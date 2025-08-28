    private void writeEmailsIntoTextFile(HashSet<String> studentEmailSet,
                                         HashSet<String> instructorEmailSet) {

        try {

            File newFile = new File(filePathForSaving + this.getCurrentDateForDisplay() + ".txt");
            FileOutputStream fos = new FileOutputStream(newFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            Writer w = new BufferedWriter(osw);

            int studentEmailCount = 0;
            if (!studentEmailSet.isEmpty()) {
                for (String email : studentEmailSet) {
                    if (!shouldIncludeTestData && email.endsWith(".tmt")) {
                        continue;
                    }
                    w.write(email + ",");
                    studentEmailCount++;
                }
            }

            int instructorEmailCount = 0;
            if (!instructorEmailSet.isEmpty()) {
                for (String email : instructorEmailSet) {
                    if (!shouldIncludeTestData && email.endsWith(".tmt")) {
                        continue;
                    }
                    w.write(email + ",");
                    instructorEmailCount++;
                }
            }

            System.out.print("Student email num: " + studentEmailCount + "\n");
            System.out.print("Instructor email num: " + instructorEmailCount + "\n");
            w.close();

        } catch (IOException e) {
            System.err.println("Problem writing to the file statsTest.txt");
        }
    }
