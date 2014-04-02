package io.github.oxguy3.craftboot;

import java.io.File;

public class LauncherDownloader {
	
	public LauncherDownloader () {}
	
	/**
	 * Returns the URL where the URL of the launcher download can be found
	 * 
	 * @return the URL
	 */
	private String getLauncherUpdateUrl() {
		//TODO should use own launcher (or prompt user for URL)
		return "http://oxguy3.github.io/craftboot-files/launcher/latest";
	}
	
	/**
	 * Downloads the launcher to the appropriate directory
	 * 
	 * @return true if successful
	 */
	public boolean downloadLauncher() {
		String downloadUrl = CraftbootUtils.downloadTextFromUrl(getLauncherUpdateUrl());
		if (downloadUrl == null) {
			return false;
		}
		
		File launcherFolder = new File(Craftboot.getDataDir(), "launcher");
		File launcherJar = new File(launcherFolder, Long.toString(System.currentTimeMillis()) + CraftbootUtils.PACKED_EXT);
		
		return CraftbootUtils.downloadToFile(downloadUrl, launcherJar);
	}
}
