subprojects {
    configurations.all {
        resolutionStrategy.dependencySubstitution {
            substitute(module("com.turo.nibel:nibel-runtime"))
                .using(project(":nibel-runtime"))

            substitute(module("com.turo.nibel:nibel-compiler"))
                .using(project(":nibel-compiler"))
        }
    }
}
