package tests.container;

import container.Service;

/**
 * Classe utilisée pour le test de dépendance circulaire
 * @author pf
 *
 */
public class A implements Service
{
	public A(B b)
	{}
	
	@Override
	public void updateConfig() {}

}
