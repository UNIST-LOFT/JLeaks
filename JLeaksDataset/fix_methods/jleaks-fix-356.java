
    void writeConstantPool() throws IOException {
        IndexGroup cp = pkg.cp;

        if (verbose > 0)  Utils.log.info("Writing CP");

        for (int k = 0; k < ConstantPool.TAGS_IN_ORDER.length; k++) {
            byte  tag   = ConstantPool.TAGS_IN_ORDER[k];
            Index index = cp.getIndexByTag(tag);

            Entry[] cpMap = index.cpMap;
            if (verbose > 0)
                Utils.log.info("Writing "+cpMap.length+" "+ConstantPool.tagName(tag)+" entries...");

            if (optDumpBands) {
                try (PrintStream ps = new PrintStream(getDumpStream(index, ".idx"))) {
                    printArrayTo(ps, cpMap, 0, cpMap.length);
                }
            }

            switch (tag) {
            case CONSTANT_Utf8:
                writeUtf8Bands(cpMap);
                break;
            case CONSTANT_Integer:
                for (int i = 0; i < cpMap.length; i++) {
                    NumberEntry e = (NumberEntry) cpMap[i];
                    int x = ((Integer)e.numberValue()).intValue();
                    cp_Int.putInt(x);
                }
                break;
            case CONSTANT_Float:
                for (int i = 0; i < cpMap.length; i++) {
                    NumberEntry e = (NumberEntry) cpMap[i];
                    float fx = ((Float)e.numberValue()).floatValue();
                    int x = Float.floatToIntBits(fx);
                    cp_Float.putInt(x);
                }
                break;
            case CONSTANT_Long:
                for (int i = 0; i < cpMap.length; i++) {
                    NumberEntry e = (NumberEntry) cpMap[i];
                    long x = ((Long)e.numberValue()).longValue();
                    cp_Long_hi.putInt((int)(x >>> 32));
                    cp_Long_lo.putInt((int)(x >>> 0));
                }
                break;
            case CONSTANT_Double:
                for (int i = 0; i < cpMap.length; i++) {
                    NumberEntry e = (NumberEntry) cpMap[i];
                    double dx = ((Double)e.numberValue()).doubleValue();
                    long x = Double.doubleToLongBits(dx);
                    cp_Double_hi.putInt((int)(x >>> 32));
                    cp_Double_lo.putInt((int)(x >>> 0));
                }
                break;
            case CONSTANT_String:
                for (int i = 0; i < cpMap.length; i++) {
                    StringEntry e = (StringEntry) cpMap[i];
                    cp_String.putRef(e.ref);
                }
                break;
            case CONSTANT_Class:
                for (int i = 0; i < cpMap.length; i++) {
                    ClassEntry e = (ClassEntry) cpMap[i];
                    cp_Class.putRef(e.ref);
                }
                break;
            case CONSTANT_Signature:
                writeSignatureBands(cpMap);
                break;
            case CONSTANT_NameandType:
                for (int i = 0; i < cpMap.length; i++) {
                    DescriptorEntry e = (DescriptorEntry) cpMap[i];
                    cp_Descr_name.putRef(e.nameRef);
                    cp_Descr_type.putRef(e.typeRef);
                }
                break;
            case CONSTANT_Fieldref:
                writeMemberRefs(tag, cpMap, cp_Field_class, cp_Field_desc);
                break;
            case CONSTANT_Methodref:
                writeMemberRefs(tag, cpMap, cp_Method_class, cp_Method_desc);
                break;
            case CONSTANT_InterfaceMethodref:
                writeMemberRefs(tag, cpMap, cp_Imethod_class, cp_Imethod_desc);
                break;
            default:
                assert(false);
            }
        }
    }