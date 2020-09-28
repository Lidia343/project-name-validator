package project.name.validator;


import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class ChangedNameValidator
{
	private IResource m_preChangeResource;
	
	private IProject m_project;
	
	private boolean m_createProblemMarker = false;
	
	private final String m_problemNameMarkerAttribute = "PROBLEM_NAME_MARKER_ATTRIBUTE";
	
	public void addChangedNameListeners ()
	{
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener (createPreChangeListener());
		workspace.addResourceChangeListener (createPreRefreshListener(), IResourceChangeEvent.PRE_REFRESH);
		workspace.addResourceChangeListener (createPostChangeListener(), IResourceChangeEvent.POST_BUILD);
	}
	
	private IResourceChangeListener createPreChangeListener ()
	{
		return new IResourceChangeListener ()
		{
			@Override
			public void resourceChanged (IResourceChangeEvent a_event)
			{
				m_preChangeResource = a_event.getResource();
			}
		};
	}
	
	private IResourceChangeListener createPreRefreshListener ()
	{
		return new IResourceChangeListener ()
		{
			@Override
			public void resourceChanged (IResourceChangeEvent a_event)
			{
				if (m_preChangeResource == null) return;
				
				String pathLastSegment = m_preChangeResource.getFullPath().lastSegment();
				
				IResource preRefreshResource = a_event.getResource();
				if (preRefreshResource == null) return;
				String newName = preRefreshResource.getName();
				
				if (!newName.equals(pathLastSegment)) m_createProblemMarker = true;
				else m_createProblemMarker = false;
				
				m_project = preRefreshResource.getProject();
			}
		};
	}
	
	private IResourceChangeListener createPostChangeListener ()
	{
		return new IResourceChangeListener ()
		{
			@Override
			public void resourceChanged (IResourceChangeEvent a_event)
			{
				if (m_project == null) return;
				try
				{
					if (m_createProblemMarker)
					{
						IMarker marker = m_project.createMarker(IMarker.PROBLEM);
						marker.setAttribute(m_problemNameMarkerAttribute, "problem_name");
					}
					else
					{
						IMarker[] markers = m_project.findMarkers(IMarker.PROBLEM, false, IResource.DEPTH_INFINITE);
						if (markers.length == 1)
						{
							IMarker marker = markers[0];
							if (marker.getAttribute(m_problemNameMarkerAttribute, null) != null)
							{
								marker.delete();
							}
						}
					}
				}
				catch (CoreException e)
				{
					e.printStackTrace();
				}
			}
		};
	}
}
