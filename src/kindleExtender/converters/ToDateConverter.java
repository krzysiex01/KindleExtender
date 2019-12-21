package kindleExtender.converters;

import javafx.util.StringConverter;

import java.util.Date;

public class ToDateConverter extends StringConverter<Long> {

    @Override
    public String toString(Long aLong) {
        return new Date(aLong).toString();
    }

    @Override
    public Long fromString(String s) {
        return null;
    }
}
