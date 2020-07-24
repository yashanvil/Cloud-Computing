import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.util.*;
import java.util.ArrayList;
import org.opennebula.client.Client;
import org.opennebula.client.OneResponse;
import org.opennebula.client.host.Host;
import org.opennebula.client.host.HostPool;
import org.opennebula.client.vm.VirtualMachine;
import org.opennebula.client.vm.VirtualMachinePool;

import java.text.DecimalFormat;
public class VMHostAllocation{
  private OneResponse rc;
	private static DecimalFormat df2 = new DecimalFormat(".##");


  public int getOptimalHost(Client oneClient)
  {
    ArrayList <HOSTPERF> arrHost = new ArrayList<HOSTPERF>();

    retrieveInformation(oneClient,arrHost);

    arrHost.sort(Comparator.comparingDouble(HOSTPERF::getCpuUsage));
    ArrayList<HOSTPERF> tempHost = new ArrayList<HOSTPERF>(arrHost.subList(0, 5));

    arrHost.sort(Comparator.comparingDouble(HOSTPERF::getMemUsage));
    ArrayList<HOSTPERF> tempHost2 = new ArrayList<HOSTPERF>(arrHost.subList(0, 5));

    // arrHost.sort(Comparator.comparingDouble(HOSTPERF::getDiskUsage));
    // ArrayList<HOSTPERF> tempHost3 = new ArrayList<HOSTPERF>(arrHost.subList(0, 5));


    tempHost.retainAll(tempHost2);
    //tempHost.retainAll(tempHost3);

    int optimalId;

    if(tempHost.isEmpty())
    {
        optimalId = arrHost.get(0).getID();
    }
    else
    {
        optimalId = tempHost.get(0).getID();
    }

    return optimalId;

  }


  public void retrieveInformation(Client oneClient, ArrayList<HOSTPERF> arrHost)
  {
    try
		{
			HostPool pool = new HostPool( oneClient );
			pool.info();

			double cpuUsage, memUsage, diskUsage;
			for( Host host: pool)
			{
				rc = host.info();
        cpuUsage = (Double.parseDouble(host.xpath("/HOST/HOST_SHARE/CPU_USAGE"))/Double.parseDouble(host.xpath("/HOST/HOST_SHARE/MAX_CPU")))*100;
				memUsage = (Double.parseDouble(host.xpath("/HOST/HOST_SHARE/MEM_USAGE"))/Double.parseDouble(host.xpath("/HOST/HOST_SHARE/MAX_MEM")))*100;
				diskUsage = (Double.parseDouble(host.xpath("/HOST/HOST_SHARE/DISK_USAGE"))/Double.parseDouble(host.xpath("/HOST/HOST_SHARE/MAX_DISK")))*100;
				int numVM = Integer.parseInt(host.xpath("/HOST/HOST_SHARE/RUNNING_VMS"));

				arrHost.add(new HOSTPERF(Integer.parseInt(host.xpath("/HOST/ID")), (host.xpath("/HOST/NAME")).toString(), cpuUsage * 0.6, memUsage*0.3, diskUsage, numVM));

			}

			System.out.println("Physical Hosts with resource usage:");
			System.out.println("HOSTID\tCPU Usage\tMem Usage\tDisk Usage\tVMs");
			for(HOSTPERF h: arrHost)
			{
				System.out.println(h.HOSTID + "\t" + df2.format(h.HostCpuUsage) +"\t\t" + df2.format(h.HostMemUsage) + "\t\t" + h.HostDiskUsage + "\t\t" + h.NumVM);
			}
			System.out.println();
		}catch(Exception e){
			System.out.println("Error viewing all of the Host info");
			e.printStackTrace();
		}
	}


	/*class of HOST*/
	public class HOSTPERF
	{
		int HOSTID;
		String HOSTNAME;
		double HostCpuUsage;
		double HostMemUsage;
		double HostDiskUsage;
		int NumVM;

		public HOSTPERF(int _hostID, String _hostName, double _cpuUsage, double _memUsage, double _diskUsage, int _numVM)
		{
			HOSTID = _hostID;
			HOSTNAME = _hostName;
			HostCpuUsage = _cpuUsage;
			HostMemUsage = _memUsage;
			HostDiskUsage = _diskUsage;
			NumVM = _numVM;
		}

		public int getID(){
			return HOSTID;
		}
		public String getName(){
			return HOSTNAME;
		}
		public double getCpuUsage(){
			return HostCpuUsage;
		}
		public double getMemUsage(){
			return HostMemUsage;
		}
		public double getDiskUsage(){
			return HostDiskUsage;
		}
		public int getNumVM(){
			return NumVM;
		}
	}

}
