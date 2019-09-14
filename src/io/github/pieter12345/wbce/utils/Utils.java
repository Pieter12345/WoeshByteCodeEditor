package io.github.pieter12345.wbce.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * Utils class.
 * This class contains useful methods that do not belong elsewhere.
 * @author P.J.S. Kools
 */
public abstract class Utils {
	
//	/**
//	 * removeFile method.
//	 * Removes the given file or directory (including subdirectories).
//	 * @param file - The File to remove.
//	 * @return True if the removal was successful. False if one or more files could not be removed.
//	 *  If the file does not exist, true is returned.
//	 */
//	public static boolean removeFile(File file) {
//		boolean ret = true;
//		if(file.isDirectory()) {
//			File[] localFiles = file.listFiles();
//			if(localFiles == null) {
//				return false; // IOException occurred or the file was not a directory.
//			}
//			for(File localFile : localFiles) {
//				ret &= removeFile(localFile); // Recursive call.
//			}
//		}
//		return file.delete() && ret;
//	}
	
	/**
	 * getStacktrace method.
	 * @param throwable - The Throwable for which to create the stacktrace String.
	 * @return The stacktrace printed when "throwable.printStackTrace()" is called.
	 */
	public static String getStacktrace(Throwable throwable) {
		if(throwable == null) {
			throw new NullPointerException("Exception argument is null.");
		}
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		try {
			throwable.printStackTrace(new PrintStream(outStream, true, StandardCharsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(); // Never happens.
		}
		return new String(outStream.toByteArray(), StandardCharsets.UTF_8);
	}
	
//	/**
//	 * Converts a File to an URL. This method can be convenient as alterntive to catching a never-thrown Exception.
//	 * @param file - The file to convert.
//	 * @return The URL of the file, or null if the file was null.
//	 */
//	public static URL fileToURL(File file) {
//		try {
//			return (file == null ? null : file.toURI().toURL());
//		} catch (MalformedURLException e) {
//			throw new RuntimeException("Never happens.");
//		}
//	}
	
	/**
	 * Glues elements in an iterable together into a string with the given glue.
	 * @param iterable - The iterable containing the elements to generate a string with.
	 * @param stringifier - Used to convert object T into a string.
	 * @param glue - The glue used between elements in the iterable.
	 * @return The glued string(e1+glue+e2+glue+e3 etc) or an empty string if no elements were found.
	 */
	public static <T> String glueIterable(Iterable<T> iterable, Stringifier<T> stringifier, String glue) {
		Iterator<T> it = iterable.iterator();
		if(!it.hasNext()) {
			return "";
		}
		StringBuilder str = new StringBuilder(stringifier.toString(it.next()));
		while(it.hasNext()) {
			str.append(glue).append(stringifier.toString(it.next()));
		}
		return str.toString();
	}
	
	public static interface Stringifier<T> {
		String toString(T object);
	}
}
