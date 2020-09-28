package project.name.validator.main;

import org.eclipse.ui.IStartup;

import project.name.validator.ChangedNameValidator;

public class Startup implements IStartup
{
	@Override
	public void earlyStartup()
	{
		new ChangedNameValidator().addChangedNameListeners();
	}
}
