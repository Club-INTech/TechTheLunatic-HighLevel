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

package threads.dataHandlers;

import debug.AffichageDebug;
import debug.DisplayTable;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import robot.Robot;
import threads.AbstractThread;
import utils.Sleep;

import java.io.*;

/**
 * Lecture des balises, juste utilisé pendant leur phase de dev pour récupérer des valeurs
 */
public class ThreadBalises extends AbstractThread
{
    SerialPort serialPort;

    private InputStream input;

    private BufferedWriter out;
    private BufferedWriter outpos;

    private Robot robot;

    private enum Perm{
        MUST_BE_LAST,
        CAN_BE_FIRST,
        CANT_BE_FIRST
    }

    private final byte CANAL_1 = 1;
    private final byte CANAL_2 = 2;
    private final byte INT = 0;

    private DisplayTable debugPos = null;
    private AffichageDebug debugSignal = null;

    private final long MAX_INTER_GAP = 6250;
    private final long MAX_EXTREME_GAP = 9870;

    private Perm[] permissions = new Perm[3];

    private long[] timestamps = new long[3];

    private boolean[] wrote = new boolean[3];

    private double[] counter = new double[4];

    private final String[] names = new String[4];

    public ThreadBalises(Robot robot)
    {
        this.robot = robot;
        try
        {
            File file = new File("data.txt");
            File file2 = new File("pos.txt");
            if (!file.exists())
            {
                //file.delete();
                file.createNewFile();
            }
            if (!file2.exists())
            {
                //file.delete();
                file2.createNewFile();
            }

            out = new BufferedWriter(new FileWriter(file));
            outpos = new BufferedWriter(new FileWriter(file2));

        } catch (IOException e) {
            log.critical("Manque de droits pour l'output");
            //out = null;
            e.printStackTrace();
        }
        CommPortIdentifier portId = null;
        try
        {
            portId = CommPortIdentifier.getPortIdentifier("/dev/ttyACM0");
        }
        catch (NoSuchPortException e2)
        {
            log.critical("Catch de "+e2+" dans initialize");
        }

        try
        {
            serialPort = (SerialPort) portId.open(this.getClass().getName(), 1000);
        }
        catch (PortInUseException e1)
        {
            log.critical("Catch de "+e1+" dans initialize");
        }
        try
        {
            serialPort.setSerialPortParams(115200,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.notifyOnDataAvailable(false);

            input = serialPort.getInputStream();

        }
        catch (Exception e)
        {
            log.critical("Catch de "+e+" dans initialize");
        }
    }

    @Override
    public void run()
    {
        String vals;
        byte no;
        long timestamp;
        long lastSignalDebug = System.currentTimeMillis();
        serialPort.notifyOnDataAvailable(false);
        updatePermissions(robot.getPositionFast().x, robot.getPositionFast().y);
        while(true)
        {

            vals = readLine();

            try{
                no = Byte.parseByte(vals.substring(0,1));
                timestamp = Long.parseLong(vals.substring(2));
            } catch (NumberFormatException e)
            {

                log.warning("BALISES : Mauvaise donnée balise");
                continue;
            }

            checkData(no, timestamp);
            counter[no]++;

            if(wrote[0] && wrote[1] && wrote[2])
            {
                wrote[0] = false; wrote[1] = false; wrote[2] = false;
                updatePermissions(robot.getPositionFast().x, robot.getPositionFast().y);

                if(extremeLateness())
                {
                    log.critical("BALISES : Valeurs refusées car ecart > 9670 us");
                    continue;
                }

                try {
                    out.write(timestamps[CANAL_1]+";"+timestamps[CANAL_2]+";"+timestamps[INT]);
                    out.newLine();
                    out.flush();
                    outpos.write(robot.getPositionFast().x+"\t"+robot.getPositionFast().y);
                    outpos.newLine();
                    outpos.flush();

                    if(debugPos != null)
                    {
                        debugPos.addPointFromTimestamps(timestamps[CANAL_1], timestamps[CANAL_2], timestamps[INT],0);
                        debugPos.addPoint(robot.getPositionFast().clone(),1);
                    }
                    counter[3]++;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(debugSignal != null && System.currentTimeMillis() - lastSignalDebug >= 1000)
            {
                debugSignal.addData(counter.clone(), names);
                counter[0]=0;  counter[1]=0;  counter[2]=0; counter[3]=0;
                lastSignalDebug = System.currentTimeMillis();
            }

            Sleep.sleep(1);

        }
    }

    /**
     * Lit un byte. On sait qu'il doit y en a avoir un.
     * @return
     * @throws IOException
     */
    public int read() throws IOException
    {
        if (input.available() == 0) Sleep.sleep(5); // On attend un tout petit peu, au cas où

        if (input.available() == 0)
            throw new IOException(); // visiblement on ne recevra rien de plus

        byte out = (byte) input.read();


        return out & 0xFF;

    }

    public boolean available() throws IOException
    {
        // tant qu'on est occupé, on dit qu'on ne reçoit rien
       /* if(busy)
            return false;*/
        return input.available() != 0;
    }

    private String readLine()
    {
        String res = "";
            try {
                int lastReceived;

                long time = System.currentTimeMillis();
                while (!available())
                {
                    if(System.currentTimeMillis() - time > 1000)
                    {
                        log.critical("Il ne daigne même pas répondre !");
                        return (res+(char)260);
                    }
                    Thread.sleep(5);
                }

                while (available()) {

                    if ((lastReceived = read()) == 10)
                        break;

                    res += (char) lastReceived;

                    time = System.currentTimeMillis();
                    while (!available())
                    {
                        if(System.currentTimeMillis() - time > 1000)
                        {
                            log.critical("blocaqe attente nouveau char (pas de /r ?) dernier : "+(int)lastReceived);
                            return (res+(char)260);
                        }
                        Thread.sleep(5);
                    }
                }

            } catch (IOException e) {
                log.debug("On a perdu la série !!");
                res+=(char)260;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return res;
    }

    void updatePermissions(float x, float y)
    {
        if (x < -1000 && y < 200)
        {
            permissions[CANAL_2] = Perm.CAN_BE_FIRST;
            permissions[CANAL_1] = Perm.CANT_BE_FIRST;
            permissions[INT] = Perm.CANT_BE_FIRST;
        }
        else if (x < -1000 && y > 1800)
        {
            permissions[CANAL_1] = Perm.CAN_BE_FIRST;
            permissions[CANAL_2] = Perm.CANT_BE_FIRST;
            permissions[INT] = Perm.CANT_BE_FIRST;
        }
        else if (x < -700)
        {
            permissions[INT] = Perm.MUST_BE_LAST;
            permissions[CANAL_1] = Perm.CAN_BE_FIRST;
            permissions[CANAL_2] = Perm.CAN_BE_FIRST;
        }
        else if (x > 700)
        {
            permissions[INT] = Perm.CAN_BE_FIRST;
            permissions[CANAL_1] = Perm.CANT_BE_FIRST;
            permissions[CANAL_2] = Perm.CANT_BE_FIRST;
        }
        else
        {
            permissions[CANAL_1] = Perm.CAN_BE_FIRST;
            permissions[CANAL_2] = Perm.CAN_BE_FIRST;
            permissions[INT] = Perm.CAN_BE_FIRST;
        }
    }

    void checkData(byte no, long timestamp)
    {
        if(wrote[no])
            return;

        if( permissions[no] == Perm.CAN_BE_FIRST || ( permissions[no] == Perm.CANT_BE_FIRST && !isFirst(no) ) ||
                ( permissions[no] == Perm.MUST_BE_LAST && isLast(no) ) )
        {
            timestamps[no] = timestamp;
            wrote[no] = true;

            if(isLate(no))
            {
                for(byte i=0 ; i<3 ; i++)
                    if(i!=no)
                        wrote[i]=false;
                updatePermissions(robot.getPositionFast().x, robot.getPositionFast().y);
            }
        }
    }

    boolean isFirst(byte no)
    {
        for(byte i=0; i < 3 ; i++)
            if(i != no && wrote[i])
                return false;
        return true;
    }

    boolean isLast(byte no)
    {
        for(byte i=0; i<3 ; i++)
            if(i != no && !wrote[i])
                return false;
        return true;
    }

    boolean isLate(byte no)
    {
        for(byte i=0 ; i<3 ;i++)
            if(i!=no && wrote[i] && timestamps[no] - timestamps[i] > MAX_INTER_GAP)
                return true;
        return false;
    }

    boolean extremeLateness()
    {
        long max = timestamps[0];
        long min = timestamps[0];
        for(byte i=0 ; i<3 ; i++)
        {
            if(timestamps[i] > max)
                max = timestamps[i];
            else if(timestamps[i] < min)
                min = timestamps[i];
        }

        return max-min > MAX_EXTREME_GAP;
    }

    public void showDebug()
    {
        this.debugPos = new DisplayTable(true);
        this.debugSignal = new AffichageDebug();
        names[CANAL_1] = "Signal CANAL_1";
        names[CANAL_2] = "Signal CANAL_2";
        names[INT] = "Signal INT";
        names[3] = "Mesures valides/sec";

    }
}
