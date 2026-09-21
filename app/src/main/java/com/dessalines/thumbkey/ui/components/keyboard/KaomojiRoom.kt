package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val FAV="__favorites__"; private const val REC="__recents__"
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KaomojiRoom(onCommit:(String)->Unit,onBackToLetters:()->Unit,onGoToEmoji:(()->Unit)?=null,modifier:Modifier=Modifier){
 val context=LocalContext.current; var cat by remember{mutableStateOf(KaomojiLibrary.categories.first().id)}; var fav by remember{mutableStateOf(KaomojiPreferences.loadFavorites(context))}; var recent by remember{mutableStateOf(KaomojiPreferences.loadRecents(context))}
 val q=PaletteSearchCapture.query
 val shown=if(q.isNotBlank()) KaomojiLibrary.search(q).distinctBy{it.text} else when(cat){FAV->KaomojiLibrary.allItems.filter{it.text in fav};REC->recent.mapNotNull{r->KaomojiLibrary.allItems.firstOrNull{it.text==r}};else->KaomojiLibrary.categories.firstOrNull{it.id==cat}?.items.orEmpty()}
 Column(modifier.padding(7.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
  Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){ Text("KAOMOJI",fontWeight=FontWeight.Bold); Surface(onClick={PaletteSearchCapture.activate()},shape=RoundedCornerShape(12.dp),border=BorderStroke(1.dp,MaterialTheme.colorScheme.outline),modifier=Modifier.weight(1f)){Text(if(q.isEmpty())"Search…" else q,Modifier.padding(9.dp))}; Surface(onClick={PaletteSearchCapture.release(true);onBackToLetters()},shape=RoundedCornerShape(18.dp)){Text("✕",Modifier.padding(9.dp))} }
  Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(5.dp)){
   fun select(id:String){PaletteSearchCapture.release(true);cat=id}
   Surface(onClick={select(FAV)},shape=RoundedCornerShape(14.dp)){Text("★ Favorites",Modifier.padding(7.dp))}; Surface(onClick={select(REC)},shape=RoundedCornerShape(14.dp)){Text("↻ Recent",Modifier.padding(7.dp))}
   KaomojiLibrary.categories.forEach{c->Surface(onClick={select(c.id)},shape=RoundedCornerShape(14.dp)){Text("${c.glyph} ${c.title}",Modifier.padding(7.dp))}}
  }
  LazyVerticalGrid(columns=GridCells.Adaptive(106.dp),horizontalArrangement=Arrangement.spacedBy(4.dp),verticalArrangement=Arrangement.spacedBy(4.dp),modifier=Modifier.weight(1f)){
   items(shown,key={it.text}){entry-> Surface(shape=RoundedCornerShape(10.dp),modifier=Modifier.fillMaxWidth().combinedClickable(onClick={recent=KaomojiPreferences.recordRecent(context,entry.text);onCommit(entry.text)},onDoubleClick={fav=KaomojiPreferences.toggleFavorite(context,entry.text)})){Column(Modifier.padding(9.dp)){Text(entry.text);entry.label?.let{Text(it,style=MaterialTheme.typography.labelSmall)}}}}
  }
 }
}
