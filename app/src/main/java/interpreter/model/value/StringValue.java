package interpreter.model.value;

import interpreter.model.type.IType;
import interpreter.model.type.StringType;

/** A record, so equals/hashCode compare by content: a StringValue can be a key of the FileTable. */
public record StringValue(String val) implements IValue {
    @Override
    public IType getType() {
        return new StringType();
    }

    @Override
    public String toString() {
        return "\"" + val + "\"";
    }
}
