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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import lombok.Getter;
import lombok.extern.java.Log;

@Log
public class Craftboot {
	
	@Getter private static File dataDir;
	
	static final String LAUNCHER_CLASS_NAME = "com.skcraft.launcher.Launcher";
	static final String LAUNCHER_SUBDIR = ".craftboot";
	static final String URL_DIALOG_TEXT = "Welcome to CraftBoot! Please enter a launcher configuration URL."
			+ "\nOnly enter URLs from sources you trust, as malicious URLs could be used for phishing."
			+ "\n(if you already completed this previously, make sure that you didn't rename your launcher)";
	
	/**
	 * Does most of the everything
	 * 
	 * @param args arguments (not used)
	 */
	public static void main(String[] args) {
		setLookAndFeel();
		dataDir = makeDataDir();
		File launcherDir = new File(dataDir, "launcher");
		launcherDir.mkdir();
		
		File[] launcherPacks = launcherDir.listFiles();
		if (launcherPacks == null || launcherPacks.length == 0) {
			boolean didDownload = new LauncherDownloader().downloadLauncher();
			if (!didDownload) {
				log.severe("Failed to download launcher! Shutting down...");
				System.exit(1);
			}
			launcherPacks = launcherDir.listFiles();
		}
		ArrayList<LauncherJar> launcherPackFiles = new ArrayList<LauncherJar>(launcherPacks.length);
		
		for (int i = 0; i < launcherPacks.length; i++) {
			File f = launcherPacks[i];
			if (f.isFile()) {
				LauncherJar pf = LauncherJar.makePackFile(f);
				if (pf == null) {
					log.warning("Skipped incorrectly formatted launcher pack file: " + launcherPacks[i].getName());
				} else {
					launcherPackFiles.add(pf);
				}
			}
		}
		launcherPackFiles.trimToSize();
		
		// determine which LauncherJar is newest
		LauncherJar newestPackFile = null;
		for (LauncherJar pf : launcherPackFiles) {
			if (newestPackFile == null || pf.getFileName() > newestPackFile.getFileName()) {
				newestPackFile = pf;
			}
		}
		
		if (newestPackFile == null) {
			log.severe("No valid jar files! Shutting down...");
			System.exit(1);
		}
		
		if (newestPackFile.isPacked()) {
			boolean didUnpack = newestPackFile.unpackJar();
			if (!didUnpack) {
				log.severe("Failed to unpack jar file! Shutting down...");
				System.exit(1);
			}
		}
		
		prepareUserUrl();
		
		try {
			runLauncherJar(newestPackFile);
		} catch (Exception e) {
			log.severe("Failed to run launcher jar!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Attempts to run a given LauncherJar
	 * 
	 * @param jar the LauncherJar to run (must not be packed!)
	 * @throws ClassNotFoundException if the launcher jar lacks the right class
	 * @throws SecurityException see Class.getConstructor()
	 * @throws NoSuchMethodException see Class.getConstructor()
	 * @throws InvocationTargetException see Constructor.newInstance()
	 * @throws IllegalArgumentException  see Constructor.newInstance()
	 * @throws IllegalAccessException  see Constructor.newInstance()
	 * @throws InstantiationException  see Constructor.newInstance()
	 */
	public static void runLauncherJar(LauncherJar jar) throws ClassNotFoundException,
	NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
	IllegalArgumentException, InvocationTargetException {
		
		URL[] jarUrls = new URL[1];
		try {
			jarUrls[0] = jar.getFile().toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			log.severe("Could not convert File to URL! Shutting down...");
			System.exit(1);
		}
		
		ClassLoader loader = URLClassLoader.newInstance(jarUrls);
		Class<?> launcherClass = loader.loadClass(LAUNCHER_CLASS_NAME);
		Method launcherMethod = launcherClass.getDeclaredMethod("main", Class.forName("[Ljava.lang.String;"));
		
		String[] args = {
				"--dir", dataDir.getAbsolutePath(),
				"--bootstrap-version", "1"
				};
		
		launcherMethod.invoke(null, new Object[]{ args });
	}
	
	/**
	 * Asks the user for the URL to their launcher properties file
	 * or uses the pre-existing properties file
	 */
	public static void prepareUserUrl() {
		File launcherProperties = new File(dataDir, "launcher.properties");
		File craftbootUrl = new File(dataDir, ".craftbooturl");
		String propertiesUrl = "";

    // If there is a .craftbooturl file in the jar, copy it out to the data directory first
    URL resourceUrl = Craftboot.class.getClassLoader().getResource(".craftbooturl");
    if (resourceUrl != null) {
      InputStream resInStream = Craftboot.class.getClassLoader().getResourceAsStream(".craftbooturl");
      try {
        OutputStream resOutStream = new FileOutputStream(craftbootUrl);
        int readBytes;
        byte[] buffer = new byte[4096];
        while ((readBytes = resInStream.read(buffer)) > 0) {
          resOutStream.write(buffer, 0, readBytes);
        }
        resOutStream.close();
      } catch (Exception e) {
        log.warning("Unable to copy .craftbooturl from jar file");
      }
    }
		
		// get the properties URL from the file if it exists
		if (craftbootUrl.exists()) {
			propertiesUrl = CraftbootUtils.getTextFromFile(craftbootUrl);
			if (propertiesUrl == null) {
				log.warning("Could not read URL from file, will prompt user to re-enter URL");
			}
		}
		
		// if the launcher.properties file doesn't or properties URL file didn't exist, prompt
		// the user for a launcher.properties URL
		if (!launcherProperties.exists() || propertiesUrl == null) {
			propertiesUrl = (propertiesUrl == null) ? "" : propertiesUrl;
			while (propertiesUrl == "") {
				
				propertiesUrl = JOptionPane.showInputDialog(URL_DIALOG_TEXT);
			}
			if (propertiesUrl == null) {
				log.info("User canceled setup, shutting down...");
				System.exit(0);
				return;
			}
			
			// save the url to a file
			PrintStream out;
			try {
				out = new PrintStream(new FileOutputStream(craftbootUrl));
				out.print(propertiesUrl);
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		if (!CraftbootUtils.downloadToFile(propertiesUrl, launcherProperties)) {
			log.warning("Failed to download launcher.properties, default version will likely be used");
		}
		
		if (launcherProperties.exists()) {
			try {
				System.setProperty("com.skcraft.launcher.propertiesFile", launcherProperties.getCanonicalPath());
			} catch (IOException e) {
				log.warning("Failed to set system property for launcher.properties, default version will likely be used");
				System.setProperty("com.skcraft.launcher.propertiesFile", null);
				e.printStackTrace();
			}
		} else {
			System.setProperty("com.skcraft.launcher.propertiesFile", null);
		}
	}
	
	
	/**
	 * Creates a reference to the directory where the launcher will be stored
	 * 
	 * Directory should be <user home folder>/<LAUNCHER_SUBDIR>/<name of craftboot jar file>
	 */
	public static File makeDataDir() {
		File homeDir = new File(CraftbootUtils.getUserHome());
		File craftbootDir = new File(homeDir, LAUNCHER_SUBDIR);
		String launcherFilename = new File(Craftboot.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getName();
		File instanceDir = new File(craftbootDir, launcherFilename);
		instanceDir.mkdirs();
		return instanceDir;
	}
	
	/**
	 * Sets the javax.swing L&F to the system's native style
	 */
	public static void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
