	public void close () throws SQLException
	{
        if (p_stmt != null)
        {
            Connection conn = p_stmt.getConnection();
            p_stmt.close();
            
            if (!close && !useTransactionConnection)
            {
                conn.close();
            }
        }
        close = true;
	}	//	close
