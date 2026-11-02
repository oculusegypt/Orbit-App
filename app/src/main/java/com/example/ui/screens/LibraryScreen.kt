package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.models.LibraryBook
import com.example.ui.theme.OrbitEmerald
import com.example.ui.theme.OrbitIndigo
import com.example.ui.theme.OrbitNavy
import com.example.ui.viewmodel.OrbitViewModel

@Composable
fun LibraryScreen(
    viewModel: OrbitViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val books by viewModel.books.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }
    var selectedBookForChapters by remember { mutableStateOf<LibraryBook?>(null) }

    val categories = listOf("الكل", "الحساب الذهني والسروبان", "تأسيس لغة عربية وقراءة", "تأسيس رياضيات", "إعداد وتأهيل المدربين - العباقرة الصغار")

    val filteredBooks = books.filter { b ->
        val matchCat = selectedCategory == "الكل" || b.category.contains(selectedCategory)
        val matchSearch = searchQuery.isBlank() || b.title.contains(searchQuery, ignoreCase = true) || b.description.contains(searchQuery, ignoreCase = true)
        matchCat && matchSearch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("المكتبة الرقمية والملازم 📚", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text("المناهج المعتمدة وكراسات التدريب المنزلي لعام 2026", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث في الملازم والكتب التدريبية...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("library_search_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Categories
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OrbitIndigo,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Books List
        items(filteredBooks) { book ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth().testTag("library_book_card_${book.id}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AsyncImage(
                        model = book.coverUrl,
                        contentDescription = book.title,
                        modifier = Modifier
                            .width(85.dp)
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            color = OrbitIndigo.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = book.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = OrbitIndigo,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = book.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "تأليف: ${book.author}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "${book.pagesCount} صفحة • إصدار ${book.publishedYear}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { selectedBookForChapters = book },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("الفهرس (${book.chapters.size})", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    val url = "https://wa.me/201028340042?text=" + Uri.encode("مرحباً مس سارة، أرغب في تحميل والحصول على نسخة ملزمة: ${book.title}")
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("طلب النسخة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Chapters breakdown dialog
    selectedBookForChapters?.let { book ->
        Dialog(onDismissRequest = { selectedBookForChapters = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("فهرس المحتوى وفصول الكتاب", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        IconButton(onClick = { selectedBookForChapters = null }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }
                    Text(book.title, fontSize = 12.sp, color = OrbitIndigo, maxLines = 1)
                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        book.chapters.forEach { ch ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(ch.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                    Text("ص ${ch.pages}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { selectedBookForChapters = null },
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitNavy),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إغلاق")
                    }
                }
            }
        }
    }
}
