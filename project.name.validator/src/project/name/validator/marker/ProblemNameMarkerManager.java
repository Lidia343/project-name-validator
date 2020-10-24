package project.name.validator.marker;

import java.util.Objects;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import project.name.validator.ProjectValidator;

/**
 * Класс для управления процессами создания и удаления
 * маркера.
 */
public class ProblemNameMarkerManager
{
	public static final String MARKER_ATTRIBUTE_PROBLEM_NAME = "MARKER_ATTRIBUTE_PROBLEM_NAME";
	
	public static final String MARKER_ATTRIBUTE_VALUE_PROBLEM_NAME = "problemName";
	
	public static final String MARKER_ATTRIBUTE_VALUE_MESSAGE = "Имя проекта должно совпадать с именем его папки " +
																"в файловой системе.";
	
	private IResource m_resource;
	
	private ProjectValidator m_projectValidator;
	
	/**
	 * Конструктор класса ProblemNameMarkerManager.
	 * @param a_resource
	 * 		  Ресурс, к которому будет привязан маркер.
	 * 		  NotNull
	 */
	public ProblemNameMarkerManager (IResource a_resource)
	{
		m_resource = Objects.requireNonNull(a_resource);
		m_projectValidator = new ProjectValidator(m_resource);
	}
	
	/**
	 * Создаёт проблемный маркер, указывающий на неверное
	 * имя ресурса, если данный маркер ещё не был создан.
	 * @return true - если маркер был создан, false -
	 * иначе 
	 * @throws CoreException
	 */
	public boolean createMarker () throws CoreException
	{
		if (!m_projectValidator.isProjectOpen() || markerExists()) return false;
		IMarker marker = m_resource.createMarker(IMarker.PROBLEM);
		marker.setAttribute(MARKER_ATTRIBUTE_PROBLEM_NAME, MARKER_ATTRIBUTE_VALUE_PROBLEM_NAME);
		IPath location = m_resource.getLocation();
		if (location != null)
		{
			marker.setAttribute(IMarker.LOCATION, location.toString());
		}
		marker.setAttribute(IMarker.MESSAGE, MARKER_ATTRIBUTE_VALUE_MESSAGE);
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		marker.setAttribute(IMarker.USER_EDITABLE, false);
		return true;
	}
	
	/**
	 * Удаляет маркер, если он существует.
	 * @return true - если маркер был удалён, false -
	 * иначе
	 * @throws CoreException
	 */
	public boolean deleteMarker () throws CoreException
	{
		IMarker marker = findMarker();
		if (marker != null) 
		{
			marker.delete();
			return true;
		}
		return false;
	}
	
	/**
	 * @return true - если маркер существует, false -
	 * иначе
	 * @throws CoreException
	 */
	public boolean markerExists () throws CoreException
	{
		if (findMarker() != null) return true;
		return false;
	}
	
	/**
	 * @return первый найденный на ресурсе проблемный
	 * маркер, указывающий на неверное имя ресурса.
	 * Может быть null.
	 * @throws CoreException
	 */
	public IMarker findMarker () throws CoreException
	{
		if (!m_projectValidator.isProjectOpen()) return null;
		IMarker[] markers = m_resource.findMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO);
		for (IMarker marker : markers)
		{
			if (marker.getAttribute(MARKER_ATTRIBUTE_PROBLEM_NAME, null) != null)
			{
				return marker;
			}
		}
		return null;
	}
	
	/**
	 * @return ресурс, к которому привязан маркер
	 */
	public IResource getResource ()
	{
		return m_resource;
	}
}
