package io.github.oxguy3.craftboot;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class CraftbootUtils {

	public static final String PACKED_EXT = ".jar.pack";
	public static final String UNPACKED_EXT = ".jar";
	
	/**
	 * Downloads text from a given URL and returns it as a String
	 * 
	 * @return the text (or null if download failed)
	 */
	public static String downloadTextFromUrl(String url) {
		InputStream in;
		try {
			in = new URL(url).openStream();
			Scanner scan = new Scanner(in);
			return scan.hasNext() ? scan.next() : null;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Downloads from a given URL to a given File
	 * 
	 * @param url the URL as a String
	 * @param file the File to download to
	 * @return true if successful
	 */
	public static boolean downloadToFile(String url, File file) {
		BufferedInputStream in = null;
		FileOutputStream fout = null;
	    try {
	        in = new BufferedInputStream(new URL(url).openStream());
	        fout = new FileOutputStream(file);

	        final byte data[] = new byte[1024];
	        int count;
	        while ((count = in.read(data, 0, 1024)) != -1) {
	            fout.write(data, 0, count);
	        }
	    } catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
	        try {
				if (in != null) {
		            in.close();
		        }
		        if (fout != null) {
		            fout.close();
		        }
	        } catch (IOException e) {
				e.printStackTrace();
				return false;
	        }
	    }
	    return true;
	}
}
