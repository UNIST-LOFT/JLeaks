
    @SuppressWarnings("unchecked")
    void writeAttrDefs() throws IOException {
        List<Object[]> defList = new ArrayList<>();
        for (int i = 0; i < ATTR_CONTEXT_LIMIT; i++) {
            int limit = attrDefs.get(i).size();
            for (int j = 0; j < limit; j++) {
                int header = i;  // ctype
                if (j < attrIndexLimit[i]) {
                    header |= ((j + ADH_BIT_IS_LSB) << ADH_BIT_SHIFT);
                    assert(header < 0x100);  // must fit into a byte
                    // (...else header is simply ctype, with zero high bits.)
                    if (!testBit(attrDefSeen[i], 1L<<j)) {
                        // either undefined or predefined; nothing to write
                        continue;
                    }
                }
                Attribute.Layout def = attrDefs.get(i).get(j);
                defList.add(new Object[]{ Integer.valueOf(header), def });
                assert(Integer.valueOf(j).equals(attrIndexTable.get(def)));
            }
        }
        // Sort the new attr defs into some "natural" order.
        int numAttrDefs = defList.size();
        Object[][] defs = new Object[numAttrDefs][];
        defList.toArray(defs);
        Arrays.sort(defs, new Comparator() {
            public int compare(Object o0, Object o1) {
                Object[] a0 = (Object[]) o0;
                Object[] a1 = (Object[]) o1;
                // Primary sort key is attr def header.
                int r = ((Comparable)a0[0]).compareTo(a1[0]);
                if (r != 0)  return r;
                Object ind0 = attrIndexTable.get(a0[1]);
                Object ind1 = attrIndexTable.get(a1[1]);
                // Secondary sort key is attribute index.
                // (This must be so, in order to keep overflow attr order.)
                assert(ind0 != null);
                assert(ind1 != null);
                return ((Comparable)ind0).compareTo(ind1);
            }
        });
        attrDefsWritten = new Attribute.Layout[numAttrDefs];
        try (PrintStream dump = !optDumpBands ? null
                 : new PrintStream(getDumpStream(attr_definition_headers, ".def")))
        {
            int[] indexForDebug = Arrays.copyOf(attrIndexLimit, ATTR_CONTEXT_LIMIT);
            for (int i = 0; i < defs.length; i++) {
                int header = ((Integer)defs[i][0]).intValue();
                Attribute.Layout def = (Attribute.Layout) defs[i][1];
                attrDefsWritten[i] = def;
                assert((header & ADH_CONTEXT_MASK) == def.ctype());
                attr_definition_headers.putByte(header);
                attr_definition_name.putRef(ConstantPool.getUtf8Entry(def.name()));
                String layout = def.layoutForPackageMajver(getPackageMajver());
                attr_definition_layout.putRef(ConstantPool.getUtf8Entry(layout));
                // Check that we are transmitting that correct attribute index:
                boolean debug = false;
                assert(debug = true);
                if (debug) {
                    int hdrIndex = (header >> ADH_BIT_SHIFT) - ADH_BIT_IS_LSB;
                    if (hdrIndex < 0)  hdrIndex = indexForDebug[def.ctype()]++;
                    int realIndex = (attrIndexTable.get(def)).intValue();
                    assert(hdrIndex == realIndex);
                }
                if (dump != null) {
                    int index = (header >> ADH_BIT_SHIFT) - ADH_BIT_IS_LSB;
                    dump.println(index+" "+def);
                }
            }
        }
    }