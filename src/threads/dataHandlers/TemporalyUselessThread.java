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
public class TemporalyUselessThread extends AbstractThread
{
    SerialPort serialPort;

    private InputStream input;

    private BufferedWriter out;

    private Robot robot;

    public TemporalyUselessThread(Robot robot)
    {
        this.robot = robot;
        try
        {
            File file = new File("data.txt");
            if (!file.exists())
            {
                //file.delete();
                file.createNewFile();
            }
            out = new BufferedWriter(new FileWriter(file));

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
        while(true)
        {
            String vals = readLine();
            try {
                out.write(vals+";"+robot.getPositionFast().x + ";" + robot.getPositionFast().y);
                out.newLine();
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Got one !");
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
}
