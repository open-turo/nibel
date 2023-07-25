@file:Suppress("TestFunctionName")

package nibel.tests

import androidx.compose.runtime.Composable
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.nulls.shouldNotBeNull
import nibel.annotations.DestinationWithNoArgs
import nibel.annotations.ImplementationType
import nibel.annotations.UiExternalEntry
import nibel.runtime.Nibel
import org.junit.Test


@UiExternalEntry(
    ImplementationType.Fragment,
    ExternalDestinationSearchTest.AssociatedDestination::class
)
@Composable
fun ExternalDestinationSearchTestFun() = Unit

class ExternalDestinationSearchTest {

    object AssociatedDestination : DestinationWithNoArgs
    object UnassociatedDestination : DestinationWithNoArgs

    @Test
    fun `Nibel#findEntryFactory should return entry instance if associated destination is used`() {
        val entry = Nibel.findEntryFactory(AssociatedDestination)
        entry.shouldNotBeNull()
    }

    @Test
    fun `Nibel#findEntryFactory should throw error if unassociated destination is used`() {
        shouldThrowAny {
            Nibel.findEntryFactory(UnassociatedDestination)
        }
    }
}
