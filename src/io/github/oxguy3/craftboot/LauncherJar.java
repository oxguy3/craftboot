package io.github.oxguy3.craftboot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

import lombok.Getter;

/**
 * Represents a .jar file containing a version of the launcher (may also
 * represent a .jar.pack file)
 */
public class LauncherJar {
	
	// the file referencing the jar's location
	@Getter private File file;
	
	// whether or not the jar is packed
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
			name = name.substring(0, name.length() - CraftbootUtils.PACKED_EXT.length());
		} else {
			name = name.substring(0, name.length() - CraftbootUtils.UNPACKED_EXT.length());
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
		newFileName = newFileName.substring(0, newFileName.length() - CraftbootUtils.PACKED_EXT.length())
				+ CraftbootUtils.UNPACKED_EXT;
		File unpackedFile = new File(file.getParentFile(), newFileName);

		FileInputStream fis;
		BufferedInputStream bis;
		FileOutputStream fos;
		BufferedOutputStream bos;
		JarOutputStream jos;
		
		try {
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);
			
			fos = new FileOutputStream(unpackedFile);
			bos = new BufferedOutputStream(fos);
			jos = new JarOutputStream(bos);
			
			unpacker.unpack(bis, jos);

			jos.close();
			bos.close();
			fos.close();
			bis.close();
			fis.close();
			
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
		
		// check that the file extension is correct, then remove it
		if (name.endsWith(CraftbootUtils.PACKED_EXT)) {
			packed = true;
			name = name.substring(0, name.length() - CraftbootUtils.PACKED_EXT.length());
			
		} else if (name.endsWith(CraftbootUtils.UNPACKED_EXT)) {
			packed = false;
			name = name.substring(0, name.length() - CraftbootUtils.UNPACKED_EXT.length());
			
		} else {
			return null;
		}
		
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
