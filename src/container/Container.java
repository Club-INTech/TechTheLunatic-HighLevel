/*
 * Copyright (c) 2016, INTech.
 *
 * This file is part of INTech's HighLevel.
 *
 *  INTech's HighLevel is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  INTech's HighLevel is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with it.  If not, see <http://www.gnu.org/licenses/>.
 */

package container;

import exceptions.ContainerException;
import robot.serial.SerialManager;
import threads.ThreadExit;
import threads.ThreadName;
import utils.Config;
import utils.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * 
 * Gestionnaire de la durée de vie des objets dans le code.
 * Permet à n'importe quelle classe implémentant l'interface "Service" d'appeller d'autres instances de services via son constructeur.
 * Une classe implémentant service n'est instanciée que par la classe "Container"
 * La liste des services est disponible dans l'énumération ServiceNames 
 * 
 * @author pf
 */
public class Container implements Service
{
	// liste des services déjà instanciés. Contient au moins Config et Log. Les autres services appelables seront présents quand ils auront été appelés
	private HashMap<String, Service> instanciedServices = new HashMap<String, Service>();
	
	private boolean threadsStarted = false;
	
	// permet de détecter les dépendances circulaires
	private Stack<String> stack = new Stack<String>();
	
	private Log log;
	private Config config;
	
	private static int nbInstances = 0;
	
	private static final boolean showGraph = false;
	private FileWriter fw;

	/**
	 * Fonction appelé automatiquement à la fin du programme.
	 * ferme la connexion serie, termine les différents threads, et ferme le log.
	 * @throws InterruptedException 
	 * @throws ContainerException 
	 */
	public void destructor() throws ContainerException, InterruptedException
	{
		// arrêt des threads
		if(threadsStarted)
			for(ThreadName n : ThreadName.values())
			{
				getService(n.c).interrupt();
				getService(n.c).join(100); // on attend au plus 50ms que le thread s'arrête
			}
		
		threadsStarted = false;
		
		log.debug("Fermeture de la série");
		/**
		 * Mieux vaut écrire SerialManager.class.getSimpleName()) que "SerialManager",
		 * car en cas de refactor, le premier est automatiquement ajusté
		 */
		if(instanciedServices.containsKey(SerialManager.class.getSimpleName()))
			((SerialManager)instanciedServices.get(SerialManager.class.getSimpleName())).threadSerial.close();

		if(showGraph)
		{
			try {
				fw.write("}\n");
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// fermeture du log
		log.debug("Fermeture du log");
		log.close();
		nbInstances--;
		System.out.println("Singularité évaporée.");
		System.out.println();
		printMessage("outro.txt");
	}
	
	/**
	 * Instancie le gestionnaire de dépendances et quelques services critiques (log et config qui sont interdépendants)
	 * @throws ContainerException si un autre container est déjà instancié
	 * @throws InterruptedException 
	 */
	public Container() throws ContainerException, InterruptedException
	{
		/**
		 * On vérifie qu'il y ait un seul container à la fois
		 */
		if(nbInstances != 0)
			throw new ContainerException("Un autre container existe déjà! Annulation du constructeur.");

		nbInstances++;
		
		/**
		 * Affichage d'un petit message de bienvenue
		 */
		printMessage("intro.txt");
		
		/**
		 * Affiche la version du programme (dernier commit et sa branche)
		 */
		try {
			Process p = Runtime.getRuntime().exec("git log -1 --oneline");
			Process p2 = Runtime.getRuntime().exec("git branch");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader in2 = new BufferedReader(new InputStreamReader(p2.getInputStream()));
			String s = in.readLine();
			int index = s.indexOf(" ");
			in.close();
			String s2 = in2.readLine();

			while(!s2.contains("*"))
				s2 = in2.readLine();

			int index2 = s2.indexOf(" ");
			System.out.println("Version : "+s.substring(0, index)+" on "+s2.substring(index2+1)+" - ["+s.substring(index+1)+"]");
			in2.close();
		} catch (IOException e1) {
			System.out.println(e1);
		}
		
		/**
		 * Infos diverses
		 */
		System.out.println("System : "+System.getProperty("os.name")+" "+System.getProperty("os.version")+" "+System.getProperty("os.arch"));
		System.out.println("Java : "+System.getProperty("java.vendor")+" "+System.getProperty("java.version")+", max memory : "+Math.round(100.*Runtime.getRuntime().maxMemory()/(1024.*1024.*1024.))/100.+"G, available processors : "+Runtime.getRuntime().availableProcessors());
		System.out.println();

		System.out.println("    Remember, with great power comes great current squared times resistance !");
		System.out.println();
		
		log = getServiceRecursif(Log.class);
		config = getServiceRecursif(Config.class);
		log.updateConfig();

		// Le container est aussi un service
		instanciedServices.put(getClass().getSimpleName(), this);

		if(showGraph)
		{
			try {
				fw = new FileWriter(new File("dependances.dot"));
				fw.write("digraph dependancesJava {\n");
			} catch (IOException e) {
				log.warning(e);
			}
		}
	}
	
	/**
	 * Créé un object de la classe demandée, ou le récupère s'il a déjà été créé
	 * S'occupe automatiquement des dépendances
	 * Toutes les classes demandées doivent implémenter Service ; c'est juste une sécurité.
	 * @param classe
	 * @return un objet de cette classe
	 * @throws ContainerException
	 * @throws InterruptedException 
	 */
	public <S extends Service> S getService(Class<S> serviceTo) throws ContainerException, InterruptedException
	{
		stack.clear(); // pile d'appel vidée
		return getServiceDisplay(null, serviceTo);
	}
	
	@SuppressWarnings("unused")
	private <S extends Service> S getServiceDisplay(Class<? extends Service> serviceFrom, Class<S> serviceTo) throws ContainerException, InterruptedException
	{
		/**
		 * On ne crée pas forcément le graphe de dépendances pour éviter une lourdeur inutile
		 */
		if(showGraph && !serviceTo.equals(Log.class))
		{
			ArrayList<String> ok = new ArrayList<String>();

			try {
				if(ok.contains(serviceTo.getSimpleName()))
					fw.write(serviceTo.getSimpleName()+" [color=grey80, style=filled];\n");
				else
					fw.write(serviceTo.getSimpleName()+";\n");

				if(serviceFrom != null)
					fw.write(serviceFrom.getSimpleName()+" -> "+serviceTo.getSimpleName()+";\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return getServiceRecursif(serviceTo);
	}

	/**
	 * Méthode récursive qui fait tout le boulot
	 * @param classe
	 * @return
	 * @throws ContainerException
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked")
	public <S extends Service> S getServiceRecursif(Class<S> classe) throws ContainerException, InterruptedException
	{
		try {
			/**
			 * Si l'objet existe déjà, on le renvoie
			 */			
			if(instanciedServices.containsKey(classe.getSimpleName()))
				return (S) instanciedServices.get(classe.getSimpleName());
			
			/**
			 * Détection de dépendances circulaires
			 */
			if(stack.contains(classe.getSimpleName()))
			{
				// Dépendance circulaire détectée !
				String out = "";
				for(String s : stack)
					out += s + " -> ";
				out += classe.getSimpleName();
				throw new ContainerException(out);
			}
			
			// Pas de dépendance circulaire
			
			// On met à jour la pile
			stack.push(classe.getSimpleName());

			/**
			 * Récupération du constructeur et de ses paramètres
			 * On suppose qu'il n'y a chaque fois qu'un seul constructeur pour cette classe
			 */
			if(classe.getConstructors().length > 1)
				throw new ContainerException(classe.getSimpleName()+" a plusieurs constructeurs !");

			Constructor<S> constructeur = (Constructor<S>) classe.getConstructors()[0];
			Class<Service>[] param = (Class<Service>[]) constructeur.getParameterTypes();
			
			/**
			 * On demande récursivement chacun de ses paramètres
			 */
			Object[] paramObject = new Object[param.length];
			for(int i = 0; i < param.length; i++)
				paramObject[i] = getServiceDisplay(classe, param[i]);

			/**
			 * Instanciation et sauvegarde
			 */
			S s = constructeur.newInstance(paramObject);
			instanciedServices.put(classe.getSimpleName(), (Service) s);
			
			/**
			 * Mise à jour de la config
			 */
			if(config != null)
				for(Method m : Service.class.getMethods())
					classe.getMethod(m.getName(), Config.class).invoke(s, config);
			
			// Mise à jour de la pile
			stack.pop();
			
			return s;
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException | InstantiationException e) {
			e.printStackTrace();
			throw new ContainerException(e.getMessage());
		}
	}

	/**
	 * Démarrage de tous les threads
	 */
	public void startAllThreads() throws InterruptedException
	{
		for(ThreadName n : ThreadName.values())
		{
			try {
				getServiceRecursif(n.c).start();
			} catch (ContainerException e) {
				log.critical(e);
			}
		}
			
		/**
		 * Planification du hook de fermeture
		 */
		ThreadExit.makeInstance(this);
		Runtime.getRuntime().addShutdownHook(ThreadExit.getInstance());
		
		log.debug("Démarrage des threads fini");
		threadsStarted = true;

	}
	
	/**
	 * Affichage d'un fichier
	 * @param filename
	 */
	private void printMessage(String filename)
	{
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader(filename));
				String line;
				    
				while((line = reader.readLine()) != null)
					System.out.println(line);
				    
				reader.close();
			} catch (IOException e) {
				System.err.println(e);
			}

	}

	@Override
	public void updateConfig()
	{}

}
