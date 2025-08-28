  public boolean add(Session session) {
    Require.nonNull("Session to add", session);

    try (Span span = tracer.getCurrentContext().createSpan(
        "INSERT into  sessions_map (session_ids, session_uri, session_caps, session_start) values (?, ?, ?, ?) ")) {
      Map<String, EventAttributeValue> attributeMap = new HashMap<>();
      SESSION_ID.accept(span, session.getId());
      SESSION_ID_EVENT.accept(attributeMap, session.getId());
      CAPABILITIES.accept(span, session.getCapabilities());
      CAPABILITIES_EVENT.accept(attributeMap, session.getCapabilities());
      setCommonSpanAttributes(span);
      setCommonEventAttributes(attributeMap);
      attributeMap.put(AttributeKey.SESSION_URI.getKey(),
                       EventAttribute.setValue(session.getUri().toString()));

      try (PreparedStatement statement = connection.prepareStatement(
          String.format("insert into %1$s (%2$s, %3$s, %4$s, %5$s, %6$s) values (?, ?, ?, ?, ?)",
                        TABLE_NAME,
                        SESSION_ID_COL,
                        SESSION_URI_COL,
                        SESSION_STEREOTYPE_COL,
                        SESSION_CAPS_COL,
                        SESSION_START_COL))) {

        statement.setString(1, session.getId().toString());
        statement.setString(2, session.getUri().toString());
        statement.setString(3, JSON.toJson(session.getStereotype()));
        statement.setString(4, JSON.toJson(session.getCapabilities()));
        statement.setString(5, JSON.toJson(session.getStartTime()));

        String statementStr = statement.toString();
        span.setAttribute(DATABASE_STATEMENT, statementStr);
        span.setAttribute(DATABASE_OPERATION, "insert");
        attributeMap.put(DATABASE_STATEMENT, EventAttribute.setValue(statementStr));
        attributeMap.put(DATABASE_OPERATION, EventAttribute.setValue("insert"));

        int rowCount = statement.executeUpdate();
        attributeMap.put("rows.added", EventAttribute.setValue(rowCount));
        span.addEvent("Inserted into the database", attributeMap);
        return rowCount >= 1;
      } catch (SQLException e) {
        span.setAttribute("error", true);
        span.setStatus(Status.CANCELLED);
        EXCEPTION.accept(attributeMap, e);
        attributeMap.put(AttributeKey.EXCEPTION_MESSAGE.getKey(),
                         EventAttribute.setValue("Unable to add session information to the database: " + e.getMessage()));
        span.addEvent(AttributeKey.EXCEPTION_EVENT.getKey(), attributeMap);

        throw new JdbcException(e);
      }
    }
  }
