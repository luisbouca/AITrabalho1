package Controller;

import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import jdk.nashorn.internal.objects.NativeArray;

public class Controller {
    
   
    private static MainContainer mainContainer; 
    private static List<ContainerController> ContainerControllers; 
    private static int num_airports=10,num_airplanes=3;

    public static void main(String[] args) 
    {
        try
        {      
            //Initial configuration methods.  
            CreateMainContainer();
            CreateContainers();
            CreatingAgents();
        }
        catch(Exception ex)
        {
            System.console().printf("Exception: "+ex.getMessage());
        }
    }

    private static void CreateMainContainer()               
    {  
        mainContainer = new MainContainer();
        mainContainer.initMainContainerInPlatform("localhost", "9888", "MainContainer");
    }

    private static void CreateContainers() throws Exception
    {
        ContainerControllers = new ArrayList<>();

            //Creats a random number of subcontainers.
            for(int i = 0; i <num_airports; i++)
                ContainerControllers.add(mainContainer.initContainerInPlatform("localhost", "9888", "Container"+i));

    }

    private static void CreatingAgents() throws Exception
    {
        //Starting agent interface in maincontainer.
        mainContainer.startAgentInPlatform(mainContainer.container,"Interface", "Agents.Interface");

        //for each sub container, is necessary to add a single agent with airport role, and multiple agents with airplane role.
        for(ContainerController container : ContainerControllers)
        {
            int airport_number = ContainerControllers.indexOf(container);
            
            //Creates a group of airplanes per subcontainer.
            for(int i = 0; i < num_airplanes; i++)
                mainContainer.startAgentInPlatform(container,"Airplane_"+((num_airplanes*airport_number)+i), "Agents.Airplane");
            
            // Let all Airplanes be ready
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            //Arguments to be passed to the airport
            Object[] arguments = new Object[1];
            arguments[0] = airport_number;
            //Creates a agent with airport role per subcontainer.
            mainContainer.startAgentInPlatform(container,"Airport_"+airport_number, "Agents.Airport",arguments);
        }
    }
	
}
