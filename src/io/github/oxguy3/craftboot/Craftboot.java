package io.github.oxguy3.craftboot;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import lombok.Getter;
import lombok.extern.java.Log;

@Log
public class Craftboot {
	
	@Getter private static File dataDir;
	
	public static void main(String[] args) {
		dataDir = makeDataDir();
		File launcherDir = new File(dataDir, "launcher");
		
		if (!launcherDir.exists()) {
			launcherDir.mkdir();
		}
		
		File[] launcherPacks = launcherDir.listFiles();
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
		
		URL[] launcherUrls = new URL[1];
		try {
			launcherUrls[0] = newestPackFile.getFile().toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			log.severe("Could not convert File to URL! Shutting down...");
			System.exit(1);
		}
		
	}
	
	/**
	 * Creates a reference to the directory where the launcher will be stored
	 */
	public static File makeDataDir() {
		//TODO use a better directory based on OS version
		File file = new File(System.getProperty("user.dir"));
		// since we're using working directory, we know it exists
		// but once we're not using working directory, be sure to mkdir if needed!
		return file;
		/*String os = System.getProperty("os.name");
		if (os.contains("Windows")) {
			dataDir = new File();
		}*/
	}
}
