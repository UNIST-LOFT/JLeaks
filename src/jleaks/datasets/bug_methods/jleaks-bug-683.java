	private void _processTemplate(TemplateResource templateResource, Writer writer)
		throws Exception {

		if (templateResource == null) {
			throw new Exception("Unable to find template resource");
		}

		Reader reader = templateResource.getReader();

		if (reader == null) {
			throw new Exception(
				"Unable to find template resource " + templateResource);
		}

		freemarker.template.Template template =
			new freemarker.template.Template(templateResource.getTemplateId(), reader, _configuration,TemplateResource.DEFAUT_ENCODING);

		template.process(_context, writer);
	}
