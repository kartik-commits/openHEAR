import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.openhear.common.di.AppContainer
import com.openhear.common.ui.screens.ScanScreen
import com.openhear.common.ui.theme.OpenHearTheme

@Composable
fun App() {
    val settings = AppContainer.settings
    var showDisclaimer by remember {
        mutableStateOf(!settings.getBoolean("openhear_disclaimer_accepted", false))
    }

    OpenHearTheme {
        Navigator(screen = ScanScreen()) { navigator ->
            SlideTransition(navigator)
        }

        if (showDisclaimer) {
            AlertDialog(
                onDismissRequest = { /* Unskippable */ },
                title = { Text("OpenHear — Legal Notice & Disclaimer") },
                text = {
                    Text(
                        "OpenHear is an independent, open-source companion utility. It is not affiliated with, endorsed by, or associated with Imagine Marketing Limited or boAt.\n\n" +
                                "THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES, OR HARDWARE ISSUES ARISING FROM THE USE OF THIS SOFTWARE.\n\n" +
                                "Please listen responsibly at safe volume levels.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            settings.putBoolean("openhear_disclaimer_accepted", true)
                            showDisclaimer = false
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("I Understand & Accept")
                    }
                }
            )
        }
    }
}
