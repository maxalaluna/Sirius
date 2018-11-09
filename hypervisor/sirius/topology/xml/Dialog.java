/**
 * SIRIUS - Testbed platform console 
 * Faculdade de Ciências da Universidade de Lisboa
 * Eric Vial (evial at lasige.di.fc.ul.pt)
 */

package net.floodlightcontroller.sirius.topology.xml;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

public class Dialog {

	private static final String[] OPENFLOW_STRINGS = { "OpenFlow10", "OpenFlow11", 
			"OpenFlow12", "OpenFlow13", "OpenFlow14", "OpenFlow15" };
	
    public static void errorDialog(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", 
        		JOptionPane.ERROR_MESSAGE);
    }
   
    public static void infoDialog(String message) {
        JOptionPane.showMessageDialog(null, message, "Information", 
        		JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static boolean starDialog(Config config) {
    	SpinnerNumberModel sizeModel = new SpinnerNumberModel(config.defaultStarSize, 2, 12, 1);
    	SpinnerNumberModel lengthModel = new SpinnerNumberModel(config.defaultStarLength, 0, 1000, 10);
    	SpinnerNumberModel startModel = new SpinnerNumberModel(config.defaultStarStart, 0, 360, 10);    	
    	SpinnerNumberModel endModel = new SpinnerNumberModel(config.defaultStarEnd, 0, 360, 10);    	
        final Object[] inputs = new Object[] {
        	new JLabel("Host number"), 
        	new JSpinner(sizeModel),
        	new JLabel("Branch length"), 
        	new JSpinner(lengthModel),
        	new JLabel("Start angle"), 
        	new JSpinner(startModel),
        	new JLabel("End angle"), 
        	new JSpinner(endModel),
    	};
    	int res = JOptionPane.showOptionDialog(null, inputs, "Add host star", 
    			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
    	if (res == JOptionPane.OK_OPTION) {
    		config.defaultStarSize = (int)sizeModel.getValue();
    		config.defaultStarLength = (int)lengthModel.getValue();
    		config.defaultStarStart = (int)startModel.getValue();
    		config.defaultStarEnd = (int)endModel.getValue();
    		return true;
    	}
    	return false;
    }
    
    public static double rotateDialog(int value) {
        SpinnerNumberModel model = new SpinnerNumberModel(value, 0, 360, 10);
        final JComponent[] inputs = new JComponent[] {
   			new JLabel("Angle in degree"),
   			new JSpinner(model)
    	};
    	int res = JOptionPane.showOptionDialog(null, inputs, "Node rotation", 
    			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
    	if (res == JOptionPane.OK_OPTION)
    		return ((int)model.getValue() * Math.PI) / 180;
    	return 0;
    }
    
    private static JSpinner newSpinner(SpinnerModel model) {
    	JSpinner spinner = new JSpinner(model);
    	JComponent editor = spinner.getEditor();
    	((JSpinner.DefaultEditor)editor).getTextField()
    			.setHorizontalAlignment(JTextField.LEFT);
    	return spinner;
    }
    
    public static int hullDialog(Config config) {
    	SpinnerModel cloudField = new SpinnerNumberModel(0, 0, config.cloudMaxNumber, 1);
        final Object[] inputs = new Object[] { new JLabel("Cloud id"), newSpinner(cloudField) };
    	int res = JOptionPane.showOptionDialog(null, inputs, "Edit cloud hulls", 
    			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
    	if (res == JOptionPane.OK_OPTION)
    		return (int)cloudField.getValue();
    	return -1;
    }

    public static boolean switchDialog(Config config, Switch node) {
    	JTextField nameField = new JTextField(node.getName());
    	JTextField ipField = new JTextField(node.getIp());
    	SpinnerModel cpuField = new SpinnerNumberModel(node.getCpu(), 1, 100, 1);
    	SpinnerModel memField = new SpinnerNumberModel(node.getMem(), 1, 100, 1);
    	JComboBox<String> cloudCombo = new JComboBox<String>(config.getCloudNames(config));
    	JComboBox<String> versionCombo = new JComboBox<String>(OPENFLOW_STRINGS);
    	SpinnerModel flowField = new SpinnerNumberModel(node.getFlows(), 1, 10000, 10);
    	SpinnerModel secureField = new SpinnerNumberModel(node.getSecurity(), 0, 100, 1);
    	SpinnerModel dependField = new SpinnerNumberModel(node.getDependability(), 0, 100, 1);
    	SpinnerModel trustField = new SpinnerNumberModel(node.getCloudType(), 0, 2, 1);
    	versionCombo.setSelectedIndex(node.getVersion());
    	cloudCombo.setSelectedIndex(node.getCloud());
        final Object[] inputs = new Object[] {
	        	new JLabel("Node name"), nameField,
	        	new JLabel("Ip address"), ipField,
	        	new JLabel("CPU cores"), newSpinner(cpuField),
	        	new JLabel("RAM memory"), newSpinner(memField),
	        	new JLabel("Cloud id"), cloudCombo,
	   			new JLabel("OpenFlow version"), versionCombo,
	   			new JLabel("Maximum flow number"), newSpinner(flowField),
	   			new JLabel("Security level"), newSpinner(secureField),
	   			new JLabel("Dependability level"), newSpinner(dependField),
	   			new JLabel("Cloud type"), newSpinner(trustField),
	    	};
        if (node.getMapping() > 0)
        	for (Object input : inputs) ((JComponent)input).setEnabled(false);
    	int res = JOptionPane.showOptionDialog(null, inputs, "Edit switch " + node.getName(), 
    			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
    	if (res == JOptionPane.OK_OPTION) {
    		String name = nameField.getText();
    		String ip = ipField.getText();
    		int cpu = (int)cpuField.getValue();
    		int mem = (int)memField.getValue();
    		int cloud = cloudCombo.getSelectedIndex();
    		int version = versionCombo.getSelectedIndex();
    		int flows = (int)flowField.getValue();
    		int secure = (int)secureField.getValue();
    		int depend = (int)dependField.getValue();
    		int trust = (int)trustField.getValue();
    		
    		// Update node's attributes
    		node.setName(name); 
    		node.setIp(ip); 
    		node.setCpu(cpu);
    		node.setMem(mem);
   			node.setCloud(cloud);
    		node.setVersion(version);
    		node.setFlows(flows);
    		node.setSecurity(secure);
    		node.setDependability(depend);
   			node.setCloudType(trust);
    		return true;
    	}
    	return false;
    }
        
    public static boolean hostDialog(Config config, Host node) {
       	JTextField nameField = new JTextField(node.getName());
    	JTextField ipField = new JTextField(node.getIp());
    	SpinnerModel cpuField = new SpinnerNumberModel(node.getCpu(), 1, 100, 1);
    	SpinnerModel memField = new SpinnerNumberModel(node.getMem(), 1, 100, 1);
    	JComboBox<String> cloudCombo = new JComboBox<String>(config.getCloudNames(config));
    	JTextField macField = new JTextField(node.getMac());
    	cloudCombo.setSelectedIndex(node.getCloud());
        final Object[] inputs = new Object[] {
	        	new JLabel("Node name"), nameField,
	           	new JLabel("Ip address"), ipField,
	           	new JLabel("CPU cores"), newSpinner(cpuField),
	           	new JLabel("RAM memory"), newSpinner(memField),
	           	new JLabel("Cloud id"), cloudCombo,	
	           	new JLabel("MAC address"), macField
	    };
        if (node.getMapping() > 0)
        	for (Object input : inputs) ((JComponent)input).setEnabled(false);
    	int res = JOptionPane.showOptionDialog(null, inputs, "Edit host " + node.getName(), 
    			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
    	if (res == JOptionPane.OK_OPTION) {
    		String name = nameField.getText();
    		String ip = ipField.getText();
    		int cpu = (int)cpuField.getValue();
    		int mem = (int)memField.getValue();
    		int cloud = cloudCombo.getSelectedIndex();
    		String mac = macField.getText();
    		
    		// Update node's attributes
    		node.setName(name); 
    		node.setIp(ip); 
    		node.setCpu(cpu);
    		node.setMem(mem);
   			node.setCloud(cloud);
    		if (!mac.isEmpty()) node.setMac(mac);
    		return true;
    	}
    	return false;
    }
    
    public static boolean controllerDialog(Config config, Controller node) {
       	JTextField nameField = new JTextField(node.getName());
    	JTextField ipField = new JTextField(node.getIp());
    	SpinnerModel cpuField = new SpinnerNumberModel(node.getCpu(), 1, 100, 1);
    	SpinnerModel memField = new SpinnerNumberModel(node.getMem(), 1, 100, 1);
    	JComboBox<String> cloudCombo = new JComboBox<String>(config.getCloudNames(config));
    	SpinnerModel portField = new SpinnerNumberModel(node.getPort(), 1, 10000, 1);
    	cloudCombo.setSelectedIndex(node.getCloud());
        final Object[] inputs = new Object[] {
        		new JLabel("Node name"), nameField,
               	new JLabel("Ip address"), ipField,
               	new JLabel("CPU cores"), newSpinner(cpuField),
               	new JLabel("RAM memory"), newSpinner(memField),
               	new JLabel("Cloud id"), cloudCombo,	
               	new JLabel("Listening port"), newSpinner(portField),
    	};
    	int res = JOptionPane.showOptionDialog(null, inputs, "Edit controller " + node.getName(), 
    			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
    	if (res == JOptionPane.OK_OPTION) {
    		String name = nameField.getText();
    		String ip = ipField.getText();
       		int cpu = (int)cpuField.getValue();
    		int mem = (int)memField.getValue();
    		int cloud = cloudCombo.getSelectedIndex();
    		int port = (int)portField.getValue();
    		
    		// Update node's attributes
    		node.setName(name); 
    		node.setIp(ip); 
    		node.setCpu(cpu);
    		node.setMem(mem);
   			node.setCloud(cloud);
    		node.setPort(port);
    		return true;
    	}
    	return false;
    }

    public static boolean linkDialog(Config config, Link link) {
    	SpinnerModel bandModel = new SpinnerNumberModel(link.getBand(), 1, 10000, 1);
    	SpinnerModel delayModel = new SpinnerNumberModel(link.getDelay(), 0, 1000, 1);
    	SpinnerModel lossModel = new SpinnerNumberModel(link.getLoss(), 0, 100, 1);
    	SpinnerModel secureModel = new SpinnerNumberModel(link.getSecurity(), 0, 100, 1);
        final Object[] inputs = new Object[] {
	   			new JLabel("Bandwidth"), newSpinner(bandModel),
	   			new JLabel("Link delay"), newSpinner(delayModel),
	   			new JLabel("Loss rate"), newSpinner(lossModel),
	   			new JLabel("Security level"), newSpinner(secureModel)
    	};
    	int res = JOptionPane.showOptionDialog(null, inputs, "Edit link", 
    			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
    	if (res == JOptionPane.OK_OPTION) {
    		int band = (int)bandModel.getValue();
    		int delay = (int)delayModel.getValue();
    		int loss = (int)lossModel.getValue();
    		int secure = (int)secureModel.getValue();
    		
    		// Update link's attributes
    		link.setBand(band); 
    		link.setDelay(delay);
    		link.setLoss(loss);
    		link.setSecurity(secure);
    		return true;
    	}
    	return false;
    }
 
    private static JPanel createButton(final JOptionPane optionPane, 
    		final int index, Icon icon, String label) {
    	final JButton button = new JButton(icon);
    	ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
    			optionPane.setValue(index);
    		}
        };
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setPreferredSize(new Dimension(100, 100));
        button.addActionListener(actionListener);
        button.setText(label);
        panel.add(button);
        return panel;
    }
    
    public static int nodeDialog(Applet applet) {
        JOptionPane pane = new JOptionPane();
    	JPanel grid = new JPanel();
    	grid.add(createButton(pane, 1, applet.loadIcon("host", 32, 32), "Host"));
    	grid.add(createButton(pane, 2, applet.loadIcon("switch", 32, 32), "Switch"));
    	pane.setMessage("Select node type");
        pane.setMessageType(JOptionPane.PLAIN_MESSAGE);
        pane.setOptions(new Object[] { grid });
        JDialog dialog = pane.createDialog(null, "Node creation");
        dialog.setVisible(true);
        Object res = pane.getValue();
        if (res == null) return 0;
        return (int)res;
    }
    
    public static String contextDialog(String[] lst) {
    	return (String)JOptionPane.showInputDialog(null, "Select previous action:",
    			"Context restoration", JOptionPane.PLAIN_MESSAGE,
    			null, lst, lst[0]);
    }
    
    public static String loadDialog(String[] files) {
    	return (String)JOptionPane.showInputDialog(null, "Select configuration file:",
    			"Load configuration file", JOptionPane.PLAIN_MESSAGE,
    			null, files, files[0]);
    }
    
    public static String saveDialog(String file) {
    	return (String)JOptionPane.showInputDialog(null, "Enter configuration file:",
    			"Save configuration file", JOptionPane.PLAIN_MESSAGE,
    			null, null, file);
    }
    
    public static boolean confirmDialog(String message) {
    	int res = JOptionPane.showConfirmDialog (null, message,
    			"Warning", JOptionPane.YES_NO_OPTION);
    	return res == JOptionPane.YES_OPTION;
    }
    
    public static void aboutDialog(Applet applet) {
    	JOptionPane.showMessageDialog(null, "Sirius Admin Console\n"
    			+ "Eric Vial (evial@lasige.di.fc.ul.pt", "About",
    			JOptionPane.INFORMATION_MESSAGE,
    			applet.loadIcon("logo", 128, 60));
    }
}
