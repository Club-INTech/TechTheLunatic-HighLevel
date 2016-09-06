package tests.container;

import container.Service;

/**
 * Classe utilisée pour le test de dépendance circulaire
 * @author pf
 *
 */
public class B implements Service
{
	public B(A a)
	{}
	
	@Override
	public void updateConfig() {}
}
