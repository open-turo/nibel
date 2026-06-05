-dontobfuscate

# Keep Hilt-generated code (referenced by app module's Dagger component)
-keep class **_GeneratedInjector { *; }
-keep class **_HiltModules* { *; }
-keep class **_LazyMapKey { *; }

# Keep ViewModels (injected by Hilt across module boundaries)
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep Fragment subclasses (instantiated by class name)
-keep class * extends androidx.fragment.app.Fragment { *; }

-dontwarn com.turo.nibel.sample.**
