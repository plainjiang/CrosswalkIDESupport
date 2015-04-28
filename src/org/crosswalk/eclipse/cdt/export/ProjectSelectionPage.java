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

package org.crosswalk.eclipse.cdt.export;

import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.Bundle;
import org.crosswalk.eclipse.cdt.helpers.ProjectHelper;


public class ProjectSelectionPage extends WizardPage implements SelectionListener{
	private Combo exportedProjectCombo;
	public IProject exportProject;
	private static IProject[] crosswalkProjects;

	public ProjectSelectionPage(String pageName) {
		super(pageName);
		Bundle bundle = Platform.getBundle("org.crosswalk.eclipse.cdt");
		Path path = new Path("images/icon-68.png");
		URL imageUrl = FileLocator.find(bundle, path, null);
		setImageDescriptor(ImageDescriptor.createFromURL(imageUrl));
		setTitle("Export Crosswalk Project");
		setDescription("Select a Crosswalk Project to export.");
		crosswalkProjects = ProjectHelper.getCrosswalkProjects();
		if (crosswalkProjects.length == 0) {
			setErrorMessage("No project available to export.");
			setPageComplete(false);
		}
		else
			exportProject = crosswalkProjects[0];
	}

	@Override
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NULL);
		setControl(mainComposite);
		GridLayout layout = new GridLayout(4, false);
		layout.horizontalSpacing = 10;
		mainComposite.setLayout(layout);
		
		Label exportedProjectLabel = new Label(mainComposite, SWT.NONE);
		exportedProjectLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				false, false, 1, 1));
		exportedProjectLabel.setText("Project to export:");

		exportedProjectCombo = new Combo(mainComposite, SWT.READ_ONLY);
		GridData gdExportedProjectCombo = new GridData(SWT.FILL, SWT.CENTER,
				true, false, 3, 1);
		exportedProjectCombo.setLayoutData(gdExportedProjectCombo);
		exportedProjectCombo.setItems(getProjectNames());
		exportedProjectCombo.setText(exportProject.getName());
		exportedProjectCombo.addSelectionListener(this);
	}

	private String[] getProjectNames() {
		// get name list of crosswalkProjects Array
		String[] projectNames = new String[crosswalkProjects.length];
		for (int i = 0; i < crosswalkProjects.length; i++) {
			projectNames[i] = crosswalkProjects[i].getName();
		}
		return projectNames;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		int index = 0;
		Object source = e.getSource();
		if (source == exportedProjectCombo) {
			index = exportedProjectCombo.getSelectionIndex();
			projectChanged(crosswalkProjects[index]);

		}
	}

	private void projectChanged(IProject p) {
		exportProject = p;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
}
