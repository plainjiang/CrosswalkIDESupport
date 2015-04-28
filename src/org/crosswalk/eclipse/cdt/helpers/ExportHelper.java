/*
 *  Copyright 2014 Intel Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.crosswalk.eclipse.cdt.helpers;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.crosswalk.eclipse.cdt.CdtConstants;
import org.crosswalk.eclipse.cdt.CdtPluginLog;
import org.crosswalk.eclipse.cdt.export.DebPackageParameters;
import org.crosswalk.eclipse.cdt.newProject.NewProjectWizardState;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.json.JSONObject;
import org.json.JSONTokener;



public final class ExportHelper {
	private static IProgressMonitor pMonitor;
	NewProjectWizardState nProjectWizardState;
	
	
	public static  int doExport(IProject project, String targetFormat,
			File destination, Object formatParameters, IProgressMonitor monitor) throws IOException {
		int runResult = 0;
		pMonitor = monitor;
		if (targetFormat == "DEB") {
			runResult = generateDebPackage(project,
					(DebPackageParameters) formatParameters);
		} else if (targetFormat == "XPK") {// TODO
			
		} else {
			CdtPluginLog.logError("Unsupported exporting format:"
					+ targetFormat, null);
			return 1;
		}
		return runResult;
	}
	
	

	static String[] mapToStringArray(Map<String, String> map) {
		final String[] strings = new String[map.size()];
		int i = 0;
		for (Map.Entry<String, String> e : map.entrySet()) {
			strings[i] = e.getKey() + '=' + e.getValue();
			i++;
		}
		return strings;
	}

	private static  void exportStream(InputStream in, PrintStream print)
			throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			int c;
			while ((c = in.read()) != -1) {
				baos.write(c);
			}
			print.println(new String(baos.toByteArray()));
		} finally {
			baos.close();
		}
	}

	
	public static  int generateDebPackage(IProject project,DebPackageParameters packageparameters) throws IOException{
		int runResult = 0;
		StringBuilder cmd = new StringBuilder();
		
		final Map<String, String> env = new HashMap<String, String>(
				System.getenv());	
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		
		Path sourceManifestFile = FileSystems.getDefault().getPath(project.getLocation().toString(), "manifest.json");	//copy the manifest.json file

		String manifestLocation = project.getLocation().toString() + File.separator + "manifest.json";
		JSONObject manifest = new JSONObject(new JSONTokener(				//get the manifest file 
				new FileReader(manifestLocation)));
		
		String packageName = CdtConstants.CROSSWALK_PACKAGE_PREFIX + manifest.get("name");
		

			ProjectHelper projectHelper = new ProjectHelper();
			projectHelper.resourceHandler(root.getLocation().toString());
			String sourceFolder = project.getLocation().toString();
			String targetFolder = sourceFolder + File.separator + ".tmp" + File.separator + packageName + File.separator + "app";
			Path targetManifestFile = FileSystems.getDefault().getPath(targetFolder , "manifest.json");
			Files.copy(sourceManifestFile, targetManifestFile, REPLACE_EXISTING);
			//copy the icon file
			if(NewProjectWizardState.useDefaultIcon){		//do nothing,since we copied the icon from org.crosswalk.appName folder
			}
			else{			//user-specified icon.We have to delete the default icons and copy the user-specified icon
				String iconName = manifest.getJSONArray("icons").getJSONObject(2).get("src").toString();	
				Path sourceIconFile = FileSystems.getDefault().getPath(sourceFolder, iconName);
				Path targetIconFile = FileSystems.getDefault().getPath(targetFolder, iconName);
				Path defaultIconFile1 = FileSystems.getDefault().getPath(targetFolder, "icon-48.png");
				Path defaultIconFile2 = FileSystems.getDefault().getPath(targetFolder, "icon.png");
				Files.copy(sourceIconFile, targetIconFile,REPLACE_EXISTING);
				Files.delete(defaultIconFile1);
				Files.delete(defaultIconFile2);
			}
			
			
			
			//copy the launch file.
			//We must copy this file no matter whether the name is index.html since user may change it after setting it in manifestSettingPage
			if(NewProjectWizardState.isPackagedProject){
				String startUrl = manifest.get("start_url").toString();
				Path sourceStartUrlFile = FileSystems.getDefault().getPath(sourceFolder, startUrl);
				Path targetStartUrlFile = FileSystems.getDefault().getPath(targetFolder, startUrl);
				Files.copy(sourceStartUrlFile, targetStartUrlFile,REPLACE_EXISTING);
			}
		

		
		
		File buildDir = new File(project.getLocation().toString() + File.separator+".tmp" + File.separator + packageName);
		
		cmd.append("crosswalk-app build");	
		pMonitor.worked(1);
		Process process = Runtime.getRuntime().exec(cmd.toString(),
					mapToStringArray(env), buildDir);//execute cmd in specific targetFolder
		
		try {
			process.waitFor();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//copy the deb package to user-specified path
		String debPackageName = packageparameters.appName + "_" + packageparameters.appVersion + "-1_" + packageparameters.supportedArch + ".deb";
		Path source = FileSystems.getDefault().getPath(buildDir.toString() ,"pkg", debPackageName);
		Path target = FileSystems.getDefault().getPath(packageparameters.targetFolder,debPackageName);
		Files.copy(source, target, REPLACE_EXISTING);

		
		
		
			try {
				for (int i=0; i<20; i++) {
					pMonitor.worked(1);
					Thread.sleep(10);
				}
			}
			catch (InterruptedException e) {
			}
				// redirect the error and input stream
				try {
					exportStream(process.getErrorStream(), System.err);
					exportStream(process.getInputStream(), System.out);
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					runResult = process.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
					CdtPluginLog.logError("Error when involking package tool.", e);
				}

				return runResult;
		
	}
	
	
		

}
