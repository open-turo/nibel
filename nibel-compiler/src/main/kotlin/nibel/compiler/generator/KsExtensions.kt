package nibel.compiler.generator

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

fun Any.asImplementationType(): ImplementationType {
    val name = when (this) {
        is KSType -> declaration.simpleName.asString()
        is KSClassDeclaration -> simpleName.asString()
        else -> error("Unexpected enum value type: ${this::class}")
    }
    return when (name) {
        ImplementationType.Fragment.name -> ImplementationType.Fragment
        ImplementationType.Composable.name -> ImplementationType.Composable
        else -> error("Unknown ${ImplementationType::class.qualifiedName}")
    }
}
