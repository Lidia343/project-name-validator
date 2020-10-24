package project.name.validator;

import java.util.Objects;

import org.eclipse.core.resources.IResource;

/**
 * Класс, позволяющий проверить, открыт ли проект ресурса
 * IResource или нет.
 */
public class ProjectValidator
{
	private IResource m_resource;
	
	/**
	 * Конструктор класса ProjectValidator.
	 * @param a_resource
	 * 		  Ресурс, содержащий проект для проверки
	 */
	public ProjectValidator (IResource a_resource)
	{
		m_resource = Objects.requireNonNull(a_resource);
	}
	
	/**
	 * @return true - если проект переданного в конструктор
	 * ресурса открыт, false - иначе
	 */
	public boolean isProjectOpen ()
	{
		return (m_resource != null && m_resource.exists() && m_resource.getProject() != null &&
				m_resource.getProject().isOpen()) ? true : false;
	}
}
