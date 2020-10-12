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
	 * Проверяет имена уже существующих в рабочей области проектов,
	 * не занесённых пользователем в исключения.
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
	
	/**
	 * Метод, определяющий необходимость проверки проекта, передаваемого
	 * в качестве параметра.
	 * @param a_project
	 * 		  Проект, для которого необходимо определить, занесён ли он
	 * 		  пользователем в исключения
	 * @return true, если проект занесён в исключения (то есть свойство,
	 * определяющее, нужно ли игнорировать переименование проекта,
	 * существует и его значение равно true), false - иначе
	 */
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
	
	/**
	 * @param a_project
	 * 		  Проект для проверки
	 * @return true в случае существования свойства RenameIgnoringProperty
	 * на проекте a_project, false - иначе
	 */
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
	 * Если параметр a_createWarningDialog равен true, в случае
	 * несовпадения имён и несуществования на проекте свойства
	 * RenameIgnoringProperty метод создаёт диалоговое окно с
	 * предупреждением пользователю и возможностью выбрать,
	 * нужно ли заносить проект a_project в исключения.
	 * @param a_project
	 * 		  Проект, имя которого необходимо проверить. NotNull
	 * @param a_createWarningDialog
	 * 		  Параметр, определяющий нужно ли создавать диалоговое
	 * 		  окно с предупреждением в случае несовпадения имени
	 * 		  проекта и имени его папки и несуществования свойства
	 * 		  RenameIgnoringProperty на проекте
	 */
	public void validateProjectName (IProject a_project, boolean a_createWarningDialog)
	{
		/**
		 * Если проект занесён в исключения, следует вызвать метод
		 * ProblemNameMarkerManager.deleteMarker, который удалит
		 * маркер, если он существует:*/
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
				/**
				 * Если пользователь с помощью диалогового окна занёс
				 * проект в исключения, необходимость создания маркера
				 * отсутствует:*/
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
	
	/**
	 * Метод удаляет маркер проблемного имени проекта, если он
	 * существует.
	 * @param a_manager
	 * 	      Объект, с помощью которого происходит удаление
	 * 		  маркера
	 */
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
	 * проекта в файловой системе, и проект не занесён в
	 * исключения, на него ставится маркер проблемы. После
	 * исправления проблемы пользователем маркер удаляется.
	 */
	public void addChangedNameListener ()
	{
		ResourcesPlugin.getWorkspace().addResourceChangeListener (createPostBuildListener(),
																  IResourceChangeEvent.POST_BUILD);
	}
	
	
	/**
	 * Создаёт слушатель изменения ресурса (для события
	 * IResourceChangeEvent.POST_BUILD). Слушатель вызывает
	 * метод проверки имени проекта ресурса ("validateProjectName").
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
