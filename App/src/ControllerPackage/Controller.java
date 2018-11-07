package ControllerPackage;
import ControllerPackage.MainContainer;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import jdk.nashorn.internal.objects.NativeArray;

public class Controller {
    
   
    private static MainContainer mainContainer; 
    private static List<ContainerController> ContainerControllers; 

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
        mainContainer.initContainerInPlatform("localhost", "9888", "MainContainer");
    }

    private static void CreateContainers() throws Exception
    {
        ContainerControllers = new ArrayList<>();

            //Creats a random number of subcontainers.
            for(int i = 0; i < new Random().nextInt(5 + 1); i++)
                ContainerControllers.add(mainContainer.initContainerInPlatform("localhost", "9888", "Container"+i));

    }

    private static void CreatingAgents() throws Exception
    {
        //Starting agent interface in maincontainer.
        mainContainer.startAgentInPlatform("AgentInterface", "AgentsPackage.Interface");

        //for each sub container, is necessary to add a single agent with airport role, and multiple agents with airplane role.
        for(ContainerController container : ContainerControllers)
        {
            //Adding a single agent with airport role.
            container.createNewAgent("Airport_"+ContainerControllers.indexOf(container), "AgentsPackage.Airport", new Object[] {}).start();

            //Creats random number of airplanes per sub container.
            for(int i = 0; i < new Random().nextInt(5 + 1); i++)
                container.createNewAgent("Airplane_"+i, "AgentsPackage.Airplane", new Object[] {}).start();
        }
    }
	
}
