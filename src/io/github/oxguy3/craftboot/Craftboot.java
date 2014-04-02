package io.github.oxguy3.craftboot;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import lombok.Getter;
import lombok.extern.java.Log;

@Log
public class Craftboot {
	
	@Getter private static File dataDir;
	
	public static final String LAUNCHER_CLASS_NAME = "com.skcraft.launcher.Launcher";
	
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
		
		try {
			runLauncherJar(newestPackFile);
		} catch (Exception e) {
			log.severe("Failed to run launcher jar!");
			e.printStackTrace();
		}
		
		
		//com.skcraft.launcher.Launcher
		
	}
	
	/**
	 * Attempts to run a given LauncherJar
	 * 
	 * @param jar the LauncherJar to run
	 * @throws ClassNotFoundException if the launcher jar lacks the right class
	 * @throws SecurityException see Class.getConstructor()
	 * @throws NoSuchMethodException see Class.getConstructor()
	 * @throws InvocationTargetException see Constructor.newInstance()
	 * @throws IllegalArgumentException  see Constructor.newInstance()
	 * @throws IllegalAccessException  see Constructor.newInstance()
	 * @throws InstantiationException  see Constructor.newInstance()
	 */
	public static void runLauncherJar(LauncherJar jar) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
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
		log.info(args.getClass().getName());
		
		launcherMethod.invoke(null, new Object[]{ args });
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
