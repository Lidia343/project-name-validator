package project.name.validator;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import project.name.validator.marker.ProblemNameMarkerManager;
import project.name.validator.refresh.RefreshExecutor;

public class ChangedNameValidator
{	
	private IResource m_preChangeResource;
	
	private ProblemNameMarkerManager m_markerManager;
	
	private boolean m_createProblemMarker = false;
	
	private RefreshExecutor m_refreshExecutor= new RefreshExecutor();
	
	public void validateExistingProjectNames ()
	{
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects();
		if (projects == null) return;
		for (IProject project : projects)
		{
			validateProjectName(project);
		}
	}
	
	private void validateProjectName (IProject a_project)
	{
		String name = a_project.getName();
		String pathLastSegment = a_project.getFullPath().lastSegment();
		if (!name.equals(pathLastSegment))
		{
			ProblemNameMarkerManager manager = new ProblemNameMarkerManager(a_project);
			try
			{
				manager.createMarker();
			}
			catch (CoreException e)
			{
				e.printStackTrace();
			}
		}
	}
	
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
				IResource resource = a_event.getResource();
				if (resource == null  || resource.getType() != IResource.PROJECT) return;
				m_preChangeResource = resource;
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
					boolean changed = false;
					if (m_createProblemMarker)
					{
						if (m_markerManager.createMarker()) changed = true;
					}
					else
					{
						if (m_markerManager.deleteMarker()) changed = true;
					}
					if (changed) m_refreshExecutor.refresh();
				}
				catch (CoreException | ExecutionException | NotDefinedException | NotEnabledException |
					   NotHandledException e)
				{
					e.printStackTrace();
				}
			}
		};
	}
}
