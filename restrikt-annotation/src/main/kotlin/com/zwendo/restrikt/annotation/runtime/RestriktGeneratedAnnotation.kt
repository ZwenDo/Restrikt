package com.zwendo.restrikt.annotation.runtime

@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = """This element is generated by the Restrikt plugin. It is not intended to be used directly.
        To generate this element, use its corresponding annotation. You can freely opt-in for this element if you intend
        to use it through reflection.""",
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class RestriktGeneratedAnnotation
