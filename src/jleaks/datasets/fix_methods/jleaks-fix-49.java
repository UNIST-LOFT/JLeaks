protected void closeAllResources() 
{
    if (mOpenHelper != null)
        mOpenHelper.close();
}