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

import project.name.validator.log.ErrorStatusHandler;
import project.name.validator.log.Messages;
import project.name.validator.marker.ProblemNameMarkerManager;

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
			validateProjectName(project);
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
	private void validateProjectName (IProject a_project)
	{
		String name = a_project.getName();
		IPath location = a_project.getLocation();
		if (location == null) return;
		String pathLastSegment = location.lastSegment();
		ProblemNameMarkerManager manager = new ProblemNameMarkerManager(a_project);
		if (!name.equals(pathLastSegment))
		{
			try
			{
				manager.createMarker();
			}
			catch (CoreException e)
			{
				ErrorStatusHandler.log(e, Messages.Exception_Marker_Creation);
			}
		}
		else
		{
			try
			{
				manager.deleteMarker();
			}
			catch (CoreException e)
			{
				ErrorStatusHandler.log(e, Messages.Exception_Marker_Deletion);
			}
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
	 * IResourceChangeEvent.POST_BUILD). Слушатель
	 * вызывает метод создания или удаления маркера из класса
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
					
					validateProjectName(resource.getProject());
				}
			}
		};
	}
}
