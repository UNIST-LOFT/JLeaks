public void close(Reporter reporter) throws IOException 
{
    try {
        if (this.m_mutator != null) {
            this.m_mutator.close();
        }
    } finally {
        if (conn != null) {
            this.conn.close();
        }
    }
}