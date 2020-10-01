package project.name.validator.marker;

import java.util.Objects;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class ProblemNameMarkerManager
{
	public static final String MARKER_ATTRIBUTE_PROBLEM_NAME = "MARKER_ATTRIBUTE_PROBLEM_NAME";
	
	public static final String MARKER_ATTRIBUTE_VALUE_PROBLEM_NAME = "problemName";
	
	public static final String MARKER_ATTRIBUTE_VALUE_MESSAGE = "Имя проекта должно совпадать с именем папки, " +
																"в которой он находится.";
	
	private IProject m_project;
	
	public ProblemNameMarkerManager (IProject a_project)
	{
		m_project = Objects.requireNonNull(a_project);
	}
	
	public boolean createMarker () throws CoreException
	{
		if (markerExists()) return false;
		IMarker marker = m_project.createMarker(IMarker.PROBLEM);
		marker.setAttribute(MARKER_ATTRIBUTE_PROBLEM_NAME, MARKER_ATTRIBUTE_VALUE_PROBLEM_NAME);
		marker.setAttribute(IMarker.LOCATION, m_project.getLocation().toString());
		marker.setAttribute(IMarker.MESSAGE, MARKER_ATTRIBUTE_VALUE_MESSAGE);
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		marker.setAttribute(IMarker.USER_EDITABLE, false);
		return true;
	}
	
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
	
	public boolean markerExists () throws CoreException
	{
		if (findMarker() != null) return true;
		return false;
	}
	
	public IMarker findMarker () throws CoreException
	{
		IMarker[] markers = m_project.findMarkers(IMarker.PROBLEM, false, IResource.DEPTH_INFINITE);
		for (IMarker marker : markers)
		{
			if (marker.getAttribute(MARKER_ATTRIBUTE_PROBLEM_NAME, null) != null)
			{
				return marker;
			}
		}
		return null;
	}
	
	public IProject getProject ()
	{
		return m_project;
	}
}
