package project.name.validator.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import project.name.validator.ChangedNameValidator;
import project.name.validator.log.ErrorStatusHandler;
import project.name.validator.property.RenameIgnoringProperty;

/**
 * Страница свойства RenameIgnoringProperty для проекта
 * в рабочем пространстве.
 */
public class RenameIgnoringPropertyPage extends PropertyPage implements IWorkbenchPropertyPage
{
	public static final String RENAME_IGNORING_MESSAGE = "Игнорировать приводящее к несовпадению имени проекта и " +
														 "имени его папки в файловой системе переименование проекта " +
														 "(не рекомендуется)";
	
	private Button m_renameIgnoringButton;
	
	private RenameIgnoringProperty m_property;
	
	@Override
	protected Control createContents(Composite a_parent)
	{
		m_property = new RenameIgnoringProperty((IProject)getElement());
		m_renameIgnoringButton = new Button(a_parent, SWT.CHECK);
		m_renameIgnoringButton.setText(RENAME_IGNORING_MESSAGE);
		try
		{
			m_renameIgnoringButton.setSelection(m_property.getValue());
		}
		catch (CoreException e)
		{
			ErrorStatusHandler.log(e, e.getMessage());
		}	
		return a_parent;
	}
	
	@Override
	public void performDefaults ()
	{
		m_renameIgnoringButton.setSelection(false);
		try
		{
			m_property.setValue(false);
			validateProjectName();
		}
		catch (CoreException e)
		{
			ErrorStatusHandler.log(e, e.getMessage());
		}
	}
	
	@Override
	public boolean performOk ()
	{
		try
		{
			m_property.setValue(m_renameIgnoringButton.getSelection());
			validateProjectName();
		}
		catch (CoreException e)
		{
			ErrorStatusHandler.log(e, e.getMessage());
		}
		return true;
	}
	
	/**
	 * Проверяет имя проекта на идентичность имени
	 * его папки в файловой системе.
	 */
	private void validateProjectName ()
	{
		ChangedNameValidator validator = new ChangedNameValidator();
		IProject project = (IProject)getElement();
		validator.validateProjectName(project, false);
	}
}
