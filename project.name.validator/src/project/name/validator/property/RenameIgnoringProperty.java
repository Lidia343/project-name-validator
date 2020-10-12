package project.name.validator.property;

import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;

/**
 * Свойство, определяющее необходимость игнорирования
 * переименования проекта в случае, когда оно приводит
 * к несовпадению имени проекта и имени его папки в
 * файловой системе.
 * Свойство сохраняется между сессиями, а его значение
 * может быть изменено на соответствующей странице
 * окна свойств.
 */
public class RenameIgnoringProperty
{
	private final String m_renameIgnoringKey = "RENAME_IGNORING_KEY";
			
	private IProject m_project;
	
	private QualifiedName m_qualifiedName;
	
	/**
	 * Конструктор класса RenameIgnoringProperty.
	 * Вызывает метод создания квалификатора QualifiedName,
	 * который является ключом, с помощью которого
	 * идентифицируется свойство RenameIgnoringProperty.
	 * @param a_project
	 * 		  Проект, который будет иметь свойство
	 * 		  RenameIgnoringProperty. notNull
	 */
	public RenameIgnoringProperty (IProject a_project)
	{
		m_project = Objects.requireNonNull(a_project);
		createQualifiedName(m_project);
	}
	
	/**
	 * Создаёт квалификатор QualifiedName, который является
	 * ключом, с помощью которого идентифицируется свойство
	 * RenameIgnoringProperty.
	 * @param a_project
	 * 		  Проект, который будет обладать свойством
	 * 		  RenameIgnoringProperty
	 */
	private void createQualifiedName (IProject a_project)
	{
		IPath location = m_project.getLocation();
		String folderName = m_project.getName();
		if (location != null) folderName = location.lastSegment();
		m_qualifiedName = new QualifiedName(folderName, m_renameIgnoringKey);
	}
	
	/**
	 * @return true, если свойство существует, false -
	 * иначе
	 * @throws CoreException
	 */
	public boolean exists () throws CoreException
	{
		String property = m_project.getPersistentProperty(m_qualifiedName);
		return (property != null) ? true : false;
	}
	
	/**
	 * @return значение свойства RenameIgnoringProperty.
	 * Возвращает true, если проект, обладающий свойством,
	 * находится в исключениях, false - если не находится
	 * в исключениях или не существует
	 * @throws CoreException
	 */
	public boolean getValue () throws CoreException
	{
		String property = m_project.getPersistentProperty(m_qualifiedName);
		return (property == null) ? false : Boolean.parseBoolean(property);
	}
	
	/**
	 * Устанавливает значение свойства RenameIgnoringProperty.
	 * @param a_ignoring
	 * 		  Значение свойства для установки
	 * @throws CoreException
	 */
	public void setValue (boolean a_ignoring) throws CoreException
	{
		m_project.setPersistentProperty(m_qualifiedName, Boolean.toString(a_ignoring));
	}
}
