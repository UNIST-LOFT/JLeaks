private void _processTemplate(TemplateResource templateResource, Writer writer)
throws Exception {

		Reader reader = null;

		try {
			reader = templateResource.getReader();

			Template template = new Template(templateResource.getTemplateId(), reader, _configuration,TemplateResource.DEFAUT_ENCODING);

			template.process(_context, writer);
		}
		finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
