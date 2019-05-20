package net.swamphut.swampium.extra.server

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import net.swamphut.swampium.core.Swampium
import net.swamphut.swampium.core.swobject.container.SwObject
import net.swamphut.swampium.core.swobject.dependency.ServiceProvider
import net.swamphut.swampium.core.swobject.lifecycle.LifeCycleHook
import net.swamphut.swampium.service.spec.server.EventService
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.RegisteredListener
import java.util.logging.Level


@SwObject
@ServiceProvider([EventService::class])
class SwampiumEventService : LifeCycleHook, Listener, EventService {
    private val eventPrioritySubjectMap = HashMap<Class<out Event>, HashMap<EventPriority, PublishSubject<Event>>>();
    private val listeners: HashSet<RegisteredListener> = hashSetOf()

    override fun init() {
        EventPriority.values().forEach { priority ->
            val listener = RegisteredListener(this, { _, event -> onEvent(event, priority) },
                    priority, Swampium.instance, false);
            HandlerList.getHandlerLists().forEach { it.register(listener) }
            listeners.add(listener)
        }
    }

    override fun disable() {
        HandlerList.unregisterAll(this)
        eventPrioritySubjectMap.flatMap { it.value.map { it.value } }.map { it.onComplete() }
    }

    private fun onEvent(event: Event, priority: EventPriority) {
        if (eventPrioritySubjectMap.containsKey(event::class.java)
                && eventPrioritySubjectMap[event::class.java]!!.containsKey(priority)) {
            eventPrioritySubjectMap
                    .getOrPut(event::class.java, { HashMap() })
                    .getOrPut(priority, { PublishSubject.create<Event>() }).onNext(event)
        }
    }

    override fun <T : Event> on(listener: Any, eventClass: Class<T>, eventPriority: EventPriority): Observable<T> {
        val swObjectInfo =
                return (eventPrioritySubjectMap
                        .getOrPut(eventClass, { HashMap() })
                        .getOrPut(eventPriority, { PublishSubject.create<Event>() }))
                        .doOnSubscribe { Swampium.instance.logger.log(Level.INFO, "on subscribe") }
                        .doOnDispose { Swampium.instance.logger.log(Level.INFO, "on dispose") }
                        as Observable<T>
    }
}