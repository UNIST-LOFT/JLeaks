private void writeEmailsIntoTextFile(HashSet<String> studentEmailSet,
HashSet<String> instructorEmailSet) {
    try {
        File newFile = new File(filePathForSaving + this.getCurrentDateForDisplay() + ".txt");
        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile)))) {
            int studentEmailCount = 0;
            for (String email : studentEmailSet) {
                if (!shouldIncludeTestData && email.endsWith(".tmt")) {
                    continue;
                }
                w.write(email + ",");
                studentEmailCount++;
            }
            int instructorEmailCount = 0;
            for (String email : instructorEmailSet) {
                if (!shouldIncludeTestData && email.endsWith(".tmt")) {
                    continue;
                }
                w.write(email + ",");
                instructorEmailCount++;
            }
            System.out.print("Student email num: " + studentEmailCount + "\n");
            System.out.print("Instructor email num: " + instructorEmailCount + "\n");
        }
    } catch (IOException e) {
        System.err.println("Problem writing to the file statsTest.txt");
    }
}