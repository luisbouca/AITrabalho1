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
    private static int num_airports=3,num_airplanes=1;

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
            mainContainer.initContainerInPlatform("localhost", "9888", "Air");
            for(int i = 0; i <num_airports; i++)
                ContainerControllers.add(mainContainer.initContainerInPlatform("localhost", "9888", "Container"+i));

    }

    private static void CreatingAgents() throws Exception
    {

        //for each sub container, is necessary to add a single agent with airport role, and multiple agents with airplane role.
        for(ContainerController container : ContainerControllers)
        {
            
            //Arguments to be passed
            int airport_number = ContainerControllers.indexOf(container);
            Object[] arguments = new Object[2];
            arguments[0] = airport_number;
            arguments[1] = num_airports;
            
            
            //Creates a group of airplanes per subcontainer.
            for(int i = 0; i < num_airplanes; i++)
                mainContainer.startAgentInPlatform(container,"Airplane_"+((num_airplanes*airport_number)+i), "Agents.Airplane",arguments);
            
            //Creates a agent with airport role per subcontainer.
            mainContainer.startAgentInPlatform(container,"Airport_"+airport_number, "Agents.Airport",arguments);
        }
        
        //Starting agent interface in maincontainer.
            Object[] interfaceArgument = new Object[1];
            interfaceArgument[0] = num_airports;
        mainContainer.startAgentInPlatform(mainContainer.container,"Interface", "Agents.Interface", interfaceArgument);
    }
	
}
