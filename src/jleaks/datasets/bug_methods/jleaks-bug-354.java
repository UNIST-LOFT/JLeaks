    void readConstantPool() throws IOException {
        //  cp_bands:
        //        cp_Utf8
        //        *cp_Int :UDELTA5
        //        *cp_Float :UDELTA5
        //        cp_Long
        //        cp_Double
        //        *cp_String :UDELTA5  (cp_Utf8)
        //        *cp_Class :UDELTA5  (cp_Utf8)
        //        cp_Signature
        //        cp_Descr
        //        cp_Field
        //        cp_Method
        //        cp_Imethod

        if (verbose > 0)  Utils.log.info("Reading CP");

        for (int k = 0; k < ConstantPool.TAGS_IN_ORDER.length; k++) {
            byte tag = ConstantPool.TAGS_IN_ORDER[k];
            int  len = tagCount[tag];

            Entry[] cpMap = new Entry[len];
            if (verbose > 0)
                Utils.log.info("Reading "+cpMap.length+" "+ConstantPool.tagName(tag)+" entries...");

            switch (tag) {
            case CONSTANT_Utf8:
                readUtf8Bands(cpMap);
                break;
            case CONSTANT_Integer:
                cp_Int.expectLength(cpMap.length);
                cp_Int.readFrom(in);
                for (int i = 0; i < cpMap.length; i++) {
                    int x = cp_Int.getInt();  // coding handles signs OK
                    cpMap[i] = ConstantPool.getLiteralEntry(x);
                }
                cp_Int.doneDisbursing();
                break;
            case CONSTANT_Float:
                cp_Float.expectLength(cpMap.length);
                cp_Float.readFrom(in);
                for (int i = 0; i < cpMap.length; i++) {
                    int x = cp_Float.getInt();
                    float fx = Float.intBitsToFloat(x);
                    cpMap[i] = ConstantPool.getLiteralEntry(fx);
                }
                cp_Float.doneDisbursing();
                break;
            case CONSTANT_Long:
                //  cp_Long:
                //        *cp_Long_hi :UDELTA5
                //        *cp_Long_lo :DELTA5
                cp_Long_hi.expectLength(cpMap.length);
                cp_Long_hi.readFrom(in);
                cp_Long_lo.expectLength(cpMap.length);
                cp_Long_lo.readFrom(in);
                for (int i = 0; i < cpMap.length; i++) {
                    long hi = cp_Long_hi.getInt();
                    long lo = cp_Long_lo.getInt();
                    long x = (hi << 32) + ((lo << 32) >>> 32);
                    cpMap[i] = ConstantPool.getLiteralEntry(x);
                }
                cp_Long_hi.doneDisbursing();
                cp_Long_lo.doneDisbursing();
                break;
            case CONSTANT_Double:
                //  cp_Double:
                //        *cp_Double_hi :UDELTA5
                //        *cp_Double_lo :DELTA5
                cp_Double_hi.expectLength(cpMap.length);
                cp_Double_hi.readFrom(in);
                cp_Double_lo.expectLength(cpMap.length);
                cp_Double_lo.readFrom(in);
                for (int i = 0; i < cpMap.length; i++) {
                    long hi = cp_Double_hi.getInt();
                    long lo = cp_Double_lo.getInt();
                    long x = (hi << 32) + ((lo << 32) >>> 32);
                    double dx = Double.longBitsToDouble(x);
                    cpMap[i] = ConstantPool.getLiteralEntry(dx);
                }
                cp_Double_hi.doneDisbursing();
                cp_Double_lo.doneDisbursing();
                break;
            case CONSTANT_String:
                cp_String.expectLength(cpMap.length);
                cp_String.readFrom(in);
                cp_String.setIndex(getCPIndex(CONSTANT_Utf8));
                for (int i = 0; i < cpMap.length; i++) {
                    cpMap[i] = ConstantPool.getLiteralEntry(cp_String.getRef().stringValue());
                }
                cp_String.doneDisbursing();
                break;
            case CONSTANT_Class:
                cp_Class.expectLength(cpMap.length);
                cp_Class.readFrom(in);
                cp_Class.setIndex(getCPIndex(CONSTANT_Utf8));
                for (int i = 0; i < cpMap.length; i++) {
                    cpMap[i] = ConstantPool.getClassEntry(cp_Class.getRef().stringValue());
                }
                cp_Class.doneDisbursing();
                break;
            case CONSTANT_Signature:
                readSignatureBands(cpMap);
                break;
            case CONSTANT_NameandType:
                //  cp_Descr:
                //        *cp_Descr_type :DELTA5  (cp_Signature)
                //        *cp_Descr_name :UDELTA5  (cp_Utf8)
                cp_Descr_name.expectLength(cpMap.length);
                cp_Descr_name.readFrom(in);
                cp_Descr_name.setIndex(getCPIndex(CONSTANT_Utf8));
                cp_Descr_type.expectLength(cpMap.length);
                cp_Descr_type.readFrom(in);
                cp_Descr_type.setIndex(getCPIndex(CONSTANT_Signature));
                for (int i = 0; i < cpMap.length; i++) {
                    Entry ref  = cp_Descr_name.getRef();
                    Entry ref2 = cp_Descr_type.getRef();
                    cpMap[i] = ConstantPool.getDescriptorEntry((Utf8Entry)ref,
                                                        (SignatureEntry)ref2);
                }
                cp_Descr_name.doneDisbursing();
                cp_Descr_type.doneDisbursing();
                break;
            case CONSTANT_Fieldref:
                readMemberRefs(tag, cpMap, cp_Field_class, cp_Field_desc);
                break;
            case CONSTANT_Methodref:
                readMemberRefs(tag, cpMap, cp_Method_class, cp_Method_desc);
                break;
            case CONSTANT_InterfaceMethodref:
                readMemberRefs(tag, cpMap, cp_Imethod_class, cp_Imethod_desc);
                break;
            default:
                assert(false);
            }

            Index index = initCPIndex(tag, cpMap);

            if (optDumpBands) {
                try (PrintStream ps = new PrintStream(getDumpStream(index, ".idx"))) {
                    printArrayTo(ps, index.cpMap, 0, index.cpMap.length);
                }
            }
        }

        cp_bands.doneDisbursing();

        setBandIndexes();
    }
