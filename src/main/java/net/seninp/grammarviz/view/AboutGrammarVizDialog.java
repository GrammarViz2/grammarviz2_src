package net.seninp.grammarviz.view;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import net.miginfocom.swing.MigLayout;

public class AboutGrammarVizDialog extends JDialog implements ActionListener {

  private static final long serialVersionUID = -8273240552350932580L;

  private static final String OK_BUTTON_TEXT = "OK";

private static final Object CR = "\n";
  
  public AboutGrammarVizDialog(JFrame parentFrame) {

    super(parentFrame, true);
    if (parentFrame != null) {
      Dimension parentSize = parentFrame.getSize();
      Point p = parentFrame.getLocation();
      setLocation(p.x + parentSize.width / 4, p.y + parentSize.height / 4);
    }

    StringBuffer text = new StringBuffer();
    text.append("<html>\n").append(CR);
    text.append("<h2>GrammarViz 3.0</h2>").append(CR);
    text.append("<h4>Version: 3.0. <b>beta.09.99</b>, February, 2017</h4>").append(CR);
    text.append("<b>(c) Copyright:</b><br>").append(CR);
    text.append("<p><b>Pavel Senin</b>, Pavel Senin, Los Alamos National Laboratory, MS M888, Los Alamos, NM 87545, USA. Email: <a href=\"mailto:#\">seninp@gmail.com</a></p>").append(CR);
    text.append("<p><b>Jessica Lin, Xing Wang</b>, George Mason University, Department of Computer Science.</p>").append(CR);
    text.append("<p><b>Arnold P. Boedihardjo, CrystalChen, Susan Frankenstein</b>, U.S. Army Corps of Engineers, Engineer Research and Development Center.</p>").append(CR);
    text.append("<p><b>Tim Oates, Sunil Gandhi</b>, University of Maryland, Baltimore County, Dept. of Computer Science</p>").append(CR);
    text.append("<br>").append(CR);
    text.append("<p>Visit <a href=\"http://grammarviz2.github.io/grammarviz2_site\">GrammarViz 3.0 site.</a></p>").append(CR);
    text.append("<br>").append(CR);
    text.append("The GrammarViz 3.0 software is released under <a href=\"http://www.gnu.org/licenses/gpl-2.0.html\">GNU GPL v2. license</a>").append(CR);
    text.append("</html>").append(CR);
    
    JTextPane aboutTextPane = new JTextPane();

    aboutTextPane.setEditable(false);
    aboutTextPane.setContentType("text/html");
    aboutTextPane.setText(text.toString());
  
    // Put the editor pane in a scroll pane.
    JScrollPane editorScrollPane = new JScrollPane(aboutTextPane);
    editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    MigLayout mainFrameLayout = new MigLayout("fill", "[grow,center]", "[grow]5[]");

    getContentPane().setLayout(mainFrameLayout);

    getContentPane().add(editorScrollPane, "h 200:300:,w 400:500:,growx,growy,wrap");

    JPanel buttonPane = new JPanel();
    JButton okButton = new JButton(OK_BUTTON_TEXT);

    buttonPane.add(okButton);
    okButton.addActionListener(this);

    getContentPane().add(buttonPane, "wrap");

    pack();
    setVisible(true);

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (OK_BUTTON_TEXT.equalsIgnoreCase(e.getActionCommand())) {
      this.dispose();
    }
  }

  /** This method clears the dialog and hides it. */
  public void clearAndHide() {
    setVisible(false);
  }
}
