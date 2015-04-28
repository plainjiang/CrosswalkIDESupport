package org.crosswalk.eclipse.cdt.newProject;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;

public class NewHostedPage extends WizardPage implements ModifyListener,SelectionListener,FocusListener{

	private static final int FIELD_WIDTH = 300;
	static final int WIZARD_PAGE_WIDTH = 600;
	private final NewProjectWizardState nHostedWizardState;
	private Text projectText;
	private Text applicationText;
	private boolean mIgnore;
	private ControlDecoration applicationDec;
	private ControlDecoration projectDec;
	private Label helpNote;
	private Label tipLabel;
	private Boolean appNameCanFinish;
	private Boolean projectNameCanFinish;
	
	 NewHostedPage(NewProjectWizardState values) {
		 super("newHostedCrosswalkApp");
		 	NewProjectWizardState.isPackagedProject = false;
			nHostedWizardState = values;
			setTitle("New Hosted Crosswalk Application");
			setDescription("Creates a new Hosted Crosswalk Application");
			Bundle bundle = Platform.getBundle("org.crosswalk.eclipse.cdt");
			Path path = new Path("images/icon-68.png");
			URL imageUrl = FileLocator.find(bundle, path, null);
			setImageDescriptor(ImageDescriptor.createFromURL(imageUrl));
			setPageComplete(false);
			appNameCanFinish = false;
			projectNameCanFinish = false;
			
	}


public void createControl(Composite parent) {
	Composite container = new Composite(parent, SWT.NULL);
	setControl(container);
	GridLayout gl_container = new GridLayout(4, false);
	gl_container.horizontalSpacing = 10;
	container.setLayout(gl_container);

	Label applicationLabel = new Label(container, SWT.NONE);
	applicationLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
			false, false, 2, 1));
	applicationLabel.setText("Application Name:");

	applicationText = new Text(container, SWT.BORDER);
	GridData gdApplicationText = new GridData(SWT.FILL, SWT.CENTER, true,
			false, 2, 1);
	gdApplicationText.widthHint = FIELD_WIDTH;
	applicationText.setLayoutData(gdApplicationText);
	applicationText.addModifyListener(this);
	applicationText.addFocusListener(this);
	applicationDec = createFieldDecoration(applicationText,
			"The application name must be consist of lower case letters only,and 2 letters at least.");

	
	Label projectLabel = new Label(container, SWT.NONE);
	projectLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
			false, 2, 1));
	projectLabel.setText("Project Name:");
	projectText = new Text(container, SWT.BORDER);
	GridData gdProjectText = new GridData(SWT.FILL, SWT.CENTER, true,
			false, 2, 1);
	gdProjectText.widthHint = FIELD_WIDTH;
	projectText.setLayoutData(gdProjectText);
	projectText.addModifyListener(this);
	projectText.addFocusListener(this);	
	projectDec = createFieldDecoration(
			projectText,
			"The project name is only used by Eclipse, but must be unique within the "
					+ "workspace. This can typically be the same as the application name.");

	new Label(container, SWT.NONE);
	new Label(container, SWT.NONE);
	new Label(container, SWT.NONE);
	new Label(container, SWT.NONE);

	Label label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
	label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 4, 1));

	helpNote = new Label(container, SWT.NONE);
	helpNote.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false,
			1, 1));
	helpNote.setText("Note:");
	helpNote.setVisible(false);

	tipLabel = new Label(container, SWT.WRAP);
	tipLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2,
			1));

	// Reserve space for 4 lines
	tipLabel.setText("\n\n\n\n"); //$NON-NLS-1$

	// Reserve enough width to accommodate the various wizard pages up front (since they are
	// created lazily, and we don't want the wizard to dynamically resize itself for small
	// size adjustments as each successive page is slightly larger)
	
	Label dummy = new Label(container, SWT.NONE);
	GridData data = new GridData();
	data.horizontalSpan = 4;
	data.widthHint = WIZARD_PAGE_WIDTH;
	dummy.setLayoutData(data);
}

private ControlDecoration createFieldDecoration(Control control,
		String description) {
	ControlDecoration dec = new ControlDecoration(control, SWT.LEFT);
	dec.setMarginWidth(2);
	FieldDecoration errorFieldIndicator = FieldDecorationRegistry
			.getDefault().getFieldDecoration(
					FieldDecorationRegistry.DEC_INFORMATION);
	dec.setImage(errorFieldIndicator.getImage());
	dec.setDescriptionText(description);
	control.setToolTipText(description);
	return dec;
}

// ---- Implements ModifyListener ----




@Override
public void modifyText(ModifyEvent e) {
	if (projectText.getText().length() == 0) {
		projectNameCanFinish = false;
	}
	else {
		projectNameCanFinish = true;
	}

	
	if ( !isAppNameValid(applicationText.getText())) {
		appNameCanFinish = false;
		setMessage("Application name must  contain 2 characters at least,and make sure they are lowercase letters", WARNING);	
	}
	else {
		setMessage("");
		appNameCanFinish = true;
	}
	if (projectNameCanFinish && appNameCanFinish)
		setPageComplete(true);
	else
		setPageComplete(false);
	if (mIgnore) {
		return;
	}
	Object source = e.getSource();
	if (source == projectText) {
		nHostedWizardState.projectName = projectText.getText();
		updateProjectLocation(nHostedWizardState.projectName);
		nHostedWizardState.projectModified = true;

		try {
			mIgnore = true;
			if (!nHostedWizardState.applicationModified) {
				nHostedWizardState.applicationName = nHostedWizardState.projectName;
				applicationText.setText(nHostedWizardState.projectName);
			}
			// update nHostedWizardState.projectName;
		} finally {
			mIgnore = false;
		}
	} else if (source == applicationText) {
		nHostedWizardState.applicationName = applicationText.getText();
		nHostedWizardState.applicationModified = true;

		try {
			mIgnore = true;
			if (!nHostedWizardState.projectModified) {
				nHostedWizardState.projectName = appNameToProjectName(nHostedWizardState.applicationName);
				projectText.setText(nHostedWizardState.projectName);
				updateProjectLocation(nHostedWizardState.projectName);
			}
			// updateActivityNames(nHostedWizardState.applicationName);
		} finally {
			mIgnore = false;
		}
	}
}




private String appNameToProjectName(String appName) {
	// Strip out whitespace (and capitalize subsequent words where spaces
	// were removed
	boolean upcaseNext = false;
	StringBuilder sb = new StringBuilder(appName.length());
	for (int i = 0, n = appName.length(); i < n; i++) {
		char c = appName.charAt(i);
		if (c == ' ') {
			upcaseNext = true;
		} else if (upcaseNext) {
			sb.append(Character.toUpperCase(c));
			upcaseNext = false;
		} else {
			sb.append(c);
		}
	}

	appName = sb.toString().trim();
	return appName;
}

/**
 * The project should be created in the workspace, then update the
 * project location based on the project name.
 */
private void updateProjectLocation(String projectName) {
	if (projectName == null) {
		projectName = "";
	}
	
}

// ---- Implements SelectionListener ----

@Override
public void widgetSelected(SelectionEvent e) {

}


@Override
public void widgetDefaultSelected(SelectionEvent e) {
	// TODO:
}

// ---- Implements FocusListener ----

@Override
public void focusGained(FocusEvent e) {
	// handler for focus gained
	Object source = e.getSource();
	String tip = "";
	if (source == applicationText) {
		tip = applicationDec.getDescriptionText();
	} else if (source == projectText) {
		tip = projectDec.getDescriptionText();
	}

	tipLabel.setText(tip);
	helpNote.setVisible(tip.length() > 0);
}

@Override
public void focusLost(FocusEvent e) {
	tipLabel.setText("");
	helpNote.setVisible(false);
}


public boolean isAppNameValid(String inputString)
{	
	Pattern pattern = Pattern.compile("[a-z]{0,}");
	Matcher matcher = pattern.matcher(inputString);
	int leastNameLength = 2;
	if(inputString.length() < leastNameLength || (!matcher.matches()))
		return false;
	else 
		return true;
}

public IWizardPage getNextPage()
{    		
	
	HostedManifestSettingPage page = ((NewHostedWizard)getWizard()).hostedManifestSettingPage;
	return page;
}


@Override
public boolean canFlipToNextPage() {
	if(isAppNameValid(applicationText.getText())){
		return true;
	}
	else{
		return false;
	}
	
}

}
