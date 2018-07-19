package utils;

import utils.Utils.TypeCast;

// ------------------------------------------------------------
//                             SELF
// ------------------------------------------------------------
// Support mix-in composition for self bounded types.
//
public interface Self<T extends Self<T>> {

    default T self() {

        return TypeCast.<T> Of(Object.class).cast(this);
    }
}
