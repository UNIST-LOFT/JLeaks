public int doLogic() throws IOException 
{
    try (TaxonomyReader taxoReader = getRunData().getTaxonomyReader()) {
        getRunData().setTaxonomyReader(null);
        if (taxoReader.getRefCount() != 1) {
            System.out.println("WARNING: CloseTaxonomyReader: reference count is currently " + taxoReader.getRefCount());
        }
    }
    return 1;
}