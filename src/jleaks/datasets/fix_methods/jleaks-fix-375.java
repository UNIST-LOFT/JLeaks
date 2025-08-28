public void writeBeanJson(Writer writer, BeanChange bean, ChangeSet changeSet, int position) throws IOException 
{
    try (JsonGenerator generator = jsonFactory.createGenerator(writer)) {
        writeBeanChange(generator, bean, changeSet, position);
        generator.flush();
    }
}