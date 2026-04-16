package nibel.compiler.generator

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import nibel.annotations.ImplementationType

typealias Arguments = Map<String, Any?>

fun List<KSValueArgument>.toMap(): Arguments =
    associate { it.name!!.asString() to it.value }

fun KSFunctionDeclaration.isComposable(): Boolean =
    annotations.any { it.shortName.getShortName() == "Composable" }

inline fun <reified A> KSAnnotated.findAnnotation(): KSAnnotation? =
    annotations.find { it.shortName.getShortName() == A::class.simpleName }

fun KSType.asImplementationType(): ImplementationType =
    when (declaration.simpleName.asString()) {
        ImplementationType.Fragment.name -> ImplementationType.Fragment
        ImplementationType.Composable.name -> ImplementationType.Composable
        else -> error("Unknown ${ImplementationType::class.qualifiedName}")
    }

/**
 * KSP 2 compatibility: Converts annotation argument values to [KSType].
 * In KSP 2, class literal arguments may be returned as [KSClassDeclaration] instead of [KSType].
 */
fun Any?.asKSType(): KSType {
    return when (this) {
        is KSType -> this
        is KSClassDeclaration -> asType(emptyList())
        else -> error("Cannot convert ${this?.let { it::class.simpleName } ?: "null"} to KSType")
    }
}

/**
 * KSP 2 compatibility: Converts annotation argument values to [ImplementationType].
 * In KSP 2, enum entry arguments are returned as [KSClassDeclaration] instead of [KSType].
 */
fun Any?.asImplementationType(): ImplementationType {
    return when (this) {
        is KSType -> asImplementationType()
        is KSClassDeclaration -> {
            check(classKind == ClassKind.ENUM_ENTRY) {
                "Expected ENUM_ENTRY but got $classKind for ${qualifiedName?.asString()}"
            }
            when (simpleName.asString()) {
                ImplementationType.Fragment.name -> ImplementationType.Fragment
                ImplementationType.Composable.name -> ImplementationType.Composable
                else -> error("Unknown ${ImplementationType::class.qualifiedName}: ${simpleName.asString()}")
            }
        }
        else -> error("Cannot convert ${this?.let { it::class.simpleName } ?: "null"} to ImplementationType")
    }
}
