package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProxyType
import com.example.ui.theme.TelegramBlue

@Composable
fun AddProxyDialog(
    isPersian: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (name: String, type: ProxyType, host: String, port: Int, secret: String?) -> Unit
) {
    var proxyName by remember { mutableStateOf("") }
    var proxyType by remember { mutableStateOf(ProxyType.MTPROTO) }
    var host by remember { mutableStateOf("") }
    var portStr by remember { mutableStateOf("443") }
    var secret by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("add_proxy_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = TelegramBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPersian) "افزودن پروکسی اختصاصی" else "Add Custom Proxy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isPersian) "نوع پروکسی را مشخص کنید:" else "Select Proxy Protocol:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProxyType.values().filter { it != ProxyType.DIRECT }.forEach { type ->
                        FilterChip(
                            selected = proxyType == type,
                            onClick = {
                                proxyType = type
                                if (type == ProxyType.MTPROTO && portStr == "1080") portStr = "443"
                                if (type == ProxyType.SOCKS5 && portStr == "443") portStr = "1080"
                            },
                            label = { Text(type.name, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = proxyName,
                    onValueChange = { proxyName = it },
                    label = { Text(if (isPersian) "نام نمایشی (اختیاری)" else "Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text(if (isPersian) "آدرس سرور (Server Host/IP)" else "Server IP / Host") },
                    placeholder = { Text("e.g. 149.154.167.50") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = portStr,
                    onValueChange = { portStr = it },
                    label = { Text(if (isPersian) "پورت (Port)" else "Port") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                if (proxyType == ProxyType.MTPROTO) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = secret,
                        onValueChange = { secret = it },
                        label = { Text(if (isPersian) "کلید رمز (Secret - اختیاری)" else "Secret (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val port = portStr.toIntOrNull() ?: 443
                    if (host.isNotBlank()) {
                        onSubmit(proxyName, proxyType, host, port, secret)
                    }
                },
                enabled = host.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                modifier = Modifier.testTag("save_proxy_button")
            ) {
                Text(if (isPersian) "ذخیره و تست" else "Save & Test", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isPersian) "انصراف" else "Cancel")
            }
        }
    )
}
