-dontobfuscate

# Keep all public classes (referenced by feature modules across module boundaries)
-keep class com.turo.nibel.sample.common.** { *; }

-dontwarn com.turo.nibel.sample.**
