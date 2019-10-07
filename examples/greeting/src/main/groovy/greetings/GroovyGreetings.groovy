package greetings

import org.freeplane.api.Controller
import org.freeplane.api.Node
import org.freeplane.core.ui.components.UITools

import javax.swing.JOptionPane
import java.text.MessageFormat

class GroovyGreetings {
    public void greet(Controller controller, Node node) throws Exception {
        final String greet = GreetPropertySupplier.greetProperty;
        String[] content = [node.plainText]
        final String message = new MessageFormat(greet).format(content);
        JOptionPane.showMessageDialog(UITools.currentFrame, message);
        controller.setStatusInfo("Greetings done");
    }
}
