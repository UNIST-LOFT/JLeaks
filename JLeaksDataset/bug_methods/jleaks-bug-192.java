        public ServerCache execute(HashJoinPlan parent) throws SQLException {
            List<Object> values = Lists.<Object> newArrayList();
            ResultIterator iterator = plan.iterator();
            RowProjector projector = plan.getProjector();
            ImmutableBytesWritable ptr = new ImmutableBytesWritable();
            int columnCount = projector.getColumnCount();
            int rowCount = 0;
            PDataType baseType = PVarbinary.INSTANCE;
            for (Tuple tuple = iterator.next(); tuple != null; tuple = iterator.next()) {
                if (expectSingleRow && rowCount >= 1)
                    throw new SQLExceptionInfo.Builder(SQLExceptionCode.SINGLE_ROW_SUBQUERY_RETURNS_MULTIPLE_ROWS).build().buildException();
                
                if (columnCount == 1) {
                    ColumnProjector columnProjector = projector.getColumnProjector(0);
                    baseType = columnProjector.getExpression().getDataType();
                    Object value = columnProjector.getValue(tuple, baseType, ptr);
                    values.add(value);
                } else {
                    List<Expression> expressions = Lists.<Expression>newArrayListWithExpectedSize(columnCount);
                    for (int i = 0; i < columnCount; i++) {
                        ColumnProjector columnProjector = projector.getColumnProjector(i);
                        PDataType type = columnProjector.getExpression().getDataType();
                        Object value = columnProjector.getValue(tuple, type, ptr);
                        expressions.add(LiteralExpression.newConstant(value, type));
                    }
                    Expression expression = new RowValueConstructorExpression(expressions, true);
                    baseType = expression.getDataType();
                    expression.evaluate(null, ptr);
                    values.add(baseType.toObject(ptr));
                }
                rowCount++;
            }
            
            Object result = expectSingleRow ? (values.isEmpty() ? null : values.get(0)) : PArrayDataType.instantiatePhoenixArray(baseType, values.toArray());
            parent.getContext().setSubqueryResult(select, result);
            return null;
        }
