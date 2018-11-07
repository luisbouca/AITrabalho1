/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

/**
 *
 * @author user
 */

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;


public class App {
    Runtime rt;
    static ContainerController container;

    public static void main(String[] args) {
        App a = new App();

        a.initMainContainerInPlatform("localhost", "9888", "MainContainer");
        int num_aeroportos=10,num_avioes=3;
        for(int i = 0;i<num_aeroportos;i++){
            ContainerController aeroportox = a.initContainerInPlatform("localhost", "9888", "Aeroporto"+i);
            Object[] arguments = new Object[1];
            arguments[0] = i;
            a.startAgentInPlatform(aeroportox,"Aeroporto"+i, "Agents.Aeroporto",arguments);
            // Let all Aeroports be ready
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            for(int j = 0; j<num_avioes;j++){
                a.startAgentInPlatform(aeroportox,"Aviao"+((num_avioes*i)+j), "Agents.Aviao");
            }
        }
    }

    public ContainerController initContainerInPlatform(String host, String port, String containerName) {
        // Get the JADE runtime interface (singleton)
        this.rt = Runtime.instance();

        // Create a Profile, where the launch arguments are stored
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.CONTAINER_NAME, containerName);
        profile.setParameter(Profile.MAIN_HOST, host);
        profile.setParameter(Profile.MAIN_PORT, port);
        // create a non-main agent container
        ContainerController container = rt.createAgentContainer(profile);
        return container;
    }

    public void initMainContainerInPlatform(String host, String port, String containerName) {

        // Get the JADE runtime interface (singleton)
        this.rt = Runtime.instance();

        // Create a Profile, where the launch arguments are stored
        Profile prof = new ProfileImpl();
        prof.setParameter(Profile.CONTAINER_NAME, containerName);
        prof.setParameter(Profile.MAIN_HOST, host);
        prof.setParameter(Profile.MAIN_PORT, port);
        prof.setParameter(Profile.MAIN, "true");
        prof.setParameter(Profile.GUI, "true");

        // create a main agent container
        this.container = rt.createMainContainer(prof);
        rt.setCloseVM(true);
    }

    public void startAgentInPlatform(ContainerController container,String name, String classpath) {
        try {
                AgentController ac = container.createNewAgent(name, classpath, new Object[0]);
                ac.start();
        } catch (Exception e) {
                e.printStackTrace();
        }
    }
    public void startAgentInPlatform(ContainerController container,String name, String classpath,Object[] args) {
        try {
                AgentController ac = container.createNewAgent(name, classpath, args);
                ac.start();
        } catch (Exception e) {
                e.printStackTrace();
        }
    }
    
}
