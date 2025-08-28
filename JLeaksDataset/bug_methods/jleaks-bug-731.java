
    public void close()
    {
        if ( !isClosed() )
        {
            super.close();
            this.relationship = NO_ID;
            this.score = Float.NaN;
            try
            {
                if ( resource != null )
                {
                    resource.close();
                    resource = null;
                }
            }
            finally
            {
                pool.accept( this );
            }
        }
    }
