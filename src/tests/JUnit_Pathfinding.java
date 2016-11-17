package tests;
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

import graphics.Window;
import org.junit.Before;
import org.junit.Test;
import pathfinder.Graphe;
import pathfinder.Noeud;
import pathfinder.Pathfinding;
import scripts.ScriptManager;
import strategie.GameState;
import table.Table;
import utils.Log;

import java.util.ArrayList;

/**
 * teste la fermeture des portes par la version 0 du script
 * @author alban
 *
 */
public class JUnit_Pathfinding extends JUnit_Test{

    private GameState mRobot;
    private ScriptManager scriptManager;
    private Table table;

    private Pathfinding pf;

    private Window win;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        table = container.getService(Table.class);


        log = container.getService(Log.class);
        win = new Window(table);

        pf = container.getService(Pathfinding.class);
    }


    @Test
    public void iniTable() throws Exception
    {

        log.debug("JUnit_Pathfinding.iniTable()");
        Table T = container.getService(Table.class);
        //La position de depart est mise dans le updateConfig()


        Pathfinding pf=new Pathfinding(log,config,T);
        Graphe graphe= new Graphe(log,config,T);
        graphe.initGraphe();
        pf.setGraphe(graphe);

        ArrayList<Noeud> aff= pf.Astarfoulah(graphe.getlNoeuds().get(1),graphe.getlNoeuds().get(20));
        log.debug(aff);
        for (Noeud x:aff) {
            System.out.println(x.indice+" "+x.position.x+" "+x.position.y);
        }

    }


    //@Test
    public void testClickedPF() throws Exception
    {
        while(true)
        {
            if(win.getMouse().hasClicked())
            {
                // try
                // {
                long start = System.currentTimeMillis();
                // TODO  win.getPanel().drawArrayList(pf.Astarfoulah(win.getMouse().getLeftClickPosition(), win.getMouse().getRightClickPosition()));
                long end = System.currentTimeMillis();
                System.out.println("time elapsed : " + (end - start));
                // }
          /*  catch(PathNotFoundException e)
            {
                log.debug("pas de chemin trouve entre "+win.getMouse().getLeftClickPosition()+"et"+ win.getMouse().getRightClickPosition());
            }
            catch(PointInObstacleException e)
            {
                log.debug("point d'arrivée dans un obstacle");
            }*/ //TODO exceptions
                win.getPanel().repaint();
            }
            else
                Thread.sleep(200);
        }
    }


}

/**
 * Created by shininisan on 17/11/16.
 */
