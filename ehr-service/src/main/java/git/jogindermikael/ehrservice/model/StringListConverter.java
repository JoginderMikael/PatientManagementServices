package git.jogindermikael.ehrservice.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Override public String convertToDatabaseColumn(List<String> value) {
        try { return MAPPER.writeValueAsString(value == null ? List.of() : value); }
        catch (Exception exception) { throw new IllegalArgumentException("Invalid clinical list", exception); }
    }
    @Override public List<String> convertToEntityAttribute(String value) {
        try { return value == null ? List.of() : MAPPER.readValue(value, new TypeReference<>() {}); }
        catch (Exception exception) { throw new IllegalArgumentException("Invalid persisted clinical list", exception); }
    }
}
