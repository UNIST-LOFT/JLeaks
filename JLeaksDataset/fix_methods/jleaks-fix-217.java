public static void main(String[] args) 
{
    try {
        File dataDir = new File("target/h2");
        String url = "jdbc:h2:tcp://localhost:9092/apiman";
        if (dataDir.exists()) {
            FileUtils.deleteDirectory(dataDir);
        }
        dataDir.mkdirs();
        Server.createTcpServer("-tcpPassword", "sa", "-baseDir", dataDir.getAbsolutePath(), "-tcpPort", "9092", "-tcpAllowOthers").start();
        Class.forName("org.h2.Driver");
        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            System.out.println("Connection Established: " + connection.getMetaData().getDatabaseProductName() + "/" + connection.getCatalog());
            executeUpdate(connection, "CREATE TABLE users ( username varchar(255) NOT NULL, password varchar(255) NOT NULL, PRIMARY KEY (username))");
            executeUpdate(connection, "INSERT INTO users (username, password) VALUES ('bwayne', 'ae2efd698aefdf366736a4eda1bc5241f9fbfec7')");
            executeUpdate(connection, "INSERT INTO users (username, password) VALUES ('ckent', 'ea59f7ca52a2087c99374caba0ff29be1b2dcdbf')");
            executeUpdate(connection, "INSERT INTO users (username, password) VALUES ('ballen', 'ea59f7ca52a2087c99374caba0ff29be1b2dcdbf')");
            executeUpdate(connection, "CREATE TABLE roles (rolename varchar(255) NOT NULL, username varchar(255) NOT NULL)");
            executeUpdate(connection, "INSERT INTO roles (rolename, username) VALUES ('user', 'bwayne')");
            executeUpdate(connection, "INSERT INTO roles (rolename, username) VALUES ('admin', 'bwayne')");
            executeUpdate(connection, "INSERT INTO roles (rolename, username) VALUES ('ckent', 'user')");
            executeUpdate(connection, "INSERT INTO roles (rolename, username) VALUES ('ballen', 'user')");
        }
        System.out.println("======================================================");
        System.out.println("JDBC (H2) server started successfully.");
        System.out.println("");
        System.out.println("  Data: " + dataDir.getAbsolutePath());
        System.out.println("  JDBC URL: " + url);
        System.out.println("  JDBC User: sa");
        System.out.println("  JDBC Password: ");
        System.out.println("  Authentication Query:   SELECT * FROM users u WHERE u.username = ? AND u.password = ?");
        System.out.println("  Authorization Query:    SELECT r.rolename FROM roles r WHERE r.username = ?");
        System.out.println("======================================================");
        System.out.println("");
        System.out.println("");
        System.out.println("Press Enter to stop the JDBC server.");
        new BufferedReader(new InputStreamReader(System.in)).readLine();
        System.out.println("Shutting down the JDBC server...");
        Server.shutdownTcpServer("tcp://localhost:9092", "", true, true);
        System.out.println("Done!");
    } catch (Exception e) {
        e.printStackTrace();
    }
}