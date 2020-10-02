package project.name.validator.refresh;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Класс для выполнения команды обновления рабочей области.
 */
public class RefreshExecutor
{
	public static final String COMMAND_ID = "org.eclipse.ui.file.refresh";
	
	/**
	 * Обновляет рабочую область посредством выполенения команды
	 * с ID = "org.eclipse.ui.file.refresh".
	 * @throws ExecutionException
	 * @throws NotDefinedException
	 * @throws NotEnabledException
	 * @throws NotHandledException
	 */
	public void refresh () throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException
	{
		 IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
		 handlerService.executeCommand(COMMAND_ID, null);
	}
}
