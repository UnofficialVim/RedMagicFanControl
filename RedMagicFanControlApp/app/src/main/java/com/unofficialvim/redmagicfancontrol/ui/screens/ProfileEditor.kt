package com.unofficialvim.redmagicfancontrol.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.unofficialvim.redmagicfancontrol.data.MAX_LEVEL
import com.unofficialvim.redmagicfancontrol.data.MAX_NAME
import com.unofficialvim.redmagicfancontrol.data.MAX_STEPS
import com.unofficialvim.redmagicfancontrol.data.Profile
import com.unofficialvim.redmagicfancontrol.data.Step

/** Edits one profile (name, refresh rate, fan curve). [old] == null means "add". */
@Composable
fun ProfileEditor(old: Profile?, existingNames: List<String>, onDismiss: () -> Unit, onSave: (Profile) -> Unit) {
    var name by remember { mutableStateOf(old?.name ?: "") }
    var refresh by remember { mutableStateOf((old?.refreshRate ?: 5.0).toString()) }
    val steps = remember {
        mutableStateListOf<Pair<String, String>>().apply {
            (old?.steps ?: listOf(Step(40000, 1))).forEach { add("${it.tempMilliC / 1000}" to "${it.fanLvl}") }
        }
    }

    val refreshVal = refresh.toDoubleOrNull()
    val parsed = steps.map { (t, l) -> t.toIntOrNull()?.takeIf { it in 0..150 } to l.toIntOrNull()?.takeIf { it in 0..MAX_LEVEL } }
    val error = when {
        name.isBlank() -> "Name required"
        name in existingNames -> "Name already used"
        refreshVal == null || refreshVal <= 0 -> "Refresh must be > 0"
        steps.isEmpty() -> "Add at least one step"
        parsed.any { it.first == null || it.second == null } -> "Temp 0-150 °C, level 0-$MAX_LEVEL"
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (old == null) "Add profile" else "Edit profile") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 420.dp)) {
                item {
                    OutlinedTextField(name, { name = it.take(MAX_NAME) }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(
                        refresh, { refresh = it }, label = { Text("Refresh rate (s)") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(),
                    )
                }
                item { Text("Fan curve", style = MaterialTheme.typography.titleSmall) }
                itemsIndexed(steps) { i, (t, l) ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            t, { steps[i] = it.filter(Char::isDigit).take(3) to l }, label = { Text("°C") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            l, { steps[i] = t to it.filter(Char::isDigit).take(1) }, label = { Text("Level") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f),
                        )
                        IconButton({ steps.removeAt(i) }) { Icon(Icons.Filled.Close, "Remove step") }
                    }
                }
                item {
                    TextButton({ steps.add("" to "") }, enabled = steps.size < MAX_STEPS) { Text("Add step") }
                }
                if (error != null) item { Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = error == null,
                onClick = {
                    onSave(Profile(name.trim(), refreshVal!!, parsed.map { Step(it.first!! * 1000, it.second!!) }))
                },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } },
    )
}
