/**
 * CraftBoot, a bootstrapper for SKCraft Launcher
 * Copyright (C) 2014 Hayden Schiff <http://oxguy3.github.io> and contributors
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.oxguy3.craftboot;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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
	 * Downloads text from a given URL and returns it as a String
	 * 
	 * @return the text (or null if download failed)
	 */
	public static String getTextFromFile(File file) {
		InputStream in;
		try {
			in = new FileInputStream(file);
			Scanner scan = new Scanner(in);
			return scan.hasNext() ? scan.next() : null;
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
	
	/**
	 * Gets the user's home directory in their OS
	 * 
	 * Usually this will be equivalent to the system property user.home, but
	 * because Windows is such a special snowflake, it will return the
	 * USERPROFILE environment variable on Windows systems.
	 */
	public static String getUserHome() {
		
		boolean isWindows = System.getProperty("os.name").startsWith("Windows");
		
		if (isWindows) {
			return System.getenv("USERPROFILE");
		} else {
			return System.getProperty("user.home");
		}
	}
}
