package project.name.validator.property;

import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;

public class RenameIgnoringProperty
{
	private final String m_renameIgnoringKey = "RENAME_IGNORING_KEY";
			
	private IProject m_project;
	
	private QualifiedName m_qualifiedName;
	
	public RenameIgnoringProperty (IProject a_project)
	{
		m_project = Objects.requireNonNull(a_project);
		createQualifiedName(m_project);
	}
	
	private void createQualifiedName (IProject a_project)
	{
		IPath location = m_project.getLocation();
		String folderName = m_project.getName();
		if (location != null) folderName = location.lastSegment();
		m_qualifiedName = new QualifiedName(folderName, m_renameIgnoringKey);
	}
	
	public boolean exists () throws CoreException
	{
		String property = m_project.getPersistentProperty(m_qualifiedName);
		return (property != null) ? true : false;
	}
	
	public boolean getValue () throws CoreException
	{
		String property = m_project.getPersistentProperty(m_qualifiedName);
		return (property == null) ? false : Boolean.parseBoolean(property);
	}
	
	public void setValue (boolean a_ignoring) throws CoreException
	{
		m_project.setPersistentProperty(m_qualifiedName, Boolean.toString(a_ignoring));
	}
}
