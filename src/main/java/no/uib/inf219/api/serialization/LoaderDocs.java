package no.uib.inf219.api.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoaderDocs {

    /**
     * @return All attributes for this {@link no.kh498.valentineRealms.core.parts.loader.Loader}
     */
    LoaderDoc[] value();
}
