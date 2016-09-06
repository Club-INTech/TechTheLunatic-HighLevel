package tests.container;

import container.Service;

/**
 * Classe utilisée pour le test de plusieurs constructeurs
 * @author pf
 *
 */
public class C implements Service
{
	public C(B b)
	{}

	public C(A a)
	{}
	
	@Override
	public void updateConfig() {}

}
