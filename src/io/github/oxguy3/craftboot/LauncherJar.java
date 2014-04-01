package io.github.oxguy3.craftboot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

import lombok.Getter;
import lombok.Setter;

public class LauncherJar {

	final static String PACKED_EXT = ".jar.pack";
	final static String UNPACKED_EXT = ".jar";
	
	@Getter private File file;
	@Getter private boolean isPacked;
	
	private LauncherJar (File f, boolean ip) {
		file = f;
		isPacked = ip;
	}
	
	/**
	 * Gets the timestamp represented in this file's name
	 * 
	 * @return the timestamp
	 */
	public long getFileName() {
		String name = file.getName();
		if (this.isPacked()) {
			name = name.substring(0, name.length() - PACKED_EXT.length());
		} else {
			name = name.substring(0, name.length() - UNPACKED_EXT.length());
		}
		return Long.parseLong(name);
	}
	
	/**
	 * Unpacks the jar if necessary
	 * 
	 * @return true if successful (false if jar was already unpacked)
	 */
	public boolean unpackJar() {
		if (!isPacked()) {
			return false;
		}
		
		Pack200.Unpacker unpacker = Pack200.newUnpacker();
		String newFileName = file.getName();
		newFileName = newFileName.substring(0, newFileName.length() - PACKED_EXT.length())
				+ UNPACKED_EXT;
		File unpackedFile = new File(file.getParentFile(), newFileName);
		JarOutputStream jarOut;
		try {
			jarOut = new JarOutputStream(new FileOutputStream(unpackedFile));
			unpacker.unpack(file, jarOut);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		file = unpackedFile;
		return true;
	}
	
	/**
	 * Method used to create an instance of this class from a File object
	 * 
	 * @param f the File
	 * @return the created instance, or null if one could not be made
	 */
	public static LauncherJar makePackFile(File f) {
		String name = f.getName();
		
		boolean packed;
		
		// check that the file extension is correct
		if (name.endsWith(PACKED_EXT)) {
			packed = true;
		} else if (name.endsWith(UNPACKED_EXT)) {
			packed = false;
		} else {
			return null;
		}
		
		// remove the file extension
		name = name.substring(0, name.length() - PACKED_EXT.length());
		
		// check that the file name is a legitimate timestamp
		try {
			Long.parseLong(name);
		} catch (NumberFormatException nfe) {
			return null;
		}
		
		// if we haven't returned null by this point, return a PackFile object
		return new LauncherJar(f, packed);
	}
	
}
