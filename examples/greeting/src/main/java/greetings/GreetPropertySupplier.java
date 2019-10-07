package greetings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class GreetPropertySupplier {
    static String getGreetProperty() throws IOException {
        final Properties properties = new Properties();
        try (final InputStream resource = GreetPropertySupplier.class.getResourceAsStream("greet.properties")) {
            properties.load(resource);
        }
        return properties.getProperty("greet");
    }
}
