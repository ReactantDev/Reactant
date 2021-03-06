package dev.reactant.reactant.ui.element

import dev.reactant.reactant.service.spec.server.SchedulerService
import dev.reactant.reactant.ui.UIDestroyable
import dev.reactant.reactant.ui.editing.ReactantUIElementEditing
import dev.reactant.reactant.ui.element.collection.ReactantUIElementChildrenSet
import dev.reactant.reactant.ui.element.collection.ReactantUIElementClassSet
import dev.reactant.reactant.ui.element.style.ReactantUIElementStyle
import dev.reactant.reactant.ui.event.UIElementEvent
import dev.reactant.reactant.ui.event.UIEvent
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import kotlin.reflect.KClass


abstract class ReactantUIElement(val allocatedSchedulerService: SchedulerService)
    : ReactantUIElementStyle(), UIElement {
    init {
        el = this
    }

    override val compositeDisposable: CompositeDisposable = CompositeDisposable()
    override val scheduler = UIDestroyable.convertToDestroyableScheduler(this, allocatedSchedulerService)

    override val event = PublishSubject.create<UIElementEvent>()
    private var _parent: UIElement? = null
    override var parent: UIElement?
        get() = _parent
        set(newParent) {
            if (newParent == _parent) return
            val originParent = _parent
            _parent = newParent

            originParent?.children?.remove(this)
            newParent?.children?.add(this)
        }

    override val rootElement: UIElement? get() = parent?.rootElement ?: this

    final override val children = ReactantUIElementChildrenSet(this)

    final override val attributes = HashMap<String, String?>()
    override var id: String? = null//by attributes.withDefault { null }
    override var classList = ReactantUIElementClassSet(attributes)


    private val eventSubjects: HashMap<KClass<out UIEvent>, Subject<out Any>> = HashMap()

    @Suppress("UNCHECKED_CAST")
    override fun <T : UIEvent> getEventSubject(clazz: KClass<T>): Subject<T> =
            eventSubjects.getOrPut(clazz) { PublishSubject.create<T>() } as Subject<T>

    abstract override fun edit(): ReactantUIElementEditing<ReactantUIElement>

    @Suppress("UNCHECKED_CAST")
    override fun renderVisibleElementsPositions(): LinkedHashMap<out ReactantUIElement, HashSet<Pair<Int, Int>>> = super.renderVisibleElementsPositions() as LinkedHashMap<out ReactantUIElement, HashSet<Pair<Int, Int>>>

}
