public void close () throws SQLException{
    try {
        if (p_stmt != null) {
            p_stmt.close();
        }
    } finally {
        if (m_conn != null) {
            try {
                m_conn.close();
            } catch (Exception e) {
            }
        }
        m_conn = null;
        close = true;
    }
}