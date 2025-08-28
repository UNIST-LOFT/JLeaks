void readAttrDefs() throws IOException {
    //  attr_definition_bands:
    //        *attr_definition_headers :BYTE1
    //        *attr_definition_name :UNSIGNED5  (cp_Utf8)
    //        *attr_definition_layout :UNSIGNED5  (cp_Utf8)
    attr_definition_headers.expectLength(numAttrDefs);
    attr_definition_name.expectLength(numAttrDefs);
    attr_definition_layout.expectLength(numAttrDefs);
    attr_definition_headers.readFrom(in);
    attr_definition_name.readFrom(in);
    attr_definition_layout.readFrom(in);
    PrintStream dump = !optDumpBands ? null
        : new PrintStream(getDumpStream(attr_definition_headers, ".def"));
    for (int i = 0; i < numAttrDefs; i++) {
        int       header = attr_definition_headers.getByte();
        Utf8Entry name   = (Utf8Entry) attr_definition_name.getRef();
        Utf8Entry layout = (Utf8Entry) attr_definition_layout.getRef();
        int       ctype  = (header &  ADH_CONTEXT_MASK);
        int       index  = (header >> ADH_BIT_SHIFT) - ADH_BIT_IS_LSB;
        Attribute.Layout def = new Attribute.Layout(ctype,
                                                    name.stringValue(),
                                                    layout.stringValue());
        // Check layout string for Java 6 extensions.
        String pvLayout = def.layoutForPackageMajver(getPackageMajver());
        if (!pvLayout.equals(def.layout())) {
            throw new IOException("Bad attribute layout in version 150 archive: "+def.layout());
        }
        this.setAttributeLayoutIndex(def, index);
        if (dump != null)  dump.println(index+" "+def);
    }
    if (dump != null)  dump.close();
    attr_definition_headers.doneDisbursing();
    attr_definition_name.doneDisbursing();
    attr_definition_layout.doneDisbursing();
    // Attribute layouts define bands, one per layout element.
    // Create them now, all at once.
    makeNewAttributeBands();
    attr_definition_bands.doneDisbursing();
}