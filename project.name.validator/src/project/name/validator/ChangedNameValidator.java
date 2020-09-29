package project.name.validator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import project.name.validator.marker.ProblemNameMarkerManager;

public class ChangedNameValidator
{
	private IResource m_preChangeResource;
	
	private ProblemNameMarkerManager m_markerManager;
	
	private boolean m_createProblemMarker = false;
	
	public void addChangedNameListeners ()
	{
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener (createPreChangeListener());
		workspace.addResourceChangeListener (createPreRefreshListener(), IResourceChangeEvent.PRE_REFRESH);
		workspace.addResourceChangeListener (createPostBuildListener(), IResourceChangeEvent.POST_BUILD);
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
				
				IProject project = preRefreshResource.getProject();
				if (project != null && project.exists()) m_markerManager = new ProblemNameMarkerManager(project);
			}
		};
	}
	
	private IResourceChangeListener createPostBuildListener ()
	{
		return new IResourceChangeListener ()
		{
			@Override
			public void resourceChanged (IResourceChangeEvent a_event)
			{
				if (m_markerManager == null || m_markerManager.getProject() == null) return;
				try
				{
					if (m_createProblemMarker) m_markerManager.createMarker();
					else m_markerManager.deleteMarker();
				}
				catch (CoreException e)
				{
					e.printStackTrace();
				}
			}
		};
	}
}
