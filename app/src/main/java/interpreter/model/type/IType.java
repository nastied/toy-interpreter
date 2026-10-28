package interpreter.model.type;

import interpreter.model.value.IValue;

public interface IType {
    /** The value a freshly declared variable of this type gets (rule S3). */
    IValue defaultValue();
}
