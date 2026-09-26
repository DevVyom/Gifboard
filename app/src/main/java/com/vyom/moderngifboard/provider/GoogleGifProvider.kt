package com.vyom.moderngifboard.provider
import android.net.Uri
import com.vyom.moderngifboard.model.GifItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
class GoogleGifProvider(private val safeSearch:String="off"):GifProvider {
 private val client=OkHttpClient.Builder().connectTimeout(12,TimeUnit.SECONDS).readTimeout(15,TimeUnit.SECONDS).build()
 override suspend fun trending(cursor:String?)=search("trending",cursor)
 override suspend fun search(query:String,cursor:String?):GifPage=withContext(Dispatchers.IO){
  val start=cursor?.toIntOrNull()?:0
  val url=Uri.parse("https://www.google.com/search").buildUpon().appendQueryParameter("q",query).appendQueryParameter("tbm","isch").appendQueryParameter("tbs","itp:animated").appendQueryParameter("safe",safeSearch).appendQueryParameter("start",start.toString()).build().toString()
  val req=Request.Builder().url(url).header("User-Agent","Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/124 Mobile Safari/537.36").build()
  val html=client.newCall(req).execute().use{r->if(!r.isSuccessful) error("Search failed: HTTP ${r.code}");r.body?.string().orEmpty()}
  val re=Regex("""["]ou["]:["](https?[^"]+)["](?:,["]ow["]:(\d+),["]oh["]:(\d+))?""")
  val seen=LinkedHashSet<String>()
  val items=re.findAll(html).mapNotNull{m->val raw=m.groupValues[1].replace("\\u003d","=").replace("\\u0026","&").replace("\\/","/");if(!seen.add(raw)) null else GifItem(raw.hashCode().toString(),raw,raw,m.groupValues.getOrNull(2)?.toIntOrNull()?:0,m.groupValues.getOrNull(3)?.toIntOrNull()?:0)}.take(40).toList()
  GifPage(items,if(items.isEmpty()) null else (start+20).toString())
 }
}