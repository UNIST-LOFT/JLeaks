private byte[] transform(Data dereferencedData,
XMLCryptoContext context)
throws XMLSignatureException
{
    if (md == null) {
        try {
            md = MessageDigest.getInstance(((DOMDigestMethod) digestMethod).getMessageDigestAlgorithm());
        } catch (NoSuchAlgorithmException nsae) {
            throw new XMLSignatureException(nsae);
        }
    }
    md.reset();
    DigesterOutputStream dos;
    Boolean cache = (Boolean) context.getProperty("javax.xml.crypto.dsig.cacheReference");
    if (cache != null && cache) {
        this.derefData = copyDerefData(dereferencedData);
        dos = new DigesterOutputStream(md, true);
    } else {
        dos = new DigesterOutputStream(md);
    }
    Data data = dereferencedData;
    try (OutputStream os = new UnsyncBufferedOutputStream(dos)) {
        for (int i = 0, size = transforms.size(); i < size; i++) {
            DOMTransform transform = (DOMTransform) transforms.get(i);
            if (i < size - 1) {
                data = transform.transform(data, context);
            } else {
                data = transform.transform(data, context, os);
            }
        }
        if (data != null) {
            XMLSignatureInput xi;
            // explicitly use C14N 1.1 when generating signature
            // first check system property, then context property
            boolean c14n11 = useC14N11;
            String c14nalg = CanonicalizationMethod.INCLUSIVE;
            if (context instanceof XMLSignContext) {
                if (!c14n11) {
                    Boolean prop = (Boolean) context.getProperty("com.sun.org.apache.xml.internal.security.useC14N11");
                    c14n11 = prop != null && prop;
                    if (c14n11) {
                        c14nalg = "http://www.w3.org/2006/12/xml-c14n11";
                    }
                } else {
                    c14nalg = "http://www.w3.org/2006/12/xml-c14n11";
                }
            }
            if (data instanceof ApacheData) {
                xi = ((ApacheData) data).getXMLSignatureInput();
            } else if (data instanceof OctetStreamData) {
                xi = new XMLSignatureInput(((OctetStreamData) data).getOctetStream());
            } else if (data instanceof NodeSetData) {
                TransformService spi = null;
                if (provider == null) {
                    spi = TransformService.getInstance(c14nalg, "DOM");
                } else {
                    try {
                        spi = TransformService.getInstance(c14nalg, "DOM", provider);
                    } catch (NoSuchAlgorithmException nsae) {
                        spi = TransformService.getInstance(c14nalg, "DOM");
                    }
                }
                data = spi.transform(data, context);
                xi = new XMLSignatureInput(((OctetStreamData) data).getOctetStream());
            } else {
                throw new XMLSignatureException("unrecognized Data type");
            }
            boolean secVal = Utils.secureValidation(context);
            try {
                xi.setSecureValidation(secVal);
                if (context instanceof XMLSignContext && c14n11 && !xi.isOctetStream() && !xi.isOutputStreamSet()) {
                    TransformService spi = null;
                    if (provider == null) {
                        spi = TransformService.getInstance(c14nalg, "DOM");
                    } else {
                        try {
                            spi = TransformService.getInstance(c14nalg, "DOM", provider);
                        } catch (NoSuchAlgorithmException nsae) {
                            spi = TransformService.getInstance(c14nalg, "DOM");
                        }
                    }
                    DOMTransform t = new DOMTransform(spi);
                    Element transformsElem = null;
                    String dsPrefix = DOMUtils.getSignaturePrefix(context);
                    if (allTransforms.isEmpty()) {
                        transformsElem = DOMUtils.createElement(refElem.getOwnerDocument(), "Transforms", XMLSignature.XMLNS, dsPrefix);
                        refElem.insertBefore(transformsElem, DOMUtils.getFirstChildElement(refElem));
                    } else {
                        transformsElem = DOMUtils.getFirstChildElement(refElem);
                    }
                    t.marshal(transformsElem, dsPrefix, (DOMCryptoContext) context);
                    allTransforms.add(t);
                    xi.updateOutputStream(os, true);
                } else {
                    xi.updateOutputStream(os);
                }
            } finally {
                if (xi.getOctetStreamReal() != null) {
                    xi.getOctetStreamReal().close();
                }
            }
        }
        os.flush();
        if (cache != null && cache) {
            this.dis = dos.getInputStream();
        }
        return dos.getDigestValue();
    } catch (NoSuchAlgorithmException e) {
        throw new XMLSignatureException(e);
    } catch (TransformException e) {
        throw new XMLSignatureException(e);
    } catch (MarshalException e) {
        throw new XMLSignatureException(e);
    } catch (IOException e) {
        throw new XMLSignatureException(e);
    } catch (com.sun.org.apache.xml.internal.security.c14n.CanonicalizationException e) {
        throw new XMLSignatureException(e);
    } finally {
        if (dos != null) {
            try {
                dos.close();
            } catch (IOException e) {
                throw new XMLSignatureException(e);
            }
        }
    }
}