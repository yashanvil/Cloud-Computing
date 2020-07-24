
// TEST

import org.opennebula.client.Client;
import org.opennebula.client.OneResponse;
import org.opennebula.client.host.Host;
import org.opennebula.client.host.HostPool;
import org.opennebula.client.vm.VirtualMachine;
import org.opennebula.client.vm.VirtualMachinePool;


public class VMtest{

  public Client logIntoCloud() {

		String passwd;
		Client oneClient = null;
		System.out.println("Enter your password: ");
		String username = System.getProperty("user.name");
		passwd = new String(System.console().readPassword("[%s]", "Password:"));
    try
		{
			oneClient = new Client(username + ":" + passwd, "https://csgate1.leeds.ac.uk:2633/RPC2");
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			System.out.println("Incorrect Password. Program Closing.");
			System.exit(1);
		}
		return oneClient;
  }

  public static void main(String[] args){
    	VMtest VMSample = new VMtest();
      Client oneClient = VMSample.logIntoCloud();
      String vmTemplate ="CPU=\"0.1\"\n"
                    + "SCHED_DS_REQUIREMENTS=\"ID=101\"\n"
                    + "NIC=[\n"
                    + "\tNETWORK_UNAME=\"oneadmin\",\n"
                    + "\tNETWORK=\"vnet1\" ]\n"
                    + "DISK=[\n"
                    + "\tIMAGE_UNAME=\"oneadmin\",\n"
                    + "\tIMAGE=\"ttylinux Base\" ]\n"
                    + "SUNSTONE_NETWORK_SELECT=\"YES\"\n"
                    + "SUNSTONE_CAPACITY_SELECT=\"YES\"\n"
                    + "MEMORY=\"128\"\n"
                    + "HYPERVISOR=\"kvm\"\n"
                    + "GRAPHICS=[\n"
                    + "\tLISTEN=\"0.0.0.0\",\n"
                    + "\tTYPE=\"VNC\" ]\n";

  System.out.print("Trying to allocate the virtual machine... ");

  OneResponse rc = VirtualMachine.allocate(oneClient, vmTemplate);
  int newVMID = Integer.parseInt(rc.getMessage());

  System.out.println("A new VM with VM ID " + newVMID + "has been created.");
  VirtualMachine vm = new VirtualMachine(newVMID, oneClient);

  //Deploying for hostID
    int firstHostID = getHostID(oneClient);

    if(firstHostID != 0){
      vm.deploy(firstHostID);
      System.out.println("\nVM with ID: " + newVMID + " has been deployed at Host with ID: "+ firstHostID);
    }
    else{
      System.out.println("Error while checking the HostID and deploying the VM");
    }

    rc = vm.info();

    //checking for running status
    while(vm.status() != "runn" )
    {
      rc = vm.info();
    }

    System.out.println("The new VM " +
            vm.getName() + " has status: " + vm.status());
    System.out.println("The path of the disk is: " + vm.xpath("TEMPLATE/DISK/SOURCE"));

    //Migrating VM to an optimal Host
    VMHostAllocation findResource = new VMHostAllocation();


    int hostID = findResource.getOptimalHost(oneClient);

    long startTime = System.currentTimeMillis();
    if (hostID!= firstHostID) {
      rc = vm.migrate(hostID,true);


      System.out.println("Migration Successful at HostID:" + hostID);
    }
    else
    {
      System.out.println("VM already running on an optimal host");
    }

    long endTime = System.currentTimeMillis();

    long migrating = endTime - startTime;

    System.out.println("Time taken for VM to migrate:  ");
    System.out.printf("%d%n",migrating);

    vm = null;

    VirtualMachinePool vmPool = new VirtualMachinePool(oneClient);
    rc = vmPool.info();
    System.out.println(
            "\nThese are all the Virtual Machines in the pool:");
    for ( VirtualMachine vmachine : vmPool )
    {
        System.out.println("\tID :" + vmachine.getId() +
                           ", Name :" + vmachine.getName() );


        if ( vmachine.getId().equals( ""+ newVMID ) )
        {
            vm = vmachine;
        }
    }
  }

  public static int getHostID(Client oneClient)
  {
    HostPool pool = new HostPool(oneClient);
    pool.info();

    OneResponse rc;
    int min_number_VM = 100;
    int hostID = 1000;

    try{
      for (Host host : pool ) {

        rc = host.info();
        int number_of_VM = Integer.parseInt(host.xpath("/HOST/HOST_SHARE/RUNNING_VMS"));

        if(number_of_VM !=0 && number_of_VM < min_number_VM )
        {
          min_number_VM = number_of_VM;
          hostID = Integer.parseInt(host.xpath("/HOST/ID"));
        }
      }
    }
    catch (Exception e)
    {
      System.out.println("Error while retriving the hostID");
    }
    return hostID;
  }

  public static void printVMachinePool (VirtualMachinePool vmPool)
  {
      System.out.println("--------------------------------------------");
      System.out.println("Number of VMs: " + vmPool.getLength());
      System.out.println("User ID\t\tName\t\tEnabled");

      for( VirtualMachine vm : vmPool )
      {
          String id = vm.getId();
          String name = vm.getName();
          String enab = vm.xpath("enabled");

          System.out.println(id+"\t\t"+name+"\t\t"+enab);
      }

      System.out.println("--------------------------------------------");
  }

}
