package project.name.validator.log;

import org.eclipse.core.internal.runtime.Activator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Обработчик статуса IStatus.ERROR.
 */
@SuppressWarnings("restriction")
public class ErrorStatusHandler
{
	/**
	 * Логирует передаваемые исключение и сообщение, а также
	 * показывает их в окне пользователю.
	 * @param a_exception
	 * 		  Исключение
	 * @param a_message
	 * 		  Сообщение
	 */
	public static void log (Exception a_exception, String a_message)
	{
		IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, a_message, a_exception);
		StatusManager.getManager().handle(status, StatusManager.LOG | StatusManager.SHOW);
	}
}
