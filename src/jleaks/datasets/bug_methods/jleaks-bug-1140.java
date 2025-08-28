  @Test public void testMultipleScannableTableFunctionWithNamedParameters()
      throws SQLException, ClassNotFoundException {
    Connection connection = DriverManager.getConnection("jdbc:calcite:");
    CalciteConnection calciteConnection =
        connection.unwrap(CalciteConnection.class);
    SchemaPlus rootSchema = calciteConnection.getRootSchema();
    SchemaPlus schema = rootSchema.add("s", new AbstractSchema());
    final TableFunction table1 = TableFunctionImpl.create(Smalls.MAZE_METHOD);
    schema.add("Maze", table1);
    final TableFunction table2 = TableFunctionImpl.create(Smalls.MAZE2_METHOD);
    schema.add("Maze", table2);
    final TableFunction table3 = TableFunctionImpl.create(Smalls.MAZE3_METHOD);
    schema.add("Maze", table3);
    final String sql = "select *\n"
        + "from table(\"s\".\"Maze\"(5, 3, 1))";
    final Statement statement = connection.createStatement();
    ResultSet resultSet = statement.executeQuery(sql);
    final String result = "S=abcde\n"
        + "S=xyz\n";
    assertThat(CalciteAssert.toString(resultSet),
        is(result + "S=generate(w=5, h=3, s=1)\n"));

    final String sql2 = "select *\n"
        + "from table(\"s\".\"Maze\"(WIDTH => 5, HEIGHT => 3, SEED => 1))";
    resultSet = statement.executeQuery(sql2);
    assertThat(CalciteAssert.toString(resultSet),
        is(result + "S=generate2(w=5, h=3, s=1)\n"));

    final String sql3 = "select *\n"
        + "from table(\"s\".\"Maze\"(HEIGHT => 3, WIDTH => 5))";
    resultSet = statement.executeQuery(sql3);
    assertThat(CalciteAssert.toString(resultSet),
        is(result + "S=generate2(w=5, h=3, s=null)\n"));

    final String sql4 = "select *\n"
        + "from table(\"s\".\"Maze\"(FOO => 'a'))";
    resultSet = statement.executeQuery(sql4);
    assertThat(CalciteAssert.toString(resultSet),
        is(result + "S=generate3(foo=a)\n"));
    connection.close();
  }
