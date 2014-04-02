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
