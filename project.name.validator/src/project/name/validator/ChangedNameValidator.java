package project.name.validator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.PlatformUI;

import project.name.validator.log.ErrorStatusHandler;
import project.name.validator.marker.ProblemNameMarkerManager;
import project.name.validator.ui.ProblemNameDialog;
import project.name.validator.property.RenameIgnoringProperty;

/**
 * Класс для проверки имени каждого проекта, входящего в Workspace,
 * на идентичность имени папки проекта в файловой системе.
 */
public class ChangedNameValidator
{
	/**
	 * Проверяет имена уже существующих в рабочей области проектов.
	 * Если имя проекта не совпадает с именем папки проекта в
	 * файловой системе, на проект ставится маркер проблемы, иначе
	 * маркер удаляется.
	 */
	public void validateExistingProjectNames ()
	{
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects();
		for (IProject project : projects)
		{
			if (project.isOpen()) validateProjectName(project, false);
		}
	}
	
	public boolean ignoreProject (IProject a_project)
	{
		try
		{
			RenameIgnoringProperty property = new RenameIgnoringProperty(a_project);
			if (property.exists() && property.getValue()) return true;
			return false;
		}
		catch(CoreException e)
		{
			ErrorStatusHandler.log(e, e.getMessage());
			return false;
		}
	}
	
	private boolean renameIgnoringPropertyExists (IProject a_project)
	{
		try
		{
			RenameIgnoringProperty property = new RenameIgnoringProperty(a_project);
			return property.exists() ? true : false;
		}
		catch(CoreException e)
		{
			ErrorStatusHandler.log(e, e.getMessage());
			return false;
		}
	}
	
	/**
	 * Проверяет имя проекта a_project.
	 * Если имя проекта не совпадает с именем папки проекта в
	 * файловой системе, на проект ставится маркер проблемы,
	 * иначе маркер удаляется.
	 * @param a_project
	 * 		  Проект, имя которого необходимо проверить. NotNull
	 */
	public void validateProjectName (IProject a_project, boolean a_createWarningDialog)
	{
		ProblemNameMarkerManager manager = new ProblemNameMarkerManager(a_project);
		if (ignoreProject(a_project))
		{
			deleteMarker(manager);
			return;
		}
		String name = a_project.getName();
		IPath location = a_project.getLocation();
		if (location == null) return;
		String pathLastSegment = location.lastSegment();
		if (!name.equals(pathLastSegment))
		{
			if (a_createWarningDialog && !renameIgnoringPropertyExists(a_project))
			{
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable()
				{
				    @Override
				    public void run ()
				    {
				    	ProblemNameDialog dialog = new ProblemNameDialog(a_project);
						dialog.open();
				    }
				});
				if (ignoreProject(a_project)) return;
			}
			try
			{
				manager.createMarker();
			}
			catch (CoreException e)
			{
				ErrorStatusHandler.log(e, Messages.Exception_Marker_Creation);
			}
		}
		else deleteMarker(manager);
	}
	
	private void deleteMarker (ProblemNameMarkerManager a_manager)
	{
		try
		{
			a_manager.deleteMarker();
		}
		catch (CoreException e)
		{
			ErrorStatusHandler.log(e, Messages.Exception_Marker_Deletion);
		}
	}
	
	/**
	 * Добавляет к рабочей области слушатель изменения имени
	 * проекта. Если имя проекта не совпадает с именем папки
	 * проекта в файловой системе, на проект ставится маркер
	 * проблемы. После исправления проблемы пользователем
	 * маркер удаляется.
	 */
	public void addChangedNameListener ()
	{
		ResourcesPlugin.getWorkspace().addResourceChangeListener (createPostBuildListener(),
																  IResourceChangeEvent.POST_BUILD);
	}
	
	
	/**
	 * Создаёт слушатель изменения ресурса (для события
	 * IResourceChangeEvent.POST_BUILD). В зависимости от
	 * результата сравнения имени проекта ресурса с именем
	 * его папки в файловой системе слушатель вызывает метод
	 * создания маркера (если имена не совпадают) или его
	 * удаления (если они совпадают) из класса
	 * ProblemNameMarkerManager.
	 * @return слушатель изменения ресурса
	 */
	private IResourceChangeListener createPostBuildListener ()
	{
		return new IResourceChangeListener ()
		{
			@Override
			public void resourceChanged (IResourceChangeEvent a_event)
			{
				IResourceDelta rootDelta = a_event.getDelta();
				if (rootDelta == null) return;
				IResourceDelta[] children = rootDelta.getAffectedChildren(IResourceDelta.ADDED);
				for (IResourceDelta delta : children)
				{
					IResource resource = delta.getResource();
					
					if (resource == null || !resource.exists() || resource.getType() != IResource.PROJECT) continue;
					
					IProject project = resource.getProject();
					validateProjectName(project, true);
				}
			}
		};
	}
}
