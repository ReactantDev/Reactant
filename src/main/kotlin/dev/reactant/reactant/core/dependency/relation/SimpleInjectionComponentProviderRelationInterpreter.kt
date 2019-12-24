package dev.reactant.reactant.core.dependency.relation

import dev.reactant.reactant.core.dependency.injection.InjectRequirement
import dev.reactant.reactant.core.dependency.injection.producer.ComponentProvider
import dev.reactant.reactant.core.dependency.injection.producer.Provider
import dev.reactant.reactant.core.exception.ProviderRequirementCannotFulfilException

/**
 * Handle the direct inject requirement, which mean the requiring type is not nullable or with any type arugment
 */
open class SimpleInjectionComponentProviderRelationInterpreter : ProviderRelationInterpreter {
    override fun interpret(interpretTarget: Provider, providers: Set<Provider>): Set<InterpretedProviderRelation>? {
        if (interpretTarget !is ComponentProvider<*>) return null

        return filterInterpretableRequirements(interpretTarget)
                .map { requirement ->
                    solve(interpretTarget, providers, requirement).let { solution ->
                        InterpretedProviderRelation(
                                this, interpretTarget, solution,
                                "Solution that solve the direct relation from the providers list",
                                setOf(requirement to solution)
                        )
                    }
                }.toSet()
    }

    protected open fun filterInterpretableRequirements(interpretTarget: ComponentProvider<*>): List<InjectRequirement> {
        return interpretTarget.constructorInjectRequirements
                .union(interpretTarget.propertiesInjectRequirements.map { it.value })
                .filter { isRequirementInterpretable(it) }
    }

    protected open fun isRequirementInterpretable(requirement: InjectRequirement): Boolean = requirement.requiredType.run { !isMarkedNullable && arguments.isEmpty() }

    protected open fun solve(interpretTarget: Provider, providers: Set<Provider>, injectRequirement: InjectRequirement): Provider = SimpleInjectionResolverUtil.solve(interpretTarget, providers, injectRequirement)
            ?: throw ProviderRequirementCannotFulfilException(this, interpretTarget,
                    "No provider available for ${injectRequirement.requiredType} (name=\"${injectRequirement.name}\")")
                    .also { (interpretTarget as ComponentProvider<*>).catchedThrowable = it }
}
