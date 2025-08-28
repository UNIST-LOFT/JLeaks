    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {
        String coords = (String) submittedValue;

        if (isValueBlank(coords)) {
            return null;
        }

        String[] cropCoords = coords.split("_");

        int x = (int) Double.parseDouble(cropCoords[0]);
        int y = (int) Double.parseDouble(cropCoords[1]);
        int w = (int) Double.parseDouble(cropCoords[2]);
        int h = (int) Double.parseDouble(cropCoords[3]);

        if (w <= 0 || h <= 0) {
            return null;
        }

        ImageCropper cropper = (ImageCropper) component;
        Resource resource = getImageResource(context, cropper);
        InputStream inputStream;
        String imagePath = cropper.getImage();
        String contentType = null;

        try {

            if (resource != null && !"RES_NOT_FOUND".equals(resource.toString())) {
                inputStream = resource.getInputStream();
                contentType = resource.getContentType();
            }
            else {

                boolean isExternal = imagePath.startsWith("http");

                if (isExternal) {
                    URL url = new URL(imagePath);
                    URLConnection urlConnection = url.openConnection();
                    inputStream = urlConnection.getInputStream();
                    contentType = urlConnection.getContentType();
                }
                else {
                    ExternalContext externalContext = context.getExternalContext();
                    File file = new File(externalContext.getRealPath("") + imagePath);
                    inputStream = new FileInputStream(file);
                }
            }

            // wrap input stream by BoundedInputStream to prevent uncontrolled resource consumption (#3286)
            if (cropper.getSizeLimit() != null) {
                inputStream = new BoundedInputStream(inputStream, cropper.getSizeLimit());
            }

            BufferedImage outputImage = ImageIO.read(inputStream);
            inputStream.close();

            // avoid java.awt.image.RasterFormatException: (x + width) is outside of Raster
            // see #1208
            if (x + w > outputImage.getWidth()) {
                w = outputImage.getWidth() - x;
            }
            if (y + h > outputImage.getHeight()) {
                h = outputImage.getHeight() - y;
            }

            BufferedImage cropped = outputImage.getSubimage(x, y, w, h);
            ByteArrayOutputStream croppedOutImage = new ByteArrayOutputStream();
            String format = guessImageFormat(contentType, imagePath);
            ImageIO.write(cropped, format, croppedOutImage);

            return new CroppedImage(cropper.getImage(), croppedOutImage.toByteArray(), x, y, w, h);
        }
        catch (IOException e) {
            throw new ConverterException(e);
        }
    }
