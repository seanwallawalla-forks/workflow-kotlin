package com.squareup.workflow1.ui

import kotlin.reflect.KClass

/**
 * A [ViewRegistry] that contains only other registries and delegates to their [buildView] methods.
 *
 * Whenever any registries are combined using the [ViewRegistry] factory functions or `plus`
 * operators, an instance of this class is returned. All registries' keys are checked at
 * construction to ensure that no duplicate keys exist.
 *
 * The implementation of [buildView] consists of a single layer of indirection – the responsible
 * [ViewRegistry] is looked up in a map by key, and then that registry's [buildView] is called.
 *
 * When multiple [CompositeViewRegistry]s are combined, they are flattened, so that there is never
 * more than one layer of indirection. In other words, a [CompositeViewRegistry] will never contain
 * a reference to another [CompositeViewRegistry].
 */
@WorkflowUiExperimentalApi
internal class CompositeViewRegistry private constructor(
  private val registriesByKey: Map<KClass<*>, ViewRegistry>
) : ViewRegistry {

  constructor (vararg registries: ViewRegistry) : this(mergeRegistries(*registries))

  override val keys: Set<KClass<*>> get() = registriesByKey.keys

  override fun <RenderingT : Any> getFactoryFor(
    renderingType: KClass<out RenderingT>
  ): ViewFactory<RenderingT>? = registriesByKey[renderingType]?.getFactoryFor(renderingType)

  companion object {
    private fun mergeRegistries(vararg registries: ViewRegistry): Map<KClass<*>, ViewRegistry> {
      val registriesByKey = mutableMapOf<KClass<*>, ViewRegistry>()

      fun putAllUnique(other: Map<KClass<*>, ViewRegistry>) {
        val duplicateKeys = registriesByKey.keys.intersect(other.keys)
        check(duplicateKeys.isEmpty()) { "Must not have duplicate entries: $duplicateKeys" }
        registriesByKey.putAll(other)
      }

      registries.forEach { registry ->
        if (registry is CompositeViewRegistry) {
          // Try to keep the composite registry as flat as possible.
          putAllUnique(registry.registriesByKey)
        } else {
          putAllUnique(registry.keys.associateWith { registry })
        }
      }
      return registriesByKey.toMap()
    }
  }
}
