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

/**
 * Класс для проверки имени каждого проекта, входящего в Workspace,
 * на идентичность имени папки проекта в файловой системе.
 */
public class ChangedNameValidator
{	
	private String m_resourceValidName;
	
	private ProblemNameMarkerManager m_markerManager;
	
	private boolean m_createProblemMarker = false;
	
	private RefreshExecutor m_refreshExecutor= new RefreshExecutor();
	
	/**
	 * Проверяет имена уже существующих в рабочей области проектов.
	 * Если имя проекта не совпадает с именем папки проекта в
	 * файловой системе, на проект ставится маркер проблемы.
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
		if (!a_project.exists()) return;
		String name = a_project.getName();
		String pathLastSegment = a_project.getLocation().lastSegment();
		ProblemNameMarkerManager manager = new ProblemNameMarkerManager(a_project);
		try
		{
			if (!name.equals(pathLastSegment)) manager.createMarker();
			else manager.deleteMarker();
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Добавляет к рабочей области слушатели изменения имени
	 * проекта. Если имя проекта не совпадает с именем папки
	 * проекта в файловой системе, на проект ставится маркер
	 * проблемы. После исправления проблемы пользователем
	 * маркер удаляется.
	 */
	public void addChangedNameListeners ()
	{
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener (createPreChangeListener());
		workspace.addResourceChangeListener (createPreRefreshListener(), IResourceChangeEvent.PRE_REFRESH);
		workspace.addResourceChangeListener (createPostBuildListener(), IResourceChangeEvent.POST_BUILD);
	}
	
	/**
	 * Создаёт слушатель изменения ресурса (для события
	 * IResourceChangeEvent.PRE_DELETE). Если изменяемый ресурс
	 * не равен null и является проектом, в слушателе полю,
	 * соответствующему имени папки текущего проверяемого проекта,
	 * присваивается имя папки изменяемого ресурса.
	 * @return слушатель изменения ресурса
	 */
	private IResourceChangeListener createPreChangeListener ()
	{
		return new IResourceChangeListener ()
		{
			@Override
			public void resourceChanged (IResourceChangeEvent a_event)
			{
				IResource resource = a_event.getResource();
				if (resource == null  || resource.getType() != IResource.PROJECT) return;
				m_resourceValidName = resource.getLocation().lastSegment();
			}
		};
	}
	
	/**
	 * Создаёт слушатель изменения ресурса (для события
	 * IResourceChangeEvent.PRE_REFRESH). На основании результата
	 * сравнения имени проекта и имени папки проекта слушатель
	 * присваивает полю для хранения информации о необходимости
	 * создания маркера на текущем проверяемом проекте значение
	 * true (если имена не идентичны) или false (если имена
	 * идентичны).
	 * @return слушатель изменения ресурса
	 */
	private IResourceChangeListener createPreRefreshListener ()
	{
		return new IResourceChangeListener ()
		{
			@Override
			public void resourceChanged (IResourceChangeEvent a_event)
			{
				if (m_resourceValidName == null) return;
				
				IResource preRefreshResource = a_event.getResource();
				if (preRefreshResource == null) return;
				String newName = preRefreshResource.getName();
				
				if (!newName.equals(m_resourceValidName)) m_createProblemMarker = true;
				else m_createProblemMarker = false;
				
				if (preRefreshResource.exists()) m_markerManager = new ProblemNameMarkerManager(preRefreshResource);
			}
		};
	}
	
	/**
	 * Создаёт слушатель изменения ресурса (для события
	 * IResourceChangeEvent.POST_BUILD). На основании значения
	 * поля для хранения информации о необходимости
	 * создания маркера на текущем проверяемом проекте слушатель
	 * вызывает метод создания или удаления маркера из класса
	 * ProblemNameMarkerManager. Если создание/удаление
	 * произошло, происходит обновление всех проектов, в ином
	 * случае (например, когда пользователь повторно неверно
	 * переименовал проект, и на нём уже стоит маркер),
	 * обновление не производится.
	 * @return слушатель изменения ресурса
	 */
	private IResourceChangeListener createPostBuildListener ()
	{
		return new IResourceChangeListener ()
		{
			@Override
			public void resourceChanged (IResourceChangeEvent a_event)
			{
				if (m_markerManager == null) return;
				IResource resource =  m_markerManager.getResource();
				if (resource == null || !resource.exists()) return;
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
