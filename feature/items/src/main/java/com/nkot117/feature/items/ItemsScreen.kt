package com.nkot117.feature.items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nkot117.core.common.toEpochMillis
import com.nkot117.core.domain.model.Item
import com.nkot117.core.domain.model.ItemCategory
import com.nkot117.core.ui.components.ChecklistActionRow
import com.nkot117.core.ui.components.DatePickerField
import com.nkot117.core.ui.components.SecondaryButton
import com.nkot117.core.ui.mapper.label
import com.nkot117.core.ui.theme.BackgroundColor
import com.nkot117.core.ui.theme.Error300
import com.nkot117.core.ui.theme.Primary500
import com.nkot117.core.ui.theme.TextMain
import com.nkot117.core.ui.theme.TextSub
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun ItemsScreenRoute(contentPadding: PaddingValues, viewModel: ItemsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ItemsScreen(
        contentPadding = contentPadding,
        state = state,
        setDate = viewModel::setDate,
        setCategory = viewModel::setCategory,
        setRegisterItemName = viewModel::setRegisterItemName,
        registerItem = viewModel::registerItem,
        deleteItem = viewModel::deleteItem
    )
}

@Composable
fun ItemsScreen(
    contentPadding: PaddingValues,
    state: ItemsUiState,
    setDate: (Long) -> Unit,
    setCategory: (ItemCategory) -> Unit,
    setRegisterItemName: (String) -> Unit,
    registerItem: () -> Unit,
    deleteItem: (Long) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Box(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .background(
                BackgroundColor
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    PaddingValues(
                        start = 41.dp,
                        end = 41.dp,
                        top = 16.dp,
                        bottom = 88.dp
                    )
                )
        ) {
            CategoryTabRow(
                selectedCategory = state.category,
                onSelectCategory = { setCategory(it) }
            )

            Spacer(Modifier.height(20.dp))

            if (state.category == ItemCategory.DATE_SPECIFIC) {
                DatePickerField(
                    selectedDateMillis = state.date.toEpochMillis(ZoneOffset.UTC),
                    onDateChange = setDate,
                    confirmButtonLabel = "OK",
                    cancelButtonLabel = "キャンセル",
                    formLabel = "日付"
                )

                Spacer(Modifier.height(20.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.form.name,
                    onValueChange = {
                        setRegisterItemName(it)
                    },
                    placeholder = { Text("追加する持ち物") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, shape = RoundedCornerShape(12.dp))
                        .semantics { contentDescription = "item_name_text_field" },
                    shape = RoundedCornerShape(12.dp)
                )

                SecondaryButton(
                    text = "＋ 追加",
                    onClick = {
                        focusManager.clearFocus()
                        registerItem()
                    },
                    modifier = Modifier
                        .height(56.dp)
                        .semantics { contentDescription = "register_item_button" },
                    enabled = state.form.canSubmit
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "登録されている持ち物リスト",
                style = MaterialTheme.typography.titleSmall,
                color = TextMain
            )

            Spacer(Modifier.height(8.dp))

            if (state.itemList.isEmpty()) {
                Text(
                    text = "持ち物が登録されていません。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSub
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = state.itemList,
                        key = { item -> item.id ?: item.name }
                    ) { item ->
                        ChecklistActionRow(
                            title = item.name,
                            icon = Icons.Default.Delete,
                            iconColor = Error300,
                            onClick = {
                                item.id?.let {
                                    deleteItem(it)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.padding(top = 15.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryTabRow(
    selectedCategory: ItemCategory,
    onSelectCategory: (ItemCategory) -> Unit
) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier.horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        ItemCategory.entries.forEach { tag ->
            val isSelected = tag == selectedCategory
            val textColor = if (isSelected) Primary500 else TextSub

            Column(
                modifier = Modifier
                    .clickable { onSelectCategory(tag) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tag.label(),
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(6.dp))

                AnimatedVisibility(visible = isSelected) {
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .width(24.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Primary500)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ItemsScreenPreview() {
    ItemsScreen(
        contentPadding = PaddingValues(0.dp),
        state = ItemsUiState(
            date = LocalDate.now(),
            category = ItemCategory.ALWAYS,
            itemList = previewRegisteredItems()
        ),
        setDate = {},
        setCategory = {},
        setRegisterItemName = {},
        registerItem = {},
        deleteItem = {}
    )
}

private fun previewRegisteredItems(): ImmutableList<Item> = persistentListOf(
    Item(id = 1L, name = "財布", category = ItemCategory.WORKDAY),
    Item(id = 2L, name = "家の鍵", category = ItemCategory.ALWAYS),
    Item(id = 3L, name = "スマホ", category = ItemCategory.RAINY),
    Item(id = 4L, name = "ハンカチ", category = ItemCategory.SUNNY),
    Item(id = 5L, name = "ティッシュ", category = ItemCategory.DATE_SPECIFIC)
)
