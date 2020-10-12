package project.name.validator.ui;

import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import project.name.validator.property.RenameIgnoringProperty;

public class ProblemNameDialog extends Dialog
{
	private String m_warningMessage = "";
	
	private RenameIgnoringProperty m_property;
	
	public ProblemNameDialog (IProject a_project)
	{
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		m_property = new RenameIgnoringProperty(Objects.requireNonNull(a_project));
		setWarningMessage(a_project);
	}
	
	private void setWarningMessage (IProject a_project)
	{
		String projectName = "";
		IPath location = a_project.getLocation();
		if (location != null) projectName = location.lastSegment();
		m_warningMessage = "Не рекомендуется переименовывать данный проект, так как это приведёт к ошибке " +
						   "(несовпадению имени проекта и имени его папки). Добавить проект " + projectName +
						   " в исключения (данное действие или его отмену также можно совершить на странице " +
						   System.lineSeparator() + "\"Свойства\"->\"Переименование проекта\")?";
	}
	
	@Override
	public Control createDialogArea(Composite parent) 
	{
		Composite composite = new Composite ((Composite)super.createDialogArea(parent), SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Label messageLabel = new Label(composite, SWT.WRAP);
		messageLabel.setText(m_warningMessage);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL) ;
		gridData.widthHint = 500;
		messageLabel.setLayoutData(gridData);
		
		return composite;
	}
	
	@Override
	public void okPressed()
	{
		setRenameIgnoringProperty(true);
		super.okPressed();
	}
	
	@Override
	public void cancelPressed()
	{
		setRenameIgnoringProperty(false);
		super.cancelPressed();
	}
	
	private void setRenameIgnoringProperty (boolean a_property)
	{
		try
		{
			m_property.setValue(a_property);
		}
		catch (CoreException e)
		{
			
		}
	}
}
