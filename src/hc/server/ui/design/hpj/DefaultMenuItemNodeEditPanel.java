package hc.server.ui.design.hpj;

import hc.core.util.HCURL;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class DefaultMenuItemNodeEditPanel extends BaseMenuItemNodeEditPanel {
	
	private JPanel myCommand_Panel = new JPanel();
	private JPanel noJRubyTip = new JPanel();
	private JPanel centerPanel;
	
	public DefaultMenuItemNodeEditPanel() {
		super();
		
		addTargetURLPanel();
		
		myCommand_Panel.setLayout(new BorderLayout());
		myCommand_Panel.add(jtascriptPanel, BorderLayout.CENTER);
		
		noJRubyTip.setLayout(new BorderLayout());
		noJRubyTip.add(new JLabel("There is no other editable content for current type item."), BorderLayout.NORTH);

		setLayout(new BorderLayout());
		add(iconPanel, BorderLayout.NORTH);

		centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(noJRubyTip, BorderLayout.CENTER);
		
		add(centerPanel, BorderLayout.CENTER);
	}
	
	private void flip_cmd_screen(int type){
		final String element = hcurl.elementID;
		
		if(type == HPNode.TYPE_MENU_ITEM_SCREEN){
			if(element.equals(HCURL.REMOTE_HOME_SCREEN)){
				flipCommandPanel(false);
			}else{
				flipCommandPanel(true);
			}
		}else if(type == HPNode.TYPE_MENU_ITEM_CMD){
			if(element.equals(HCURL.DATA_CMD_EXIT)){
				flipCommandPanel(false);
			}else if(element.equals(HCURL.DATA_CMD_CONFIG)){
				flipCommandPanel(false);
			}else{
				flipCommandPanel(true);
			}
		}
		
		if(myCommand_Panel.isVisible()){
			initScript();
		}
	}

	private void flipCommandPanel(final boolean v) {
		centerPanel.removeAll();
		if(v){
			centerPanel.add(myCommand_Panel, BorderLayout.CENTER);
		}else{
			centerPanel.add(noJRubyTip, BorderLayout.CENTER);
		}
		
		cmd_url_panel.setVisible(v);
	}
	
	public void addTargetURLPanel(){
		cmd_url_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		cmd_url_panel.add(targetLoca);
		cmd_url_panel.add(jtfMyCommand);
		cmd_url_panel.add(testBtn);
		cmd_url_panel.add(errCommandTip);
	}

	protected void extInit(){
		flip_cmd_screen(currItem.type);
	}
}