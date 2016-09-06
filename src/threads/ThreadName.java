package threads;

import threads.dataHandlers.*;

/**
 * Tous les threads à instancier au début du match. Utilisé par le container
 * @author pf
 *
 */

public enum ThreadName
{
	TIMER(ThreadTimer.class),
	BALISES(ThreadBalises.class),
	EVENTS(ThreadEvents.class),
	SENSOR(ThreadSensor.class),
	SERIAL(ThreadSerial.class);
	
	public Class<? extends AbstractThread> c;
	
	private ThreadName(Class<? extends AbstractThread> c)
	{
		this.c = c;
	}
	

}
