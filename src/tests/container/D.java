package tests.container;

import container.Service;

/**
 * Classe utilisée pour le test de l'appel de config
 * @author pf
 *
 */
public class D implements Service
{
	public boolean updateConfigOk = false;
	
	public D()
	{}
	
	@Override
	public void updateConfig()
	{
		updateConfigOk = true;
	}
}
