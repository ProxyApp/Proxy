package com.shareyourproxy.util

import android.app.Activity
import android.app.Dialog
import android.app.Fragment
import android.content.res.ColorStateList
import android.support.v4.content.ContextCompat.getColor
import android.support.v4.content.ContextCompat.getColorStateList
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.View
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import android.support.v4.app.Fragment as SupportFragment

object ButterKnife {
    @JvmStatic fun unbind(target: Any) = LazyRegistry.reset(target)
}

//View Bindings
public fun <V : View> View.bindView(id: Int): ReadOnlyProperty<View, V> = required(id, viewFinder)
public fun <V : View> Activity.bindView(id: Int): ReadOnlyProperty<Activity, V> = required(id, viewFinder)
public fun <V : View> Dialog.bindView(id: Int): ReadOnlyProperty<Dialog, V> = required(id, viewFinder)
public fun <V : View> Fragment.bindView(id: Int): ReadOnlyProperty<Fragment, V> = required(id, viewFinder)
public fun <V : View> SupportFragment.bindView(id: Int): ReadOnlyProperty<SupportFragment, V> = required(id, viewFinder)
public fun <V : View> ViewHolder.bindView(id: Int): ReadOnlyProperty<ViewHolder, V> = required(id, viewFinder)
//View Bindings Optional
public fun <V : View> View.bindOptionalView(id: Int): ReadOnlyProperty<View, V?> = optional(id, viewFinder)
public fun <V : View> Activity.bindOptionalView(id: Int): ReadOnlyProperty<Activity, V?> = optional(id, viewFinder)
public fun <V : View> Dialog.bindOptionalView(id: Int): ReadOnlyProperty<Dialog, V?> = optional(id, viewFinder)
public fun <V : View> Fragment.bindOptionalView(id: Int): ReadOnlyProperty<Fragment, V?> = optional(id, viewFinder)
public fun <V : View> SupportFragment.bindOptionalView(id: Int): ReadOnlyProperty<SupportFragment, V?> = optional(id, viewFinder)
public fun <V : View> ViewHolder.bindOptionalView(id: Int): ReadOnlyProperty<ViewHolder, V?> = optional(id, viewFinder)
//Multiple View Bindings
public fun <V : View> View.bindViews(vararg ids: Int): ReadOnlyProperty<View, List<V>> = required(ids, viewFinder)
public fun <V : View> Activity.bindViews(vararg ids: Int): ReadOnlyProperty<Activity, List<V>> = required(ids, viewFinder)
public fun <V : View> Dialog.bindViews(vararg ids: Int): ReadOnlyProperty<Dialog, List<V>> = required(ids, viewFinder)
public fun <V : View> Fragment.bindViews(vararg ids: Int): ReadOnlyProperty<Fragment, List<V>> = required(ids, viewFinder)
public fun <V : View> SupportFragment.bindViews(vararg ids: Int): ReadOnlyProperty<SupportFragment, List<V>> = required(ids, viewFinder)
public fun <V : View> ViewHolder.bindViews(vararg ids: Int): ReadOnlyProperty<ViewHolder, List<V>> = required(ids, viewFinder)
//Multiple View Bindings Optional
public fun <V : View> View.bindOptionalViews(vararg ids: Int): ReadOnlyProperty<View, List<V>> = optional(ids, viewFinder)
public fun <V : View> Activity.bindOptionalViews(vararg ids: Int): ReadOnlyProperty<Activity, List<V>> = optional(ids, viewFinder)
public fun <V : View> Dialog.bindOptionalViews(vararg ids: Int): ReadOnlyProperty<Dialog, List<V>> = optional(ids, viewFinder)
public fun <V : View> Fragment.bindOptionalViews(vararg ids: Int): ReadOnlyProperty<Fragment, List<V>> = optional(ids, viewFinder)
public fun <V : View> SupportFragment.bindOptionalViews(vararg ids: Int): ReadOnlyProperty<SupportFragment, List<V>> = optional(ids, viewFinder)
public fun <V : View> ViewHolder.bindOptionalViews(vararg ids: Int): ReadOnlyProperty<ViewHolder, List<V>> = optional(ids, viewFinder)
//View findViewById
private val View.viewFinder: View.(Int) -> View? get() = { findViewById(it) }
private val Activity.viewFinder: Activity.(Int) -> View? get() = { findViewById(it) }
private val Dialog.viewFinder: Dialog.(Int) -> View? get() = { findViewById(it) }
private val Fragment.viewFinder: Fragment.(Int) -> View? get() = { view.findViewById(it) }
private val SupportFragment.viewFinder: SupportFragment.(Int) -> View? get() = { view.findViewById(it) }
private val ViewHolder.viewFinder: ViewHolder.(Int) -> View? get() = { itemView.findViewById(it) }

//Dimen Bindings
public fun View.bindDimen(id: Int): ReadOnlyProperty<View, Int> = required(id, dimenFinder)
public fun Activity.bindDimen(id: Int): ReadOnlyProperty<Activity, Int> = required(id, dimenFinder)
public fun Dialog.bindDimen(id: Int): ReadOnlyProperty<Dialog, Int> = required(id, dimenFinder)
public fun Fragment.bindDimen(id: Int): ReadOnlyProperty<Fragment, Int> = required(id, dimenFinder)
public fun SupportFragment.bindDimen(id: Int): ReadOnlyProperty<SupportFragment, Int> = required(id, dimenFinder)
//Dimen getDimensionPixelSize(int)
private val View.dimenFinder: View.(Int) -> Int? get() = { resources.getDimensionPixelSize(it) }
private val Activity.dimenFinder: Activity.(Int) -> Int? get() = { resources.getDimensionPixelSize(it) }
private val Dialog.dimenFinder: Dialog.(Int) -> Int? get() = { context.resources.getDimensionPixelSize(it) }
private val Fragment.dimenFinder: Fragment.(Int) -> Int? get() = { resources.getDimensionPixelSize(it) }
private val SupportFragment.dimenFinder: SupportFragment.(Int) -> Int? get() = { resources.getDimensionPixelSize(it) }

//Color<Int> bindings
public fun View.bindColor(id: Int): ReadOnlyProperty<View, Int> = required(id, colorFinder)
public fun Activity.bindColor(id: Int): ReadOnlyProperty<Activity, Int> = required(id, colorFinder)
public fun Dialog.bindColor(id: Int): ReadOnlyProperty<Dialog, Int> = required(id, colorFinder)
public fun Fragment.bindColor(id: Int): ReadOnlyProperty<Fragment, Int> = required(id, colorFinder)
public fun SupportFragment.bindColor(id: Int): ReadOnlyProperty<SupportFragment, Int> = required(id, colorFinder)
//ColorStateList bindings
public fun View.bindColorStateList(id: Int): ReadOnlyProperty<View, ColorStateList> = requiredList(id, colorFinderList)
public fun Activity.bindColorStateList(id: Int): ReadOnlyProperty<Activity, ColorStateList> = requiredList(id, colorFinderList)
public fun Dialog.bindColorStateList(id: Int): ReadOnlyProperty<Dialog, ColorStateList> = requiredList(id, colorFinderList)
public fun Fragment.bindColorStateList(id: Int): ReadOnlyProperty<Fragment, ColorStateList> = requiredList(id, colorFinderList)
public fun SupportFragment.bindColorStateList(id: Int): ReadOnlyProperty<SupportFragment, ColorStateList> = requiredList(id, colorFinderList)
//Color ContextCompat.getColor(context,int)
private val View.colorFinder: View.(Int) -> Int? get() = { getColor(context, it) }
private val Activity.colorFinder: Activity.(Int) -> Int? get() = { getColor(baseContext, it) }
private val Dialog.colorFinder: Dialog.(Int) -> Int? get() = { getColor(context, it) }
private val Fragment.colorFinder: Fragment.(Int) -> Int? get() = { getColor(context, it) }
private val SupportFragment.colorFinder: SupportFragment.(Int) -> Int? get() = { getColor(context, it) }
//Color ContextCompat.getColorStateList(context,int)
private val View.colorFinderList: View.(Int) -> ColorStateList? get() = { getColorStateList(context, it) }
private val Activity.colorFinderList: Activity.(Int) -> ColorStateList? get() = { getColorStateList(baseContext, it) }
private val Dialog.colorFinderList: Dialog.(Int) -> ColorStateList? get() = { getColorStateList(context, it) }
private val Fragment.colorFinderList: Fragment.(Int) -> ColorStateList? get() = { getColorStateList(context, it) }
private val SupportFragment.colorFinderList: SupportFragment.(Int) -> ColorStateList? get() = { getColorStateList(context, it) }
//Error Messaging
private fun viewNotFound(id: Int, desc: KProperty<*>): Nothing = throw IllegalStateException("View ID $id for '${desc.name}' not found.")
private fun attrNotFound(id: Int, desc: KProperty<*>): Nothing = throw IllegalStateException("Attribute ID $id for '${desc.name}' not found.")

@Suppress("UNCHECKED_CAST")
private fun <T, V : View> required(id: Int, finder: T.(Int) -> View?) = LazyView { t: T, desc -> t.finder(id) as V? ?: viewNotFound(id, desc) }

@Suppress("UNCHECKED_CAST")
private fun <T, V : View> optional(id: Int, finder: T.(Int) -> View?) = LazyView { t: T, desc -> t.finder(id) as V? }

@Suppress("UNCHECKED_CAST")
private fun <T, V : View> required(ids: IntArray, finder: T.(Int) -> View?) = LazyView { t: T, desc -> ids.map { t.finder(it) as V? ?: viewNotFound(it, desc) } }

@Suppress("UNCHECKED_CAST")
private fun <T, V : View> optional(ids: IntArray, finder: T.(Int) -> View?) = LazyView { t: T, desc -> ids.map { t.finder(it) as V? }.filterNotNull() }

@Suppress("UNCHECKED_CAST")
private fun <T> required(id: Int, finder: T.(Int) -> Int?) = LazyVal { t: T, desc -> t.finder(id) ?: attrNotFound(id, desc) }

@Suppress("UNCHECKED_CAST")
private fun <T> requiredList(id: Int, finder: T.(Int) -> ColorStateList?) = LazyVal { t: T, desc -> t.finder(id) ?: attrNotFound(id, desc) }

// Like Kotlin's lazy delegate but the initializer gets the target and metadata passed to it
private class LazyView<T, V>(private val initializer: (T, KProperty<*>) -> V) : ReadOnlyProperty<T, V> {
    private object EMPTY

    private var value: Any? = EMPTY

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        if (value == EMPTY) {
            value = initializer(thisRef, property)
            LazyRegistry.register(thisRef, this)
        }
        @Suppress("UNCHECKED_CAST")
        return value as V
    }

    fun reset() {
        value = EMPTY
    }
}

private object LazyRegistry {
    private val lazyMap = WeakHashMap<Any, MutableCollection<LazyView<*, *>>>()

    fun <T> register(target: T, lazy: LazyView<T, *>) {
        lazyMap.getOrPut(target, { Collections.newSetFromMap(WeakHashMap()) }).add(lazy)
    }

    fun reset(target: Any) {
        lazyMap[target]?.forEach { it.reset() }
    }
}

// Like Kotlin's lazy delegate but the initializer gets the target and metadata passed to it
private class LazyVal<T, V>(private val initializer: (T, KProperty<*>) -> V) : ReadOnlyProperty<T, V> {
    private object EMPTY

    private var value: Any? = EMPTY

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        if (value == EMPTY) {
            value = initializer(thisRef, property)
        }
        @Suppress("UNCHECKED_CAST")
        return value as V
    }

}
