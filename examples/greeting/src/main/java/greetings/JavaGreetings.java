package greetings;

import org.freeplane.api.Controller;
import org.freeplane.api.Node;
import org.freeplane.core.ui.components.UITools;

import javax.swing.*;
import java.text.MessageFormat;

public class JavaGreetings {
    public void greet(Controller controller, Node node) throws Exception {
        final String greet = GreetPropertySupplier.getGreetProperty();
        final String[] content = {node.getPlainText()};
        final String message = new MessageFormat(greet).format(content);
        JOptionPane.showMessageDialog(UITools.getCurrentFrame(), message);
        controller.setStatusInfo("Greetings done");
    }

}
