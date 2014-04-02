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

public class LauncherDownloader {
	
	public LauncherDownloader () {}
	
	/**
	 * Returns the URL where the URL of the launcher download can be found
	 * 
	 * @return the URL
	 */
	private String getLauncherUpdateUrl() {
		//TODO should use own launcher (or prompt user for URL)
		return "http://update.skcraft.com/quark/launcher/latest";
	}
	
	/**
	 * Downloads the launcher to the appropriate directory
	 * 
	 * @return true if successful
	 */
	public boolean downloadLauncher() {
		String downloadUrl = downloadTextFromUrl(getLauncherUpdateUrl());
		if (downloadUrl == null) {
			return false;
		}
		
		File launcherFolder = new File(Craftboot.getDataDir(), "launcher");
		File launcherJar = new File(launcherFolder, Long.toString(System.currentTimeMillis()) + Craftboot.PACKED_EXT);
		
		BufferedInputStream in = null;
		FileOutputStream fout = null;
	    try {
	        in = new BufferedInputStream(new URL(downloadUrl).openStream());
	        fout = new FileOutputStream(launcherJar);

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
}
