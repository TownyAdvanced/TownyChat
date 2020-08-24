package com.palmergames.bukkit.TownyChat.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import com.palmergames.bukkit.TownyChat.Chat;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.reader.UnicodeReader;


public class FileMgmt {

	public static void checkFolders(String[] folders) {
		for (String folder : folders) {
			File f = new File(folder);
			if (!(f.exists() && f.isDirectory()))
				f.mkdir();
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getFile(String filepath) {

		try {
			File f = new File(filepath);
			
			Yaml yamlChannels = new Yaml(new SafeConstructor());
			Object channelsRootDataNode;

			FileInputStream fileInputStream = new FileInputStream(f);
			try {
				channelsRootDataNode = yamlChannels.load(new UnicodeReader(fileInputStream));
				if (channelsRootDataNode == null) {
					throw new NullPointerException();
				}
			} catch (Exception ex) {
				throw new IllegalArgumentException("The following file couldn't pass on Parser.\n" + f.getPath(), ex);
			} finally {
				fileInputStream.close();
			}

			if (channelsRootDataNode instanceof Map)
				return (Map<String, Object>) channelsRootDataNode;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getFile(String filepath, String resource, Chat plugin) {

		try {
			File f = new File(filepath);
			if (!(f.exists() && f.isFile())) {
				// Populate a new file
				try {
					String resString = convertStreamToString("/" + resource);
					// If we have a plugin reference pass to load default.
					
					FileMgmt.stringToFile(resString, filepath);

				} catch (IOException e) {
					// No resource file found
					e.printStackTrace();
					return null;
				}
			}

			f = new File(filepath);

			Yaml yamlChannels = new Yaml(new SafeConstructor());
			Object channelsRootDataNode;

			FileInputStream fileInputStream = new FileInputStream(f);
			try {
				channelsRootDataNode = yamlChannels.load(new UnicodeReader(fileInputStream));
				if (channelsRootDataNode == null) {
					throw new NullPointerException();
				}
			} catch (Exception ex) {
				throw new IllegalArgumentException("The following file couldn't pass on Parser.\n" + f.getPath(), ex);
			} finally {
				fileInputStream.close();
			}

			if (channelsRootDataNode instanceof Map)
				return (Map<String, Object>) channelsRootDataNode;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	// pass a resource name and it will return it's contents as a string
	public static String convertStreamToString(String name) throws IOException {
		if (name != null) {
			Writer writer = new StringWriter();
			InputStream is = FileMgmt.class.getResourceAsStream(name);

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} catch (IOException e) {
				System.out.println("Exception ");
			} finally {
				try {
					is.close();
				} catch (NullPointerException e) {
					// Failed to open a stream
					throw new IOException();
				}
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	/**
	 * Wrapper for stringToFile writes a string to a file making all newline
	 * codes platform specific
	 * 
	 * @param source
	 * @param FileName
	 * @return true on success
	 */
	public static boolean stringToFile(String source, String FileName) {

		if (source != null) {
			// Save the string to file (*.yml)
			try {
				return stringToFile(source, new File(FileName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return false;

	}
	
	public static File CheckYMLExists(File file) {

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	/**
	 * Writes the contents of a string to a file.
	 * 
	 * @param source
	 *            String to write.
	 * @param file
	 *            File to write to.
	 * @return True on success.
	 * @throws IOException
	 */
	public static boolean stringToFile(String source, File file) throws IOException {

		try {

			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");

			source.replaceAll("\n", System.getProperty("line.separator"));

			out.write(source);
			out.close();
			return true;

		} catch (IOException e) {
			System.out.println("Exception ");
			return false;
		}
	}

	public static String fileSeparator() {
		return System.getProperty("file.separator");
	}
	
	/**
	 * Pass a file and it will return it's contents as a string.
	 * 
	 * @param file File to read.
	 * @return Contents of file. String will be empty in case of any errors.
	 */
	public static String convertFileToString(File file) {

		if (file != null && file.exists() && file.canRead() && !file.isDirectory()) {
			Writer writer = new StringWriter();
			InputStream is = null;

			char[] buffer = new char[1024];
			try {
				is = new FileInputStream(file);
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);					
				}
				reader.close();
			} catch (IOException e) {
				System.out.println("Exception ");
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException ignore) {
					}
				}
			}
			return writer.toString();
		} else {
			return "";
		}
	}
}