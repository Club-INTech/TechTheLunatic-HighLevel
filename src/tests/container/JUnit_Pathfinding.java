package tests.container;
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

import enums.Speed;
import hook.Hook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pathfinder.Graphe;
import pathfinder.Noeud;
import scripts.ScriptManager;
import strategie.GameState;
import table.Table;
import tests.JUnit_Test;

import java.util.ArrayList;


import enums.Speed;
import hook.Hook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scripts.ScriptManager;
import strategie.GameState;
import table.Table;
import  pathfinder.Pathfinding;
import java.util.ArrayList;

/**
 * teste la fermeture des portes par la version 0 du script
 * @author alban
 *
 */
public class JUnit_Pathfinding extends JUnit_Test{

    private GameState mRobot;
    private ScriptManager scriptManager;

    @Before
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


}

/**
 * Created by shininisan on 17/11/16.
 */
