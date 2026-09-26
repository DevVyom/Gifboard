package com.vyom.moderngifboard.ime
import android.content.*
import android.inputmethodservice.InputMethodService
import android.os.*
import android.view.*
import android.view.inputmethod.*
import android.widget.*
import androidx.core.content.FileProvider
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.vyom.moderngifboard.model.GifItem
import com.vyom.moderngifboard.provider.GoogleGifProvider
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
class GifKeyboardService:InputMethodService(){
 private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main);private val provider=GoogleGifProvider("off");private val http=OkHttpClient()
 private lateinit var adapter:GifAdapter;private lateinit var status:TextView;private var currentQuery="trending";private var next:String?=null;private var loading=false
 private val prefs by lazy{getSharedPreferences("gifboard",MODE_PRIVATE)}
 override fun onDestroy(){scope.cancel();super.onDestroy()}
 override fun onCreateInputView():View{
  val d=resources.displayMetrics.density;val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;minimumHeight=(280*d).toInt();setBackgroundColor(0xFFF7F7F7.toInt());setPadding((8*d).toInt(),(7*d).toInt(),(8*d).toInt(),(5*d).toInt())}
  val searchBox=TextInputLayout(this).apply{hint="Search GIFs";boxBackgroundMode=TextInputLayout.BOX_BACKGROUND_FILLED;setBoxCornerRadii(24*d,24*d,24*d,24*d)}
  val search=TextInputEditText(this).apply{setSingleLine(true);imeOptions=EditorInfo.IME_ACTION_SEARCH};searchBox.addView(search);root.addView(searchBox,LinearLayout.LayoutParams(-1,-2))
  val tabs=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
  fun tab(label:String,action:()->Unit)=MaterialButton(this).apply{text=label;isAllCaps=false;insetTop=0;insetBottom=0;setOnClickListener{action()}}
  tabs.addView(tab("Trending"){load("trending")},LinearLayout.LayoutParams(0,-2,1f));tabs.addView(tab("Recent"){showStored("recent")},LinearLayout.LayoutParams(0,-2,1f));tabs.addView(tab("Saved"){showStored("saved")},LinearLayout.LayoutParams(0,-2,1f));root.addView(tabs)
  status=TextView(this).apply{gravity=Gravity.CENTER;text="Loading GIFs...";setPadding(4,6,4,6)};root.addView(status);adapter=GifAdapter({commitGif(it)},{toggleSaved(it)})
  val list=RecyclerView(this).apply{layoutManager=GridLayoutManager(this@GifKeyboardService,2);adapter=this@GifKeyboardService.adapter;overScrollMode=View.OVER_SCROLL_NEVER;addOnScrollListener(object:RecyclerView.OnScrollListener(){override fun onScrolled(rv:RecyclerView,dx:Int,dy:Int){val lm=layoutManager as GridLayoutManager;if(!loading&&next!=null&&lm.findLastVisibleItemPosition()>this@GifKeyboardService.adapter.itemCount-5)loadPage(false)}})}
  root.addView(list,LinearLayout.LayoutParams(-1,(220*d).toInt()));val bottom=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
  bottom.addView(tab("ABC"){switchBack()},LinearLayout.LayoutParams(0,-2,1f));bottom.addView(TextView(this).apply{text="GIF";gravity=Gravity.CENTER;textSize=14f},LinearLayout.LayoutParams(0,-1,1f));bottom.addView(tab("Keyboard"){(getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()},LinearLayout.LayoutParams(0,-2,1f));root.addView(bottom)
  search.setOnEditorActionListener{_,_,_->val q=search.text?.toString()?.trim().orEmpty();if(q.isNotEmpty())load(q);true};root.post{load("trending")};return root
 }
 private fun load(q:String){currentQuery=q;next=null;adapter.set(emptyList());loadPage(true)}
 private fun loadPage(reset:Boolean){if(loading)return;loading=true;status.text="Loading GIFs...";scope.launch{try{val p=if(currentQuery=="trending")provider.trending(next) else provider.search(currentQuery,next);next=p.next;if(reset)adapter.set(p.items)else adapter.append(p.items);status.text=if(adapter.itemCount==0)"No GIFs found" else "${adapter.itemCount} GIFs - hold to save"}catch(e:Exception){status.text="Couldn't load GIFs - check connection"}finally{loading=false}}}
 private fun commitGif(item:GifItem){status.text="Preparing GIF...";scope.launch{try{val file=withContext(Dispatchers.IO){val req=Request.Builder().url(item.mediaUrl).build();val bytes=http.newCall(req).execute().use{r->if(!r.isSuccessful)error("HTTP ${r.code}");r.body?.bytes()?:error("Empty GIF")};val dir=File(cacheDir,"shared_gifs").apply{mkdirs()};File(dir,"gif_${System.currentTimeMillis()}.gif").apply{writeBytes(bytes)}};val uri=FileProvider.getUriForFile(this@GifKeyboardService,"$packageName.files",file);val info=InputContentInfoCompat(uri,ClipDescription("GIF",arrayOf("image/gif")),null);val ok=InputConnectionCompat.commitContent(currentInputConnection,currentInputEditorInfo,info,InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION,null);if(ok){remember("recent",item);status.text="GIF inserted"}else status.text="This app doesn't accept GIFs from keyboards"}catch(e:Exception){status.text="Couldn't insert this GIF"}}}
 private fun remember(bucket:String,item:GifItem){val old=prefs.getString(bucket,"").orEmpty().lines().filter{it.isNotBlank()&&it!=item.mediaUrl};prefs.edit().putString(bucket,(listOf(item.mediaUrl)+old).take(30).joinToString("\n")).apply()}
 private fun toggleSaved(item:GifItem){val list=prefs.getString("saved","").orEmpty().lines().filter{it.isNotBlank()}.toMutableList();if(list.remove(item.mediaUrl))status.text="Removed from Saved" else{list.add(0,item.mediaUrl);status.text="Saved GIF"};prefs.edit().putString("saved",list.take(60).joinToString("\n")).apply()}
 private fun showStored(bucket:String){val items=prefs.getString(bucket,"").orEmpty().lines().filter{it.isNotBlank()}.map{GifItem(it.hashCode().toString(),it,it)};next=null;adapter.set(items);status.text=if(items.isEmpty())"Nothing here yet" else "${items.size} GIFs - hold to save"}
 private fun switchBack(){if(Build.VERSION.SDK_INT>=28)switchToPreviousInputMethod()else(getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()}
}