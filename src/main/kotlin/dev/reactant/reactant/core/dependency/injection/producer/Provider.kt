package dev.reactant.reactant.core.dependency.injection.producer

import dev.reactant.reactant.core.component.container.Container
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.jvmErasure

/**
 * The producer info for resolve loading order
 */
interface Provider {
    val productType: KType;
    /**
     * Regex pattern to match name
     */
    val namePattern: String;

    /**
     * Indicate is this producer available
     * Null if available
     */
    val disabledReason: Throwable?;

    val producer: (requestedType: KType, requestedName: String, requester: Provider) -> Any?

    /**
     * Skip generic check when determining can it provide as a type.
     * Useful if the provider is going to dynamic generate injectable by generic type
     */
    val ignoreGenerics: Boolean
        get() = false

    fun canProvideType(requiredKType: KType) =
            if (ignoreGenerics) productType.jvmErasure.isSubclassOf(requiredKType.jvmErasure)
            else productType.isSubtypeOf(requiredKType)

    /**
     * The parent container of this provider
     */
    val container: Container
}
