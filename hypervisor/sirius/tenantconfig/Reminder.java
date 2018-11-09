package net.floodlightcontroller.sirius.tenantconfig;

import java.util.Timer;
import java.util.TimerTask;

import net.floodlightcontroller.sirius.tenantconfig.topology.VirtualNetwork;

public class Reminder {
    Timer timer;

    public Reminder(VirtualNetwork virtualNetwork, int seconds) {
        timer = new Timer();
        timer.schedule(new RemindTask(virtualNetwork), seconds*1000);
    }

    class RemindTask extends TimerTask {
    	
    	VirtualNetwork virtualNetwork;
    	
    	public RemindTask(VirtualNetwork virtualNetwork){
    		this.virtualNetwork = virtualNetwork;
    	}
    	
        public void run() {
            System.out.println("Time's up!"+virtualNetwork.isActive());
            virtualNetwork.setActive(false);
            System.out.println("Time's up2!"+virtualNetwork.isActive());
            timer.cancel(); //Terminate the timer thread
        }
    }

    public static void main(String args[]) {
    	VirtualNetwork vn = new VirtualNetwork(null,null, null, 11L, true, 7);
        //new Reminder(vn, 5);
        System.out.println("Task scheduled."+vn.isActive());
    }
}
