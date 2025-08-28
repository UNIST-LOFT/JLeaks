package amidst.clazz.real;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import amidst.documentation.Immutable;

@Immutable
public enum RealClasses {
	;

	private static final RealClassBuilder REAL_CLASS_BUILDER = new RealClassBuilder();

	public static List<RealClass> fromJarFile(File jarFile)
			throws FileNotFoundException, JarFileParsingException {
		return readRealClassesFromJarFile(jarFile);
	}

	private static List<RealClass> readRealClassesFromJarFile(File jarFile)
			throws JarFileParsingException, FileNotFoundException {
		if (!jarFile.exists()) {
			throw new FileNotFoundException("Attempted to load jar file at: "
					+ jarFile + " but it does not exist.");
		}
		try {
			ZipFile zipFile = new ZipFile(jarFile);
			List<RealClass> realClasses = readJarFile(zipFile);
			zipFile.close();
			return realClasses;
		} catch (IOException e) {
			throw new JarFileParsingException("Error extracting jar data.", e);
		} catch (RealClassCreationException e) {
			throw new JarFileParsingException("Error extracting jar data.", e);
		}
	}

	private static List<RealClass> readJarFile(ZipFile zipFile)
			throws IOException, RealClassCreationException {
		Enumeration<? extends ZipEntry> enu = zipFile.entries();
		List<RealClass> result = new ArrayList<RealClass>();
		while (enu.hasMoreElements()) {
			RealClass entry = readJarFileEntry(zipFile, enu.nextElement());
			if (entry != null) {
				result.add(entry);
			}
		}
		return result;
	}

	private static RealClass readJarFileEntry(ZipFile zipFile, ZipEntry entry)
			throws IOException, RealClassCreationException {
		String realClassName = getFileNameWithoutExtension(entry.getName(),
				"class");
		if (!entry.isDirectory() && realClassName != null) {
			BufferedInputStream stream = new BufferedInputStream(
					zipFile.getInputStream(entry));
			// TODO: Double check that this filter won't mess anything up.
			if (stream.available() < 8000) {
				byte[] classData = new byte[stream.available()];
				stream.read(classData);
				stream.close();
				return REAL_CLASS_BUILDER.construct(realClassName, classData);
			}
		}
		return null;
	}

	private static String getFileNameWithoutExtension(String fileName,
			String extension) {
		String[] split = fileName.split("\\.");
		if (split.length == 2 && split[0].indexOf('/') == -1
				&& split[1].equals(extension)) {
			return split[0];
		} else {
			return null;
		}
	}
}
