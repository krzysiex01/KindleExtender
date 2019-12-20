package kindleExtender.converters;

import javafx.util.StringConverter;
import javafx.util.converter.*;

public class ToLowerConverter extends StringConverter<String> {
    @Override
    public String toString(String s) {
        return s.toLowerCase();
    }

    @Override
    public String fromString(String s) {
        return s;
    }
}
