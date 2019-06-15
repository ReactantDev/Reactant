package net.swamphut.swampium.core.exception

import net.swamphut.swampium.core.dependency.injection.producer.InjectableWrapper

class CyclicDependencyRelationException(val dependencies: List<InjectableWrapper>) : Exception()