public void write(Migration migration, File file) 
{
    try (FileWriter writer = new FileWriter(file)) {
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        writer.write("<!DOCTYPE xml>\n");
        if (comment != null) {
            writer.write("<!-- ");
            writer.write(comment);
            writer.write(" -->\n");
        }
        JAXBContext jaxbContext = JAXBContext.newInstance(Migration.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        marshaller.marshal(migration, writer);
    } catch (IOException | JAXBException e) {
        throw new RuntimeException(e);
    }
}