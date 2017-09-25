package us.deathmarine.luyten;

import java.awt.*;
import org.fife.ui.rsyntaxtextarea.*;
import java.awt.event.*;
import org.fife.ui.rtextarea.*;
import javax.swing.*;

public class FindBox extends JDialog
{
    private static final long serialVersionUID = -4125409760166690462L;
    private JCheckBox mcase;
    private JCheckBox regex;
    private JCheckBox wholew;
    private JCheckBox reverse;
    private JButton findButton;
    private JTextField textField;
    private MainWindow mainWindow;
    
    public void showFindBox() {
        this.setVisible(true);
        this.textField.requestFocus();
    }
    
    public void hideFindBox() {
        this.setVisible(false);
    }
    
    public FindBox(final MainWindow mainWindow) {
        super();
        this.mainWindow = mainWindow;
        this.setDefaultCloseOperation(1);
        this.setHideOnEscapeButton();
        final JLabel label = new JLabel("Find What:");
        this.textField = new JTextField();
        final RSyntaxTextArea pane = mainWindow.getModel().getCurrentTextArea();
        if (pane != null) {
            this.textField.setText(pane.getSelectedText());
        }
        this.mcase = new JCheckBox("Match Case");
        this.regex = new JCheckBox("Regex");
        this.wholew = new JCheckBox("Whole Words");
        this.reverse = new JCheckBox("Search Backwards");
        (this.findButton = new JButton("Find")).addActionListener(new FindButton((FindButton)null));
        this.getRootPane().setDefaultButton(this.findButton);
        this.mcase.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        this.regex.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        this.wholew.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        this.reverse.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension center = new Dimension((int)(screenSize.width * 0.35), Math.min((int)(screenSize.height * 0.2), 200));
        final int x = (int)(center.width * 0.2);
        final int y = (int)(center.height * 0.2);
        this.setBounds(x, y, center.width, center.height);
        this.setResizable(false);
        final GroupLayout layout = new GroupLayout(this.getRootPane());
        this.getRootPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(label).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.textField).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.mcase).addComponent(this.wholew)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.regex).addComponent(this.reverse)))).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.findButton)));
        layout.linkSize(0, this.findButton);
        layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(label).addComponent(this.textField).addComponent(this.findButton)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.mcase).addComponent(this.regex)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.wholew).addComponent(this.reverse)))));
        this.adjustWindowPositionBySavedState();
        this.setSaveWindowPositionOnClosing();
        this.setName("Find");
        this.setTitle("Find");
        this.setVisible(true);
    }
    
    private void setHideOnEscapeButton() {
        final Action escapeAction = new AbstractAction() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void actionPerformed(final ActionEvent e) {
                FindBox.this.setVisible(false);
            }
        };
        final KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(27, 0, false);
        this.getRootPane().getInputMap(2).put(escapeKeyStroke, "ESCAPE");
        this.getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }
    
    private void adjustWindowPositionBySavedState() {
        final WindowPosition windowPosition = ConfigSaver.getLoadedInstance().getFindWindowPosition();
        if (windowPosition.isSavedWindowPositionValid()) {
            this.setLocation(windowPosition.getWindowX(), windowPosition.getWindowY());
        }
    }
    
    private void setSaveWindowPositionOnClosing() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeactivated(final WindowEvent e) {
                final WindowPosition windowPosition = ConfigSaver.getLoadedInstance().getFindWindowPosition();
                windowPosition.readPositionFromDialog(FindBox.this);
            }
        });
    }
    
    static /* synthetic */ JTextField access$0(final FindBox param_0) {
        return param_0.textField;
    }
    
    static /* synthetic */ MainWindow access$1(final FindBox param_0) {
        return param_0.mainWindow;
    }
    
    static /* synthetic */ JCheckBox access$2(final FindBox param_0) {
        return param_0.mcase;
    }
    
    static /* synthetic */ JCheckBox access$3(final FindBox param_0) {
        return param_0.regex;
    }
    
    static /* synthetic */ JCheckBox access$4(final FindBox param_0) {
        return param_0.reverse;
    }
    
    static /* synthetic */ JCheckBox access$5(final FindBox param_0) {
        return param_0.wholew;
    }
    
    private class FindButton extends AbstractAction
    {
        private static final long serialVersionUID = 75954129199541874L;
        
        @Override
        public void actionPerformed(final ActionEvent event) {
            if (FindBox.access$0(FindBox.this).getText().length() == 0) {
                return;
            }
            final RSyntaxTextArea pane = FindBox.access$1(FindBox.this).getModel().getCurrentTextArea();
            if (pane == null) {
                return;
            }
            final SearchContext context = new SearchContext();
            context.setSearchFor(FindBox.access$0(FindBox.this).getText());
            context.setMatchCase(FindBox.access$2(FindBox.this).isSelected());
            context.setRegularExpression(FindBox.access$3(FindBox.this).isSelected());
            context.setSearchForward(!FindBox.access$4(FindBox.this).isSelected());
            context.setWholeWord(FindBox.access$5(FindBox.this).isSelected());
            if (!SearchEngine.find(pane, context).wasFound()) {
                pane.setSelectionStart(0);
                pane.setSelectionEnd(0);
            }
        }
    }
}
