package com.example.limelink

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.limelink.ui.theme.LimelinkTheme
import org.limelink.limelink_aos_sdk.LimeLinkListener
import org.limelink.limelink_aos_sdk.LimeLinkSDK
import org.limelink.limelink_aos_sdk.response.LimeLinkError
import org.limelink.limelink_aos_sdk.response.LimeLinkResult
import org.limelink.limelink_aos_sdk.response.ReferrerInfo
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class LogEntry(val timestamp: String, val message: String)

class MainActivity : ComponentActivity() {

    private val logEntries = mutableStateListOf<LogEntry>()
    private val deeplinkResult = mutableStateOf<LimeLinkResult?>(null)
    private val referrerResult = mutableStateOf<ReferrerInfo?>(null)
    private val currentIntent = mutableStateOf<Intent?>(null)

    private val linkListener = object : LimeLinkListener {
        override fun onDeeplinkReceived(result: LimeLinkResult) {
            deeplinkResult.value = result
            addLog("Deeplink received: ${result.originalUrl}")
            addLog("  resolved: ${result.resolvedUri}")
            addLog("  deferred: ${result.isDeferred}")
            if (result.queryParams.isNotEmpty()) {
                addLog("  queryParams: ${result.queryParams}")
            }
            addLog("  pathParams: main=${result.pathParams.mainPath}, sub=${result.pathParams.subPath}")
        }

        override fun onDeeplinkError(error: LimeLinkError) {
            addLog("ERROR [${error.code}]: ${error.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LimeLinkSDK.addLinkListener(linkListener)
        currentIntent.value = intent
        addLog("App created")

        enableEdgeToEdge()
        setContent {
            LimelinkTheme {
                MainScreen(
                    logEntries = logEntries,
                    deeplinkResult = deeplinkResult,
                    referrerResult = referrerResult,
                    onCheckReferrer = ::checkReferrer,
                    onSimulateUrl = ::simulateUrl,
                    onSendStats = ::sendStats,
                    onClearLogs = ::clearLogs
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntent.value = intent
        addLog("onNewIntent: ${intent.dataString}")
    }

    override fun onDestroy() {
        super.onDestroy()
        LimeLinkSDK.removeLinkListener(linkListener)
    }

    private fun checkReferrer() {
        addLog("Checking install referrer...")
        LimeLinkSDK.getInstallReferrer(this) { info ->
            referrerResult.value = info
            if (info != null) {
                addLog("Referrer: ${info.referrerUrl}")
                addLog("  click: ${info.clickTimestamp}, install: ${info.installTimestamp}")
                addLog("  limeLinkUrl: ${info.limeLinkUrl}")
            } else {
                addLog("No referrer info available")
            }
        }
    }

    private fun simulateUrl(url: String) {
        addLog("Simulating URL: $url")
        val simulatedIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        val isUl = LimeLinkSDK.isUniversalLink(simulatedIntent)
        addLog("  isUniversalLink: $isUl")

        val scheme = LimeLinkSDK.getSchemeFromIntent(simulatedIntent)
        addLog("  scheme: $scheme")

        val queryParams = LimeLinkSDK.parseQueryParams(simulatedIntent)
        addLog("  queryParams: $queryParams")

        val pathParams = LimeLinkSDK.parsePathParams(simulatedIntent)
        addLog("  pathParams: main=${pathParams.mainPath}, sub=${pathParams.subPath}")

        if (isUl) {
            LimeLinkSDK.handleUniversalLink(this, simulatedIntent) { result ->
                addLog("  handleUniversalLink result: $result")
            }
        }
    }

    private fun sendStats() {
        addLog("Sending stats (via legacy saveLimeLinkStatus)...")
        val intent = currentIntent.value
        if (intent?.data != null) {
            addLog("  Using current intent: ${intent.dataString}")
            MainScope().launch {
                try {
                    @Suppress("DEPRECATION")
                    org.limelink.limelink_aos_sdk.saveLimeLinkStatus(
                        this@MainActivity,
                        intent,
                        BuildConfig.LIMELINK_API_KEY
                    )
                    addLog("  Stats sent successfully")
                } catch (e: Exception) {
                    addLog("  Stats error: ${e.message}")
                }
            }
        } else {
            addLog("  No intent data to send stats for")
        }
    }

    private fun clearLogs() {
        logEntries.clear()
    }

    private fun addLog(message: String) {
        val ts = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        logEntries.add(0, LogEntry(ts, message))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    logEntries: List<LogEntry>,
    deeplinkResult: State<LimeLinkResult?>,
    referrerResult: State<ReferrerInfo?>,
    onCheckReferrer: () -> Unit,
    onSimulateUrl: (String) -> Unit,
    onSendStats: () -> Unit,
    onClearLogs: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LimeLink SDK Demo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Card 1: Settings & Config
            item { ConfigCard() }

            // Card 2: Deeplink Info
            item { DeeplinkCard(deeplinkResult.value) }

            // Card 3: Deferred Deeplink
            item { DeferredDeeplinkCard(referrerResult.value, onCheckReferrer) }

            // Card 4: URL Simulation
            item { SimulationCard(onSimulateUrl) }

            // Card 5: Stats
            item { StatsCard(onSendStats) }

            // Card 6: Logs
            item { LogCard(logEntries, onClearLogs) }
        }
    }
}

@Composable
fun ConfigCard() {
    DemoCard(title = stringResource(R.string.card_config_title)) {
        InfoRow(stringResource(R.string.label_api_key), maskKey(BuildConfig.LIMELINK_API_KEY))
        InfoRow(stringResource(R.string.label_project_id), BuildConfig.LIMELINK_PROJECT_ID)
        InfoRow(stringResource(R.string.label_custom_domain), BuildConfig.LIMELINK_CUSTOM_DOMAIN)
        InfoRow(stringResource(R.string.label_app_id), BuildConfig.LIMELINK_ANDROID_APPLICATION_ID)
    }
}

@Composable
fun DeeplinkCard(result: LimeLinkResult?) {
    DemoCard(title = stringResource(R.string.card_deeplink_title)) {
        if (result != null) {
            InfoRow("Original URL", result.originalUrl ?: "-")
            InfoRow("Resolved URI", result.resolvedUri?.toString() ?: "-")
            InfoRow("Is Deferred", result.isDeferred.toString())
            if (result.queryParams.isNotEmpty()) {
                InfoRow("Query Params", result.queryParams.toString())
            }
            InfoRow("Main Path", result.pathParams.mainPath.ifEmpty { "-" })
            InfoRow("Sub Path", result.pathParams.subPath ?: "-")
        } else {
            Text(
                text = stringResource(R.string.label_no_deeplink),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DeferredDeeplinkCard(info: ReferrerInfo?, onCheck: () -> Unit) {
    DemoCard(title = stringResource(R.string.card_deferred_title)) {
        if (info != null) {
            InfoRow("Referrer URL", info.referrerUrl ?: "-")
            InfoRow("Click TS", info.clickTimestamp.toString())
            InfoRow("Install TS", info.installTimestamp.toString())
            InfoRow("LimeLink URL", info.limeLinkUrl ?: "-")
        } else {
            Text(
                text = stringResource(R.string.label_no_referrer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onCheck, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.btn_check_referrer))
        }
    }
}

@Composable
fun SimulationCard(onSimulate: (String) -> Unit) {
    var url by remember { mutableStateOf("https://smaxh.limelink.org/link/test-suffix") }

    DemoCard(title = stringResource(R.string.card_simulation_title)) {
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text(stringResource(R.string.hint_simulate_url)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { onSimulate(url) })
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onSimulate(url) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.btn_simulate))
        }
    }
}

@Composable
fun StatsCard(onSend: () -> Unit) {
    DemoCard(title = stringResource(R.string.card_stats_title)) {
        Text(
            text = "Send stats event using current intent data",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onSend, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.btn_send_stats))
        }
    }
}

@Composable
fun LogCard(entries: List<LogEntry>, onClear: () -> Unit) {
    DemoCard(title = stringResource(R.string.card_log_title)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onClear) {
                Text(stringResource(R.string.btn_clear_logs))
            }
        }
        if (entries.isEmpty()) {
            Text(
                text = "No logs yet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column {
                entries.take(50).forEach { entry ->
                    Text(
                        text = "[${entry.timestamp}] ${entry.message}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DemoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(0.65f)
        )
    }
}

private fun maskKey(key: String): String {
    return if (key.length > 4) "${key.take(4)}****" else key
}
